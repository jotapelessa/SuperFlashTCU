package com.example.data.remote

import com.example.data.model.FlashcardEntity
import com.example.data.model.StudyProgressReport
import kotlinx.coroutines.Dispatchers
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

class SupabaseSyncManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
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
                        // Table doesn't exist yet, test root health or key endpoint
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
                SupabaseSyncResult.Error("Falha na conexão: ${e.localizedMessage ?: "Erro desconhecido"}")
            }
        }

    suspend fun uploadFullData(
        supabaseUrl: String,
        supabaseKey: String,
        cards: List<FlashcardEntity>,
        progressReport: StudyProgressReport?,
        settingsJson: String
    ): SupabaseSyncResult = withContext(Dispatchers.IO) {
        val cleanUrl = sanitizeUrl(supabaseUrl)
        if (cleanUrl.isBlank() || supabaseKey.isBlank()) {
            return@withContext SupabaseSyncResult.Error("URL ou Chave do Supabase não configuradas.")
        }

        try {
            // 1. Convert Cards to JSONArray
            val cardsArray = JSONArray()
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
                }
                cardsArray.put(obj)
            }

            // 2. Try uploading cards to app_flashcards table
            val cardsReq = Request.Builder()
                .url("$cleanUrl/rest/v1/app_flashcards")
                .addHeader("apikey", supabaseKey.trim())
                .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                .post(cardsArray.toString().toRequestBody(jsonMediaType))
                .build()

            var cardsUploaded = false
            client.newCall(cardsReq).execute().use { resp ->
                if (resp.isSuccessful || resp.code == 201 || resp.code == 204) {
                    cardsUploaded = true
                }
            }

            // 3. Dual strategy: Also upload full backup payload to `app_sync_backups` table
            val fullPayloadObj = JSONObject().apply {
                put("key", "master_backup")
                put("updated_at", System.currentTimeMillis())
                put("cards_count", cards.size)
                put("cards_json", cardsArray.toString())
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

            if (cardsUploaded || backupUploaded) {
                SupabaseSyncResult.Success(
                    "Sincronização concluída com sucesso! ${cards.size} flashcards e configurações enviados ao Supabase.",
                    cards.size
                )
            } else {
                SupabaseSyncResult.Error("Não foi possível enviar para as tabelas Supabase. Verifique se as permissões de gravação ou RLS estão configuradas.")
            }
        } catch (e: Exception) {
            SupabaseSyncResult.Error("Erro na sincronização: ${e.localizedMessage ?: "Falha ao enviar para o Supabase"}")
        }
    }

    suspend fun downloadFullData(
        supabaseUrl: String,
        supabaseKey: String
    ): Pair<List<FlashcardEntity>?, String?> = withContext(Dispatchers.IO) {
        val cleanUrl = sanitizeUrl(supabaseUrl)
        if (cleanUrl.isBlank() || supabaseKey.isBlank()) {
            return@withContext Pair(null, "URL ou Chave do Supabase não configuradas.")
        }

        try {
            // First check backup payload in app_sync_backups
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
                        if (cardsJsonStr.isNotBlank()) {
                            val cardsList = parseCardsFromJsonArrayStr(cardsJsonStr)
                            return@withContext Pair(cardsList, null)
                        }
                    }
                }
            }

            // Fallback: fetch directly from app_flashcards
            val cardsReq = Request.Builder()
                .url("$cleanUrl/rest/v1/app_flashcards?select=*&limit=10000")
                .addHeader("apikey", supabaseKey.trim())
                .addHeader("Authorization", "Bearer ${supabaseKey.trim()}")
                .get()
                .build()

            client.newCall(cardsReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val bodyStr = resp.body?.string().orEmpty()
                    val cardsList = parseCardsFromJsonArrayStr(bodyStr)
                    return@withContext Pair(cardsList, null)
                } else {
                    return@withContext Pair(null, "Erro HTTP ${resp.code} ao baixar dados do Supabase.")
                }
            }
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
                    lastReviewedTimestamp = obj.optLong("last_reviewed_timestamp", 0L)
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
