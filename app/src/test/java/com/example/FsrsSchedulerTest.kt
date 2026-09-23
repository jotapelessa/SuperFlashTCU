package com.example

import com.example.data.model.FlashcardEntity
import com.example.data.srs.FsrsScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FsrsSchedulerTest {

    @Test
    fun `test initial stability and interval for new card`() {
        val newCard = FlashcardEntity(
            id = 1L,
            deckRaw = "L1 :: L2 :: L3",
            l1 = "Direito",
            l2 = "Constitucional",
            l3 = "Controle",
            frontHtml = "Pergunta",
            backHtml = "Resposta",
            contentHash = "hash1",
            reps = 0,
            stability = 0f,
            difficulty = 0f
        )

        val resultAgain = FsrsScheduler.schedule(newCard, rating = 1)
        val resultGood = FsrsScheduler.schedule(newCard, rating = 3)
        val resultEasy = FsrsScheduler.schedule(newCard, rating = 4)

        assertTrue("Again stability should be positive", resultAgain.stability > 0.0)
        assertTrue("Good stability should be higher than Again", resultGood.stability > resultAgain.stability)
        assertTrue("Easy stability should be higher than Good", resultEasy.stability > resultGood.stability)

        assertTrue("Again interval should be at least 1 day", resultAgain.intervalDays >= 1)
        assertTrue("Easy interval should be greater or equal to Good interval", resultEasy.intervalDays >= resultGood.intervalDays)
    }

    @Test
    fun `test previewIntervals matches individual schedule outputs for new card`() {
        val newCard = FlashcardEntity(
            id = 2L,
            deckRaw = "L1 :: L2 :: L3",
            l1 = "Direito",
            l2 = "Administrativo",
            l3 = "Licitações",
            frontHtml = "Pergunta 2",
            backHtml = "Resposta 2",
            contentHash = "hash2",
            reps = 0,
            stability = 0f,
            difficulty = 0f
        )

        val targetRetention = 0.90
        val now = 1700000000000L

        val preview = FsrsScheduler.previewIntervals(newCard, targetRetention = targetRetention, now = now)

        val expectedAgain = FsrsScheduler.schedule(newCard, rating = 1, now = now, targetRetention = targetRetention).intervalDays
        val expectedHard = FsrsScheduler.schedule(newCard, rating = 2, now = now, targetRetention = targetRetention).intervalDays
        val expectedGood = FsrsScheduler.schedule(newCard, rating = 3, now = now, targetRetention = targetRetention).intervalDays
        val expectedEasy = FsrsScheduler.schedule(newCard, rating = 4, now = now, targetRetention = targetRetention).intervalDays

        assertEquals("Again preview should match schedule", expectedAgain, preview.againDays)
        assertEquals("Hard preview should match schedule", expectedHard, preview.hardDays)
        assertEquals("Good preview should match schedule", expectedGood, preview.goodDays)
        assertEquals("Easy preview should match schedule", expectedEasy, preview.easyDays)
    }

    @Test
    fun `test previewIntervals matches individual schedule outputs for reviewed card`() {
        val now = 1700000000000L
        val lastReviewed = now - (3L * 24 * 3600 * 1000) // 3 days ago

        val reviewedCard = FlashcardEntity(
            id = 3L,
            deckRaw = "L1 :: L2 :: L3",
            l1 = "AFO",
            l2 = "Orçamento",
            l3 = "Princípios",
            frontHtml = "Princípio da Anualidade",
            backHtml = "Exercício financeiro coincide com ano civil",
            contentHash = "hash3",
            reps = 4,
            intervalDays = 5,
            stability = 6.5f,
            difficulty = 4.2f,
            lastReviewedTimestamp = lastReviewed
        )

        val targetRetention = 0.90

        val preview = FsrsScheduler.previewIntervals(reviewedCard, targetRetention = targetRetention, now = now)

        val expectedAgain = FsrsScheduler.schedule(reviewedCard, rating = 1, now = now, targetRetention = targetRetention).intervalDays
        val expectedHard = FsrsScheduler.schedule(reviewedCard, rating = 2, now = now, targetRetention = targetRetention).intervalDays
        val expectedGood = FsrsScheduler.schedule(reviewedCard, rating = 3, now = now, targetRetention = targetRetention).intervalDays
        val expectedEasy = FsrsScheduler.schedule(reviewedCard, rating = 4, now = now, targetRetention = targetRetention).intervalDays

        assertEquals("Again preview should match schedule", expectedAgain, preview.againDays)
        assertEquals("Hard preview should match schedule", expectedHard, preview.hardDays)
        assertEquals("Good preview should match schedule", expectedGood, preview.goodDays)
        assertEquals("Easy preview should match schedule", expectedEasy, preview.easyDays)
    }
}
