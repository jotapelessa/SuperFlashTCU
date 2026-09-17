package com.example

import com.example.data.ai.GeminiStudyAnalyzer
import com.example.data.model.FlashcardEntity
import com.example.data.model.L1DeckSummary
import com.example.data.model.StudyProgressReport
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GeminiStudyAnalyzerTest {

  @Test
  fun `test blank api key returns failure`() = runTest {
    val analyzer = GeminiStudyAnalyzer(defaultApiKeyProvider = { "" })
    val result = analyzer.analyzeStudyData(
      l1Decks = emptyList(),
      allCards = emptyList(),
      progressReport = StudyProgressReport(),
      todayReviewed = 0,
      dailyGoal = 20,
      customApiKey = ""
    )

    assertTrue("Should fail with blank key", result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("Chave da API") == true)
  }

  @Test
  fun `test successful response on primary model`() = runTest {
    val mockClient = OkHttpClient.Builder()
      .addInterceptor { chain ->
        val url = chain.request().url.toString()
        assertTrue("Request should target gemini-flash-latest", url.contains("gemini-flash-latest"))
        Response.Builder()
          .request(chain.request())
          .protocol(Protocol.HTTP_1_1)
          .code(200)
          .message("OK")
          .body("""
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      { "text": "## Análise do Progresso\nVocê está indo muito bem!" }
                    ],
                    "role": "model"
                  }
                }
              ]
            }
          """.trimIndent().toResponseBody("application/json".toMediaType()))
          .build()
      }
      .build()

    val analyzer = GeminiStudyAnalyzer(client = mockClient)
    val result = analyzer.analyzeStudyData(
      l1Decks = listOf(
        L1DeckSummary(
          l1 = "Direito Constitucional",
          totalCards = 20,
          dueCards = 5,
          masteredCards = 12,
          learningCards = 3,
          newCards = 0,
          l2Count = 2,
          l3Count = 4
        )
      ),
      allCards = emptyList(),
      progressReport = StudyProgressReport(totalCards = 20, totalReviews = 10, currentStreakDays = 3),
      todayReviewed = 5,
      dailyGoal = 20,
      customApiKey = "AIzaSyTestValidKey"
    )

    assertTrue("Analysis should succeed", result.isSuccess)
    val content = result.getOrNull()
    assertTrue("Content should contain expected analysis", content?.contains("Você está indo muito bem!") == true)
  }

  @Test
  fun `test waterfall fallback when primary model returns 404`() = runTest {
    val attemptedModels = mutableListOf<String>()

    val mockClient = OkHttpClient.Builder()
      .addInterceptor { chain ->
        val url = chain.request().url.toString()
        if (url.contains("gemini-flash-latest")) {
          attemptedModels.add("gemini-flash-latest")
          Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not Found")
            .body("""{"error":{"code":404,"message":"Model not found"}}""".toResponseBody("application/json".toMediaType()))
            .build()
        } else if (url.contains("gemini-flash-lite-latest")) {
          attemptedModels.add("gemini-flash-lite-latest")
          Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("""
              {
                "candidates": [
                  {
                    "content": {
                      "parts": [
                        { "text": "Resposta de fallback do gemini-flash-lite-latest com sucesso." }
                      ]
                    }
                  }
                ]
              }
            """.trimIndent().toResponseBody("application/json".toMediaType()))
            .build()
        } else {
          Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Error")
            .body("".toResponseBody("application/json".toMediaType()))
            .build()
        }
      }
      .build()

    val analyzer = GeminiStudyAnalyzer(client = mockClient)
    val result = analyzer.analyzeStudyData(
      l1Decks = emptyList(),
      allCards = emptyList(),
      progressReport = StudyProgressReport(),
      todayReviewed = 0,
      dailyGoal = 20,
      customApiKey = "AIzaSyTestValidKey"
    )

    assertTrue("Result should succeed via fallback", result.isSuccess)
    assertEquals(listOf("gemini-flash-latest", "gemini-flash-lite-latest"), attemptedModels)
    assertTrue(result.getOrNull()?.contains("Resposta de fallback") == true)
  }

  @Test
  fun `test failure when all cascade models fail`() = runTest {
    val mockClient = OkHttpClient.Builder()
      .addInterceptor { chain ->
        Response.Builder()
          .request(chain.request())
          .protocol(Protocol.HTTP_1_1)
          .code(503)
          .message("Service Unavailable")
          .body("""{"error":{"code":503,"message":"High demand"}}""".toResponseBody("application/json".toMediaType()))
          .build()
      }
      .build()

    val analyzer = GeminiStudyAnalyzer(client = mockClient)
    val result = analyzer.analyzeStudyData(
      l1Decks = emptyList(),
      allCards = emptyList(),
      progressReport = StudyProgressReport(),
      todayReviewed = 0,
      dailyGoal = 20,
      customApiKey = "AIzaSyTestValidKey"
    )

    assertTrue("Result should be failure when all models fail", result.isFailure)
    assertTrue("Error should mention HTTP 503", result.exceptionOrNull()?.message?.contains("503") == true)
  }
}
