package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.FlashcardEntity
import com.example.data.model.L1DeckSummary
import com.example.data.model.StudyProgressReport
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val thought: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiUsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    val usageMetadata: GeminiUsageMetadata? = null
)

data class GeminiAnalysisResult(
    val text: String,
    val promptTokens: Int,
    val candidatesTokens: Int,
    val totalTokens: Int,
    val latencyMs: Long,
    val modelUsed: String
)

class GeminiStudyAnalyzer(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val defaultApiKeyProvider: () -> String = { BuildConfig.GEMINI_API_KEY }
) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    suspend fun analyzeStudyData(
        l1Decks: List<L1DeckSummary>,
        allCards: List<FlashcardEntity>,
        progressReport: StudyProgressReport,
        todayReviewed: Int,
        dailyGoal: Int,
        customQuestion: String? = null,
        customApiKey: String? = null,
        customModelVersion: String? = null
    ): Result<String> {
        return analyzeStudyDataWithMetadata(
            l1Decks = l1Decks,
            allCards = allCards,
            progressReport = progressReport,
            todayReviewed = todayReviewed,
            dailyGoal = dailyGoal,
            customQuestion = customQuestion,
            customApiKey = customApiKey,
            customModelVersion = customModelVersion
        ).map { it.text }
    }

    suspend fun analyzeStudyDataWithMetadata(
        l1Decks: List<L1DeckSummary>,
        allCards: List<FlashcardEntity>,
        progressReport: StudyProgressReport,
        todayReviewed: Int,
        dailyGoal: Int,
        customQuestion: String? = null,
        customApiKey: String? = null,
        customModelVersion: String? = null
    ): Result<GeminiAnalysisResult> = withContext(Dispatchers.IO) {
        val resolvedDefault = defaultApiKeyProvider()
        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey.trim() else resolvedDefault
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Chave da API do Gemini não informada. Configure a API Key nas Configurações do aplicativo.")
            )
        }

        val requestedModel = if (!customModelVersion.isNullOrBlank()) customModelVersion.trim() else "gemini-flash-latest"
        val modelsToTry = listOf(
            requestedModel,
            "gemini-flash-latest",
            "gemini-flash-lite-latest",
            "gemini-2.0-flash"
        ).distinct()

        // 1. Build prompt context from L1/L2/L3 hierarchies and study stats
        val promptBuilder = StringBuilder()
        promptBuilder.appendLine("## ESTATÍSTICAS E PROGRESSO DE ESTUDO (ANKI REPETIÇÃO ESPAÇADA)")
        promptBuilder.appendLine("- Meta Diária: $dailyGoal cards/dia")
        promptBuilder.appendLine("- Cards Revisados Hoje: $todayReviewed")
        promptBuilder.appendLine("- Dias Consecutivos (Streak): ${progressReport.currentStreakDays} dias")
        promptBuilder.appendLine("- Total de Flashcards Cadastrados: ${progressReport.totalCards}")
        promptBuilder.appendLine("- Total de Revisões Realizadas: ${progressReport.totalReviews}")
        promptBuilder.appendLine("- Domínio Geral: ${String.format("%.1f", progressReport.overallMasteryPercentage)}%")
        promptBuilder.appendLine("- Distribuição: Vencidos=${progressReport.dueNowCount}, Aprendendo=${progressReport.learningCount}, Dominados=${progressReport.masteredCount}, Novos=${progressReport.newCount}")
        promptBuilder.appendLine()

        promptBuilder.appendLine("## HIERARQUIA DE BARALHOS (L1 / L2 / L3)")
        if (l1Decks.isEmpty()) {
            promptBuilder.appendLine("Nenhum baralho cadastrado.")
        } else {
            val nowTime = System.currentTimeMillis()
            l1Decks.forEach { l1 ->
                promptBuilder.appendLine("### [L1] Baralho Principal: ${l1.l1}")
                promptBuilder.appendLine("  - Total Cards: ${l1.totalCards}")
                promptBuilder.appendLine("  - Cards Vencidos (Revisão Imediata): ${l1.dueCards}")
                promptBuilder.appendLine("  - Em Aprendizado: ${l1.learningCards}")
                promptBuilder.appendLine("  - Dominados: ${l1.masteredCards}")
                promptBuilder.appendLine("  - Taxa de Domínio: ${String.format("%.1f", l1.masteryPercentage)}%")

                // Group cards under this L1 by L2 and L3 with smart compacting for 18k+ card bases
                val cardsInL1 = allCards.filter { it.l1 == l1.l1 }
                val l2Groups = cardsInL1.groupBy { it.l2 }
                l2Groups.forEach { (l2Name, l2Cards) ->
                    val l2Due = l2Cards.count { it.dueTimestamp <= nowTime }
                    val l2Mastered = l2Cards.count { it.masteryLevel >= 2 }
                    promptBuilder.appendLine("    * [L2 Disciplina] $l2Name: ${l2Cards.size} cards (Vencidos: $l2Due, Dominados: $l2Mastered)")

                    val l3Groups = l2Cards.groupBy { it.l3 }
                    val dueL3Groups = l3Groups.filter { (_, cards) -> cards.any { it.dueTimestamp <= nowTime } }
                    val topicsToShow = dueL3Groups.entries.take(6)
                    topicsToShow.forEach { (l3Name, l3Cards) ->
                        val l3Due = l3Cards.count { it.dueTimestamp <= nowTime }
                        promptBuilder.appendLine("      - [L3 Tópico Crítico] $l3Name: ${l3Cards.size} cards (Vencidos: $l3Due)")
                    }
                    val remainingDue = dueL3Groups.size - topicsToShow.size
                    if (remainingDue > 0) {
                        promptBuilder.appendLine("      - (+ $remainingDue outros tópicos com cards vencidos)")
                    }
                }
                promptBuilder.appendLine()
            }
        }

        if (!customQuestion.isNullOrBlank()) {
            promptBuilder.appendLine("## PERGUNTA ESPECÍFICA DO ESTUDANTE:")
            promptBuilder.appendLine(customQuestion.trim())
        } else {
            promptBuilder.appendLine("## SOLICITAÇÃO:")
            promptBuilder.appendLine("Realize uma análise detalhada sobre o progresso e estatísticas de estudos L1/L2/L3. Identifique pontos fortes, gargalos de aprendizagem por disciplina, e forneça um plano de ação prioritário com recomendações práticas para o estudante.")
        }

        val systemInstructionText = """
            Você é o Tutor de IA do Anki, especialista em Metodologia de Repetição Espaçada (SRS), Ciência da Aprendizagem e preparação para exames de alta performance.
            Analise rigorosamente a hierarquia de baralhos L1, L2 (Disciplinas) e L3 (Tópicos), bem como as metas diárias, vencimentos e taxas de domínio fornecidas.
            Sua resposta deve ser amigável, motivadora e estruturada em Markdown claro com os seguintes tópicos:
            1. 📊 Diagnóstico Geral de Desempenho (L1/L2/L3)
            2. ⚠️ Disciplinas e Tópicos Críticos (Alertas de Gargalo)
            3. 🎯 Plano de Ação Prioritário para Hoje e Próximos 7 Dias
            4. 💡 Dicas Personalizadas de Repetição Espaçada
            Seja direto, objetivo e use bullet points formatados com negritos para facilitar a leitura rápida no celular.
        """.trimIndent()

        val requestPayload = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = promptBuilder.toString()))
                )
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemInstructionText))
            )
        )

        var lastException: Throwable? = null

        val jsonString = requestAdapter.toJson(requestPayload)
        val body = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())

        for (modelName in modelsToTry) {
            val startTime = System.currentTimeMillis()
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val latency = System.currentTimeMillis() - startTime
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val parsedResponse = responseAdapter.fromJson(responseBody)

                    val textParts = parsedResponse?.candidates?.firstOrNull()?.content?.parts
                        ?.filter { it.thought != true }
                        ?.mapNotNull { it.text?.takeIf { t -> t.isNotBlank() } }

                    val fullText = textParts?.joinToString("\n\n")

                    if (!fullText.isNullOrBlank()) {
                        val usage = parsedResponse.usageMetadata
                        val pTokens = usage?.promptTokenCount ?: (promptBuilder.length / 4)
                        val cTokens = usage?.candidatesTokenCount ?: (fullText.length / 4)
                        val tTokens = usage?.totalTokenCount ?: (pTokens + cTokens)

                        return@withContext Result.success(
                            GeminiAnalysisResult(
                                text = fullText,
                                promptTokens = pTokens,
                                candidatesTokens = cTokens,
                                totalTokens = tTokens,
                                latencyMs = latency,
                                modelUsed = modelName
                            )
                        )
                    } else {
                        lastException = Exception("O modelo $modelName não retornou texto utilizável.")
                    }
                } else {
                    val code = response.code
                    val msg = response.message
                    lastException = Exception("Falha na chamada da API Gemini para o modelo $modelName (HTTP $code): $msg")
                    android.util.Log.w("GeminiStudyAnalyzer", "Falha no modelo $modelName (HTTP $code). Tentando modelo alternativo se houver...")
                }
            } catch (e: Exception) {
                lastException = e
                android.util.Log.w("GeminiStudyAnalyzer", "Exceção ao chamar $modelName: ${e.message}. Tentando próximo modelo...")
            }
        }

        Result.failure<GeminiAnalysisResult>(lastException ?: Exception("Não foi possível obter resposta da API Gemini."))
    }

    suspend fun testApiKeyConnection(
        customApiKey: String? = null,
        modelVersion: String? = null
    ): Result<Pair<Long, String>> = withContext(Dispatchers.IO) {
        val resolvedDefault = defaultApiKeyProvider()
        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey.trim() else resolvedDefault
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Nenhuma chave informada para teste.")
            )
        }

        val primary = if (!modelVersion.isNullOrBlank()) modelVersion.trim() else "gemini-flash-latest"
        val modelsToTry = listOf(
            primary,
            "gemini-flash-latest",
            "gemini-flash-lite-latest",
            "gemini-2.0-flash"
        ).distinct()

        val payload = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = "ping"))
                )
            )
        )
        val body = requestAdapter.toJson(payload).toRequestBody("application/json; charset=utf-8".toMediaType())

        var lastException: Exception? = null

        for (m in modelsToTry) {
            val startTime = System.currentTimeMillis()
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$m:generateContent?key=$apiKey"
                val request = Request.Builder().url(url).post(body).build()
                val response = client.newCall(request).execute()
                val latency = System.currentTimeMillis() - startTime
                if (response.isSuccessful) {
                    return@withContext Result.success(Pair(latency, m))
                } else {
                    val code = response.code
                    lastException = Exception("HTTP $code ($m): ${response.message}")
                    android.util.Log.w("GeminiStudyAnalyzer", "Falha de teste em $m (HTTP $code). Tentando fallback...")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        Result.failure(lastException ?: Exception("Falha ao testar conexão com a API Gemini."))
    }
}
