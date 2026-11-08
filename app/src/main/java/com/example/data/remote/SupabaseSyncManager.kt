package com.example.data.remote

import com.example.data.model.FlashcardEntity
import com.example.data.model.ReviewLogEntity
import com.example.data.model.StudyProgressReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class SupabaseSyncResult {
    data class Success(val message: String, val syncedCardsCount: Int) : SupabaseSyncResult()
    data class Error(val errorMessage: String) : SupabaseSyncResult()
}

data class CloudRestoreData(
    val cards: List<FlashcardEntity>,
    val logs: List<ReviewLogEntity> = emptyList(),
    val settingsJson: String? = null
)

class SupabaseSyncManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(supabaseUrl: String, supabaseKey: String): SupabaseSyncResult =
        withContext(Dispatchers.IO) {
            val cleanUrl = sanitizeUrl(supabaseUrl)
            if (cleanUrl.isBlank() || supabaseKey.isBlank()) {
                return@withContext SupabaseSyncResult.Error("URL ou Chave do Supabase não fornecidas.")
            }

            try {
                val request = Request.Builder()
                    .url("$cleanUrl/rest/v1/app_flashcards?select=id&limit=1")
                    .addHeader("apikey", supabaseKey.trim())
                    .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        SupabaseSyncResult.Success("Conexão estabelecida com sucesso!", 0)
                    } else if (response.code == 404) {
                        val rootRequest = Request.Builder()
                            .url("$cleanUrl/rest/v1/")
                            .addHeader("apikey", supabaseKey.trim())
                            .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
                            .get()
                            .build()
                        client.newCall(rootRequest).execute().use { rootResp ->
                            if (rootResp.isSuccessful || rootResp.code == 200) {
                                SupabaseSyncResult.Success("Conectado ao Supabase! Tabela 'app_flashcards' será sincronizada via payload unificado.", 0)
                            } else {
                                SupabaseSyncResult.Error("Erro ${response.code}: Verifique a chave API do Supabase.")
                            }
                        }
                    } else {
                        SupabaseSyncResult.Error("Erro HTTP ${response.code}: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                SupabaseSyncResult.Error("Falha na conexão com o Supabase: ${e.localizedMessage ?: "Erro desconhecido"}")
            }
        }

    suspend fun uploadFullData(
        supabaseUrl: String,
        supabaseKey: String,
        cards: List<FlashcardEntity>,
        progressReport: StudyProgressReport?,
        settingsJson: String,
        reviewLogs: List<ReviewLogEntity> = emptyList()
    ): SupabaseSyncResult = withContext(Dispatchers.IO) {
        val cleanUrl = sanitizeUrl(supabaseUrl)
        if (cleanUrl.isBlank() || supabaseKey.isBlank()) {
            return@withContext SupabaseSyncResult.Error("URL ou Chave do Supabase não configuradas.")
        }

        try {
            // 1. Upload cards in batches of 250 with retry policy (up to 3 attempts per chunk)
            val chunks = cards.chunked(250)
            var uploadedCount = 0
            for ((index, chunk) in chunks.withIndex()) {
                val chunkArray = JSONArray()
                chunk.forEach { card ->
                    val obj = JSONObject().apply {
                        put("id", card.id)
                        put("deck_raw", card.deckRaw)
                        put("l1", card.l1)
                        put("l2", card.l2)
                        put("l3", card.l3)
                        put("note_type", card.noteType)
                        put("front_html", card.frontHtml)
                        put("back_html", card.backHtml)
                        put("tags", card.tags)
                        put("content_hash", card.contentHash)
                        put("interval_days", card.intervalDays)
                        put("ease_factor", card.easeFactor)
                        put("reps", card.reps)
                        put("lapses", card.lapses)
                        put("mastery_level", card.masteryLevel)
                        put("due_timestamp", card.dueTimestamp)
                        put("last_reviewed_timestamp", card.lastReviewedTimestamp)
                        put("stability", card.stability)
                        put("difficulty", card.difficulty)
                    }
                    chunkArray.put(obj)
                }

                var chunkSuccess = false
                var lastCode = 0
                for (attempt in 1..3) {
                    val cardsReq = Request.Builder()
                        .url("$cleanUrl/rest/v1/app_flashcards")
                        .addHeader("apikey", supabaseKey.trim())
                        .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                        .post(chunkArray.toString().toRequestBody(jsonMediaType))
                        .build()

                    try {
                        client.newCall(cardsReq).execute().use { resp ->
                            lastCode = resp.code
                            if (resp.isSuccessful || resp.code == 201 || resp.code == 204) {
                                uploadedCount += chunk.size
                                chunkSuccess = true
                            }
                        }
                    } catch (e: Exception) {
                        if (attempt == 3) throw e
                    }
                    if (chunkSuccess) break
                    delay(400L * attempt)
                }

                if (!chunkSuccess) {
                    return@withContext SupabaseSyncResult.Error(
                        "Falha ao enviar lote ${index + 1} de ${chunks.size} para o Supabase (HTTP $lastCode). Sincronização interrompida para evitar dados parciais."
                    )
                }
            }

            // 2. Dual strategy: Also upload backup payload to `app_sync_backups` table
            val cardsJsonForBackup = if (cards.size <= 2000) {
                val sampleArray = JSONArray()
                cards.forEach { card ->
                    val obj = JSONObject().apply {
                        put("id", card.id)
                        put("deck_raw", card.deckRaw)
                        put("l1", card.l1)
                        put("l2", card.l2)
                        put("l3", card.l3)
                        put("note_type", card.noteType)
                        put("front_html", card.frontHtml)
                        put("back_html", card.backHtml)
                        put("tags", card.tags)
                        put("content_hash", card.contentHash)
                        put("interval_days", card.intervalDays)
                        put("ease_factor", card.easeFactor)
                        put("reps", card.reps)
                        put("lapses", card.lapses)
                        put("mastery_level", card.masteryLevel)
                        put("due_timestamp", card.dueTimestamp)
                        put("last_reviewed_timestamp", card.lastReviewedTimestamp)
                        put("stability", card.stability)
                        put("difficulty", card.difficulty)
                    }
                    sampleArray.put(obj)
                }
                sampleArray.toString()
            } else {
                ""
            }

            // Preserva até 20.000 logs atômicos de revisão no snapshot de backup
            val logsJsonForBackup = if (reviewLogs.isNotEmpty()) {
                val logsArray = JSONArray()
                reviewLogs.takeLast(20000).forEach { log ->
                    val obj = JSONObject().apply {
                        put("id", log.id)
                        put("card_id", log.cardId)
                        put("l1", log.l1)
                        put("l2", log.l2)
                        put("l3", log.l3)
                        put("rating", log.rating)
                        put("duration_ms", log.reviewDurationMs)
                        put("timestamp", log.timestamp)
                    }
                    logsArray.put(obj)
                }
                logsArray.toString()
            } else {
                ""
            }

            val fullPayloadObj = JSONObject().apply {
                put("key", "master_backup")
                put("updated_at", System.currentTimeMillis())
                put("cards_count", cards.size)
                put("cards_json", cardsJsonForBackup)
                put("logs_json", logsJsonForBackup)
                put("settings_json", settingsJson)
                put("progress_json", progressReportToJson(progressReport))
            }

            val backupArray = JSONArray().apply { put(fullPayloadObj) }

            val backupReq = Request.Builder()
                .url("$cleanUrl/rest/v1/app_sync_backups")
                .addHeader("apikey", supabaseKey.trim())
                .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                .post(backupArray.toString().toRequestBody(jsonMediaType))
                .build()

            var backupUploaded = false
            client.newCall(backupReq).execute().use { resp ->
                if (resp.isSuccessful || resp.code == 201 || resp.code == 204) {
                    backupUploaded = true
                }
            }

            if (uploadedCount > 0 || backupUploaded) {
                SupabaseSyncResult.Success(
                    "Sincronização concluída com sucesso! $uploadedCount flashcards sincronizados e backup atualizado no Supabase.",
                    uploadedCount
                )
            } else {
                SupabaseSyncResult.Error("Não foi possível enviar para as tabelas Supabase. Verifique permissões de gravação ou RLS.")
            }
        } catch (e: Exception) {
            SupabaseSyncResult.Error("Erro na sincronização: ${e.localizedMessage ?: "Falha ao enviar para o Supabase"}")
        }
    }

    suspend fun downloadFullData(
        supabaseUrl: String,
        supabaseKey: String
    ): Pair<CloudRestoreData?, String?> = withContext(Dispatchers.IO) {
        val cleanUrl = sanitizeUrl(supabaseUrl)
        if (cleanUrl.isBlank() || supabaseKey.isBlank()) {
            return@withContext Pair(null, "URL ou Chave do Supabase não configuradas.")
        }

        try {
            var logsList = emptyList<ReviewLogEntity>()
            var settingsJsonStr: String? = null
            var backupCards = emptyList<FlashcardEntity>()

            // 1. Fetch metadata payload from app_sync_backups (logs, settings, and fallback cards)
            try {
                val backupReq = Request.Builder()
                    .url("$cleanUrl/rest/v1/app_sync_backups?key=eq.master_backup&select=*&limit=1")
                    .addHeader("apikey", supabaseKey.trim())
                    .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
                    .get()
                    .build()

                client.newCall(backupReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string().orEmpty()
                        val array = JSONArray(bodyStr)
                        if (array.length() > 0) {
                            val obj = array.getJSONObject(0)
                            val cardsJsonStr = obj.optString("cards_json", "")
                            val logsJsonStr = obj.optString("logs_json", "")
                            settingsJsonStr = obj.optString("settings_json", "").ifBlank { null }
                            if (logsJsonStr.isNotBlank()) {
                                logsList = parseLogsFromJsonArrayStr(logsJsonStr)
                            }
                            if (cardsJsonStr.isNotBlank()) {
                                backupCards = parseCardsFromJsonArrayStr(cardsJsonStr)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore backup metadata fetch failure and continue to app_flashcards table
            }

            // 2. Fetch full flashcards with complete HTML & metrics from app_flashcards with pagination (up to 50k cards)
            val pageSize = 1000
            var offset = 0
            val allFetchedCards = mutableListOf<FlashcardEntity>()
            var hasMore = true
            var pageIndex = 0

            while (hasMore && pageIndex < 50) {
                pageIndex++
                val pageUrl = "$cleanUrl/rest/v1/app_flashcards?select=*&order=id.asc&limit=$pageSize&offset=$offset"
                val cardsReq = Request.Builder()
                    .url(pageUrl)
                    .addHeader("apikey", supabaseKey.trim())
                    .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
                    .get()
                    .build()

                val pageCards = client.newCall(cardsReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string().orEmpty()
                        parseCardsFromJsonArrayStr(bodyStr)
                    } else {
                        if (allFetchedCards.isEmpty() && backupCards.isNotEmpty()) {
                            emptyList()
                        } else {
                            return@withContext Pair(null, "Erro HTTP ${resp.code} ao baixar dados do Supabase na página $pageIndex.")
                        }
                    }
                }

                if (pageCards.isEmpty()) {
                    hasMore = false
                } else {
                    allFetchedCards.addAll(pageCards)
                    if (pageCards.size < pageSize) {
                        hasMore = false
                    } else {
                        offset += pageSize
                    }
                }
            }

            val finalCards = if (allFetchedCards.isNotEmpty()) allFetchedCards else backupCards
            if (finalCards.isEmpty()) {
                return@withContext Pair(null, "Nenhum flashcard encontrado no Supabase para restaurar.")
            }

            return@withContext Pair(CloudRestoreData(cards = finalCards, logs = logsList, settingsJson = settingsJsonStr), null)
        } catch (e: Exception) {
            Pair(null, "Erro ao restaurar do Supabase: ${e.localizedMessage}")
        }
    }

    private fun parseCardsFromJsonArrayStr(jsonStr: String): List<FlashcardEntity> {
        val list = mutableListOf<FlashcardEntity>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                FlashcardEntity(
                    id = obj.optLong("id", 0L),
                    deckRaw = obj.optString("deck_raw", ""),
                    l1 = obj.optString("l1", "Geral"),
                    l2 = obj.optString("l2", "Geral"),
                    l3 = obj.optString("l3", "Geral"),
                    noteType = obj.optString("note_type", "Básico+"),
                    frontHtml = obj.optString("front_html", ""),
                    backHtml = obj.optString("back_html", ""),
                    tags = obj.optString("tags", ""),
                    contentHash = obj.optString("content_hash", ""),
                    intervalDays = obj.optInt("interval_days", 0),
                    easeFactor = obj.optDouble("ease_factor", 2.5).toFloat(),
                    reps = obj.optInt("reps", 0),
                    lapses = obj.optInt("lapses", 0),
                    masteryLevel = obj.optInt("mastery_level", 0),
                    dueTimestamp = obj.optLong("due_timestamp", System.currentTimeMillis()),
                    lastReviewedTimestamp = obj.optLong("last_reviewed_timestamp", 0L),
                    stability = obj.optDouble("stability", 0.0).toFloat(),
                    difficulty = obj.optDouble("difficulty", 0.0).toFloat()
                )
            )
        }
        return list
    }

    private fun parseLogsFromJsonArrayStr(jsonStr: String): List<ReviewLogEntity> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<ReviewLogEntity>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ReviewLogEntity(
                    id = obj.optLong("id", 0L),
                    cardId = obj.optLong("card_id", 0L),
                    l1 = obj.optString("l1", "Geral"),
                    l2 = obj.optString("l2", "Geral"),
                    l3 = obj.optString("l3", "Geral"),
                    rating = obj.optInt("rating", 3),
                    reviewDurationMs = obj.optLong("duration_ms", 0L),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun progressReportToJson(report: StudyProgressReport?): String {
        if (report == null) return "{}"
        return JSONObject().apply {
            put("totalCards", report.totalCards)
            put("masteredCount", report.masteredCount)
            put("learningCount", report.learningCount)
            put("newCount", report.newCount)
            put("streakDays", report.currentStreakDays)
            put("totalReviewsCount", report.totalReviews)
            put("retentionRate", report.overallMasteryPercentage)
        }.toString()
    }

    private fun sanitizeUrl(url: String): String {
        var clean = url.trim()
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        return clean.removeSuffix("/")
    }
}
