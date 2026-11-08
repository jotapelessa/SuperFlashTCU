package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.FlashcardEntity
import com.example.data.repository.DeckRepository
import com.example.ui.viewmodel.DeckViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class L3GoalCelebrationAndRotationTest {

    private lateinit var app: android.app.Application
    private lateinit var db: AppDatabase
    private lateinit var repository: DeckRepository
    private lateinit var viewModel: DeckViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        // Limpa preferências de testes anteriores
        app.getSharedPreferences("anki_user_prefs", Context.MODE_PRIVATE).edit().clear().apply()

        db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DeckRepository(db.flashcardDao(), db.reviewLogDao())
        viewModel = DeckViewModel(app, repository)
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        db.close()
    }

    private fun createCard(id: Long, l1: String, l2: String, l3: String): FlashcardEntity {
        return FlashcardEntity(
            id = id,
            deckRaw = "$l1::$l2::$l3",
            l1 = l1,
            l2 = l2,
            l3 = l3,
            noteType = "Básico",
            frontHtml = "Pergunta $id",
            backHtml = "Resposta $id",
            tags = "tag",
            contentHash = "hash_$id",
            intervalDays = 0,
            easeFactor = 2.5f,
            reps = 0,
            lapses = 0,
            masteryLevel = 0,
            dueTimestamp = 0L,
            lastReviewedTimestamp = 0L,
            stability = 0f,
            difficulty = 0f
        )
    }

    @Test
    fun testL3GoalCelebrationTriggerIdempotence() {
        val l1 = "TCU"
        val l2 = "Controle Externo"
        val l3 = "Lei Orgânica"

        // 1. Antes de bater a meta: não deve disparar celebração
        assertFalse(viewModel.shouldCelebrateL3Topic(l1, l2, l3, currentReviewed = 1, targetCards = 2))

        // 2. Ao atingir a meta (2 >= 2): dispara exatamente uma vez
        assertTrue(viewModel.shouldCelebrateL3Topic(l1, l2, l3, currentReviewed = 2, targetCards = 2))

        // 3. Ao continuar estudando o mesmo tópico no mesmo dia: não deve redisparar em loop
        assertFalse(viewModel.shouldCelebrateL3Topic(l1, l2, l3, currentReviewed = 3, targetCards = 2))
    }

    @Test
    fun testSequentialRotationThroughL3TopicsAndCycleCompletion() {
        val l1 = "Auditor TCU"
        val l2 = "AFO"
        val cards = listOf(
            createCard(1L, l1, l2, "01. Princípios"),
            createCard(2L, l1, l2, "01. Princípios"),
            createCard(3L, l1, l2, "02. LRF"),
            createCard(4L, l1, l2, "02. LRF"),
            createCard(5L, l1, l2, "03. Receita Pública"),
            createCard(6L, l1, l2, "03. Receita Pública")
        )
        kotlinx.coroutines.runBlocking {
            db.flashcardDao().insertAll(cards)
        }
        viewModel.startStudySession(cards, applySmartShuffle = false)

        // 1. Tópico ativo inicial é o primeiro em ordem alfabética
        val initialL3 = viewModel.getActiveL3Topic(l1, l2)
        assertEquals("01. Princípios", initialL3)

        // 2. Avança de 01. Princípios -> deve ir para 02. LRF
        val nextFrom1 = viewModel.getNextL3TopicCards(l1, l2, "01. Princípios")
        assertNotNull(nextFrom1)
        assertEquals(2, nextFrom1!!.size)
        assertEquals("02. LRF", nextFrom1.first().l3)
        assertEquals("02. LRF", viewModel.getActiveL3Topic(l1, l2))

        // 3. Avança de 02. LRF -> deve ir para 03. Receita Pública
        val nextFrom2 = viewModel.getNextL3TopicCards(l1, l2, "02. LRF")
        assertNotNull(nextFrom2)
        assertEquals(2, nextFrom2!!.size)
        assertEquals("03. Receita Pública", nextFrom2.first().l3)
        assertEquals("03. Receita Pública", viewModel.getActiveL3Topic(l1, l2))

        // 4. Avança do último (03. Receita Pública) -> FECHA O CICLO e volta para 01. Princípios!
        val nextFromLast = viewModel.getNextL3TopicCards(l1, l2, "03. Receita Pública")
        assertNotNull(nextFromLast)
        assertEquals(2, nextFromLast!!.size)
        assertEquals("01. Princípios", nextFromLast.first().l3)
        assertEquals("01. Princípios", viewModel.getActiveL3Topic(l1, l2))
    }

    @Test
    fun testActiveL3TopicPersistenceAcrossSessionsAndDays() {
        val l1 = "TCU"
        val l2 = "Direito Constitucional"
        val cards = listOf(
            createCard(10L, l1, l2, "A. Direitos Fundamentais"),
            createCard(20L, l1, l2, "B. Organização do Estado"),
            createCard(30L, l1, l2, "C. Poder Judiciário")
        )
        kotlinx.coroutines.runBlocking {
            db.flashcardDao().insertAll(cards)
        }
        viewModel.startStudySession(cards, applySmartShuffle = false)

        // Define manualmente ou via estudo que o aluno parou em "B. Organização do Estado"
        viewModel.setActiveL3Topic(l1, l2, "B. Organização do Estado")

        // Simula recriação do ViewModel no dia posterior (novo processo)
        val newViewModel = DeckViewModel(app, repository)
        newViewModel.startStudySession(cards, applySmartShuffle = false)

        // O novo ViewModel deve lembrar perfeitamente de onde o aluno parou
        assertEquals("B. Organização do Estado", newViewModel.getActiveL3Topic(l1, l2))
    }
}
