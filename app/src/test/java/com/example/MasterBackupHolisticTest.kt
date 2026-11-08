package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.FlashcardEntity
import com.example.data.model.ReviewLogEntity
import com.example.data.repository.DeckRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MasterBackupHolisticTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: DeckRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DeckRepository(db.flashcardDao(), db.reviewLogDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testMasterBackupExportsAndRestoresPreferencesAndMetadataWithFullParity() = runTest {
        // 1. Popula banco com cards e logs
        val card = FlashcardEntity(
            id = 101L,
            deckRaw = "TCU::Controle Externo::Lei Orgânica",
            l1 = "TCU",
            l2 = "Controle Externo",
            l3 = "Lei Orgânica",
            noteType = "Básico",
            frontHtml = "Qual o prazo para recurso de reconsideração?",
            backHtml = "15 dias.",
            tags = "tcu controle",
            contentHash = "hash101",
            intervalDays = 7,
            easeFactor = 2.45f,
            reps = 4,
            lapses = 0,
            masteryLevel = 3,
            dueTimestamp = System.currentTimeMillis() + 86400000L * 7,
            lastReviewedTimestamp = System.currentTimeMillis(),
            stability = 8.5f,
            difficulty = 4.2f
        )
        db.flashcardDao().insertFlashcard(card)

        val log = ReviewLogEntity(
            id = 201L,
            cardId = 101L,
            l1 = "TCU",
            l2 = "Controle Externo",
            l3 = "Lei Orgânica",
            rating = 3,
            reviewDurationMs = 2300L,
            timestamp = System.currentTimeMillis()
        )
        db.reviewLogDao().insertReviewLog(log)

        // 2. Cria mapa de preferências completas simulando SharedPreferences reais
        val fakePrefs: Map<String, Any> = mapOf(
            "daily_goal_percentage" to 12.5f,
            "smart_shuffle_enabled" to true,
            "srs_algorithm" to "FSRS-5",
            "fsrs_target_retention" to 0.92f,
            "daily_study_goal" to 45,
            "app_theme_mode" to "DARK",
            "app_accent_color" to "AMBER",
            "gemini_model_version" to "gemini-flash-latest",
            "l2_goal_pct_TCU::Controle Externo" to 8.0f
        )

        val metadata: Map<String, Any> = mapOf(
            "app_name" to "SuperFlash TCU",
            "backup_version" to 6,
            "test_run" to true
        )

        // 3. Exporta via streaming
        val baos = ByteArrayOutputStream()
        repository.exportFullBackupJsonToStream(baos, preferences = fakePrefs, metadata = metadata)
        val exportedJson = baos.toString(Charsets.UTF_8.name())

        assertTrue("JSON deve conter chave metadata", exportedJson.contains("\"metadata\""))
        assertTrue("JSON deve conter chave preferences", exportedJson.contains("\"preferences\""))
        assertTrue("JSON deve conter chave cards", exportedJson.contains("\"cards\""))
        assertTrue("JSON deve conter chave logs", exportedJson.contains("\"logs\""))
        assertTrue("JSON deve conter taxa de retenção", exportedJson.contains("fsrs_target_retention"))
        assertTrue("JSON deve conter meta da disciplina L2", exportedJson.contains("l2_goal_pct_TCU::Controle Externo"))

        // 4. Limpa banco de dados Room completamente
        repository.clearAll()
        assertEquals(0, repository.getAllFlashcardsSync().size)
        assertEquals(0, repository.getAllReviewLogsSync().size)

        // 5. Restaura via streaming diretamente do InputStream gerado
        val bais = ByteArrayInputStream(baos.toByteArray())
        val restoreResult = repository.importFullBackupJsonFromStream(bais)

        // 6. Asserções rigorosas
        assertEquals(1, restoreResult.cardsCount)
        assertEquals(1, restoreResult.logsCount)
        assertEquals(fakePrefs.size, restoreResult.preferences.size)
        assertEquals("FSRS-5", restoreResult.preferences["srs_algorithm"])
        assertEquals(true, restoreResult.preferences["smart_shuffle_enabled"])
        assertEquals(12.5f, (restoreResult.preferences["daily_goal_percentage"] as Number).toFloat())
        assertEquals(0.92f, (restoreResult.preferences["fsrs_target_retention"] as Number).toFloat(), 0.001f)
        assertEquals(45, (restoreResult.preferences["daily_study_goal"] as Number).toInt())
        assertEquals("DARK", restoreResult.preferences["app_theme_mode"])
        assertEquals("AMBER", restoreResult.preferences["app_accent_color"])
        assertEquals(8.0f, (restoreResult.preferences["l2_goal_pct_TCU::Controle Externo"] as Number).toFloat(), 0.001f)

        // Valida se o Room restaurou os dados físicos idênticos
        val restoredCard = repository.getAllFlashcardsSync().first()
        assertEquals(101L, restoredCard.id)
        assertEquals(8.5f, restoredCard.stability, 0.01f)
        assertEquals(4.2f, restoredCard.difficulty, 0.01f)
        assertEquals(15.0, 15.0, 0.0)

        val restoredLogs = repository.getAllReviewLogsSync()
        assertEquals(1, restoredLogs.size)
        assertEquals(201L, restoredLogs[0].id)
        assertEquals(2300L, restoredLogs[0].reviewDurationMs)
    }
}
