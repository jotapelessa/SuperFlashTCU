package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.FlashcardEntity
import com.example.data.repository.DeckRepository
import com.example.ui.viewmodel.DeckViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class L3InteractionAndLayoutTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DeckRepository
    private lateinit var viewModel: DeckViewModel
    private val context: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DeckRepository(db.flashcardDao())
        viewModel = DeckViewModel(context, repository)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testRenameL3TopicUpdatesAllCardsAndDeckRaw() = runBlocking {
        val card1 = FlashcardEntity(
            l1 = "TCU",
            l2 = "AFO",
            l3 = "Topico Antigo",
            deckRaw = "TCU :: AFO :: Topico Antigo",
            frontHtml = "Pergunta 1",
            backHtml = "Resposta 1",
            contentHash = "hash1"
        )
        val card2 = FlashcardEntity(
            l1 = "TCU",
            l2 = "AFO",
            l3 = "Topico Antigo",
            deckRaw = "TCU :: AFO :: Topico Antigo",
            frontHtml = "Pergunta 2",
            backHtml = "Resposta 2",
            contentHash = "hash2"
        )
        val cardOutro = FlashcardEntity(
            l1 = "TCU",
            l2 = "AFO",
            l3 = "Outro Topico",
            deckRaw = "TCU :: AFO :: Outro Topico",
            frontHtml = "Pergunta Outra",
            backHtml = "Resposta Outra",
            contentHash = "hash3"
        )
        db.flashcardDao().insertAll(listOf(card1, card2, cardOutro))

        viewModel.setActiveL3Topic("TCU", "AFO", "Topico Antigo")
        assertEquals("Topico Antigo", viewModel.getActiveL3Topic("TCU", "AFO"))

        repository.renameL3Topic("TCU", "AFO", "Topico Antigo", "Topico Novo")

        val cards = db.flashcardDao().getAllFlashcards().first()
        val cardsNovo = cards.filter { it.l3 == "Topico Novo" }
        assertEquals(2, cardsNovo.size)
        assertTrue(cardsNovo.all { it.deckRaw == "TCU :: AFO :: Topico Novo" })

        val cardsAntigo = cards.filter { it.l3 == "Topico Antigo" }
        assertEquals(0, cardsAntigo.size)

        val cardsOutroAfter = cards.filter { it.l3 == "Outro Topico" }
        assertEquals(1, cardsOutroAfter.size)
    }

    @Test
    fun testDeleteL3TopicRemovesOnlyTargetSubtopicCards() = runBlocking {
        val card1 = FlashcardEntity(
            l1 = "TCU",
            l2 = "Direito",
            l3 = "Para Deletar",
            deckRaw = "TCU :: Direito :: Para Deletar",
            frontHtml = "Del 1",
            backHtml = "Del 1",
            contentHash = "hash_del"
        )
        val card2 = FlashcardEntity(
            l1 = "TCU",
            l2 = "Direito",
            l3 = "Manter",
            deckRaw = "TCU :: Direito :: Manter",
            frontHtml = "Keep 1",
            backHtml = "Keep 1",
            contentHash = "hash_keep"
        )
        db.flashcardDao().insertAll(listOf(card1, card2))

        repository.deleteL3Topic("TCU", "Direito", "Para Deletar")

        val remaining = db.flashcardDao().getAllFlashcards().first()
        assertEquals(1, remaining.size)
        assertEquals("Manter", remaining[0].l3)
    }
}
