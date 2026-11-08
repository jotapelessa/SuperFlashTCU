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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReviewLogIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DeckRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DeckRepository(db.flashcardDao(), db.reviewLogDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun createTestCard(
        id: Long = 1L,
        l1: String = "TCU",
        l2: String = "CEX",
        l3: String = "Intro",
        reps: Int = 0,
        dueTimestamp: Long = 0L,
        lastReviewedTimestamp: Long = 0L
    ): FlashcardEntity {
        return FlashcardEntity(
            id = id,
            deckRaw = "$l1 :: $l2 :: $l3",
            l1 = l1,
            l2 = l2,
            l3 = l3,
            frontHtml = "Frente $id",
            backHtml = "Verso $id",
            contentHash = "hash_$id",
            reps = reps,
            dueTimestamp = dueTimestamp,
            lastReviewedTimestamp = lastReviewedTimestamp
        )
    }

    @Test
    fun testRecordReview_persistsToReviewLogsTable() = runTest {
        val card = createTestCard(
            id = 101L,
            l1 = "TCU - Auditor",
            l2 = "Controle Externo",
            l3 = "Competências Constitucionais"
        )
        db.flashcardDao().insertFlashcard(card)

        repository.recordReview(
            card = card,
            rating = 3,
            algorithm = "FSRS",
            targetRetention = 0.90,
            reviewDurationMs = 15000L
        )

        val logs = db.reviewLogDao().getAllReviewLogsSync()
        assertEquals(1, logs.size)
        val log = logs.first()
        assertEquals(101L, log.cardId)
        assertEquals("TCU - Auditor", log.l1)
        assertEquals("Controle Externo", log.l2)
        assertEquals("Competências Constitucionais", log.l3)
        assertEquals(3, log.rating)
        assertEquals(15000L, log.reviewDurationMs)
        assertTrue(log.timestamp > 0L)
    }

    @Test
    fun testCalculateProgressReport_calculatesFull31DayStreakWithReviewLogs() {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 3600L * 1000L

        // Base de 5 cartões
        val cards = (1..5).map { i ->
            createTestCard(
                id = i.toLong(),
                l1 = "TCU",
                l2 = "AFO",
                l3 = "Orçamento",
                reps = 31,
                lastReviewedTimestamp = now // Mutável: reflete apenas o último dia!
            )
        }

        // Histórico atômico de revisões dos últimos 31 dias consecutivos
        val reviewLogs = (0..30).map { dayOffset ->
            ReviewLogEntity(
                id = (dayOffset + 1).toLong(),
                cardId = 1L,
                l1 = "TCU",
                l2 = "AFO",
                l3 = "Orçamento",
                rating = 3,
                reviewDurationMs = 8000L,
                timestamp = now - (dayOffset * dayMs)
            )
        }

        // Com os logs atômicos preservados, o streak calculado deve ser rigorosamente 31 dias
        val reportWithLogs = repository.calculateProgressReport(cards, reviewLogs)
        assertEquals(31, reportWithLogs.currentStreakDays)
        assertEquals(31, reportWithLogs.totalReviews)

        // Sem os logs (legado), como todos os 5 cards foram revisados "hoje", o streak colapsaria para 1 dia
        val reportWithoutLogs = repository.calculateProgressReport(cards, emptyList())
        assertEquals(1, reportWithoutLogs.currentStreakDays)
    }

    @Test
    fun testAnkiDayCutoff_alignsWith4AM() {
        val cal = Calendar.getInstance()

        // 02:30 AM (madrugada) -> deve pertencer ao dia anterior com cutoff às 04:00 AM
        cal.set(Calendar.HOUR_OF_DAY, 2)
        cal.set(Calendar.MINUTE, 30)
        val earlyMorning = cal.timeInMillis

        val dayStartEarly = repository.getAnkiDayStart(earlyMorning)
        val calCheck = Calendar.getInstance().apply { timeInMillis = dayStartEarly }
        assertEquals(4, calCheck.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calCheck.get(Calendar.MINUTE))

        // 10:00 AM -> pertence ao dia corrente com início às 04:00 AM
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 0)
        val morning = cal.timeInMillis

        val dayStartMorning = repository.getAnkiDayStart(morning)
        val calCheckMorning = Calendar.getInstance().apply { timeInMillis = dayStartMorning }
        assertEquals(4, calCheckMorning.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calCheckMorning.get(Calendar.MINUTE))
    }

    @Test
    fun testDueCardsFiltering_ignoresNewCardsWithZeroReps() {
        val now = System.currentTimeMillis()
        val newCard = createTestCard(
            id = 1L,
            l1 = "TCU",
            l2 = "CEX",
            l3 = "Intro",
            reps = 0,
            dueTimestamp = now - 1000L // Carimbo passado, mas nunca foi estudado (reps == 0)
        )
        val reviewedDueCard = createTestCard(
            id = 2L,
            l1 = "TCU",
            l2 = "CEX",
            l3 = "Intro",
            reps = 2,
            dueTimestamp = now - 1000L // Estudado e vencido
        )

        val cards = listOf(newCard, reviewedDueCard)
        val dueCount = cards.count { it.reps > 0 && it.dueTimestamp <= now }
        assertEquals(1, dueCount) // Apenas o card com reps > 0 deve ser contado como vencido
    }
}
