package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.importer.AnkiCsvImporter
import com.example.data.importer.SampleData
import com.example.data.local.AppDatabase
import com.example.data.repository.DeckRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Flashcard Decks", appName)
  }

  @Test
  fun `test anki csv importer parses L1 L2 L3 hierarchy strictly`() {
    val rawDeck = "Auditor TCU/TCEs/TCDF - PARTE 1::Direito Constitucional::a. Constituição: conceito, objeto, elementos e classificações"
    val hierarchy = AnkiCsvImporter.parseDeckHierarchy(rawDeck)
    assertEquals("Auditor TCU/TCEs/TCDF - PARTE 1", hierarchy.l1)
    assertEquals("Direito Constitucional", hierarchy.l2)
    assertEquals("a. Constituição: conceito, objeto, elementos e classificações", hierarchy.l3)
  }

  @Test
  fun `test anki csv importer preserves HTML`() {
    val result = AnkiCsvImporter.parseAnkiCsv(SampleData.sampleCsv)
    assertTrue("Should parse valid cards", result.validCards.isNotEmpty())
    val firstCard = result.validCards.first()
    assertTrue("Should preserve div HTML", firstCard.backHtml.contains("<div style=\"text-align: justify;\">"))
    assertTrue("Should preserve color span/b HTML", firstCard.backHtml.contains("color: rgb(255, 0, 0)"))
  }

  @Test
  fun `test pre-import check prevents redundant cards and duplicate folders on subsequent imports`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    val repository = DeckRepository(db.flashcardDao())

    // 1. Initial import of sample cards
    val initialResult = repository.importAnkiCsv(SampleData.sampleCsv)
    assertTrue("Initial import should insert cards", initialResult.validCards.isNotEmpty())
    val initialCount = db.flashcardDao().countTotal()
    assertTrue("DB should have inserted cards", initialCount > 0)

    // 2. Pre-import check on subsequent exact import
    val checkReport = repository.preImportCheck(SampleData.sampleCsv)
    assertEquals(initialCount, checkReport.redundantCardsSkipped)
    assertEquals(0, checkReport.newCardsToInsert)
    assertTrue("Should be detected as fully duplicate deck", checkReport.isFullyDuplicateDeck)
    assertTrue("L1 folders should be matched against DB", checkReport.matchedExistingL1Folders.isNotEmpty())
    assertTrue("No new L1 folders should be created", checkReport.newL1FoldersToCreate.isEmpty())

    // 3. Subsequent import should not insert any duplicate cards
    val secondResult = repository.importAnkiCsv(SampleData.sampleCsv)
    assertEquals(0, secondResult.validCards.size)
    assertEquals(initialCount, secondResult.skippedDuplicates)
    assertEquals(initialCount, db.flashcardDao().countTotal())

    // 4. Test duplicate folder prevention with casing and extra whitespace
    val messyCsv = """
        Deck;Note type;Field 1;Field 2
        "  Auditor TCU/TCEs/TCDF - PARTE 1  :: DIREITO CONSTITUCIONAL :: a. Constituição: conceito, objeto, elementos e classificações ";"Básico+";"Pergunta Nova Exclusiva";"Resposta Nova Exclusiva"
    """.trimIndent()

    val checkMessy = repository.preImportCheck(messyCsv)
    assertEquals(1, checkMessy.newCardsToInsert)
    assertTrue("Should normalize folder names to DB canonical names", checkMessy.folderNormalizationsApplied > 0)
    assertTrue("Should match existing L1", checkMessy.matchedExistingL1Folders.contains("Auditor TCU/TCEs/TCDF - PARTE 1"))
    assertTrue("Should match existing L2", checkMessy.matchedExistingL2Folders.contains("Direito Constitucional"))

    val importMessy = repository.importAnkiCsv(messyCsv)
    assertEquals(1, importMessy.validCards.size)
    // Check that the card was stored with canonical L1 and L2, NOT creating duplicate folders
    val insertedCard = importMessy.validCards.first()
    assertEquals("Auditor TCU/TCEs/TCDF - PARTE 1", insertedCard.l1)
    assertEquals("Direito Constitucional", insertedCard.l2)

    db.close()
  }

  @Test
  fun `test smart shuffle prioritizes overdue cards and lower mastery before new cards`() {
    val now = 1000000L
    // Overdue card: due in the past, reps > 0
    val overdueCard = com.example.data.model.FlashcardEntity(
      id = 1L,
      deckRaw = "L1::L2::L3",
      l1 = "L1", l2 = "L2", l3 = "L3",
      frontHtml = "Overdue", backHtml = "Ans",
      contentHash = "hash1",
      reps = 3, intervalDays = 1, masteryLevel = 1,
      dueTimestamp = now - 50000L
    )

    // Low mastery card (learning, not overdue)
    val lowMasteryCard = com.example.data.model.FlashcardEntity(
      id = 2L,
      deckRaw = "L1::L2::L3",
      l1 = "L1", l2 = "L2", l3 = "L3",
      frontHtml = "Low Mastery", backHtml = "Ans",
      contentHash = "hash2",
      reps = 2, intervalDays = 2, masteryLevel = 1,
      dueTimestamp = now + 100000L
    )

    // New card (never reviewed, reps == 0)
    val newCard = com.example.data.model.FlashcardEntity(
      id = 3L,
      deckRaw = "L1::L2::L3",
      l1 = "L1", l2 = "L2", l3 = "L3",
      frontHtml = "New Card", backHtml = "Ans",
      contentHash = "hash3",
      reps = 0, intervalDays = 0, masteryLevel = 0,
      dueTimestamp = now
    )

    // Mastered card (masteryLevel == 2, not overdue)
    val masteredCard = com.example.data.model.FlashcardEntity(
      id = 4L,
      deckRaw = "L1::L2::L3",
      l1 = "L1", l2 = "L2", l3 = "L3",
      frontHtml = "Mastered Card", backHtml = "Ans",
      contentHash = "hash4",
      reps = 10, intervalDays = 30, masteryLevel = 2,
      dueTimestamp = now + 500000L
    )

    val shuffled = DeckRepository.applySmartShuffle(
      listOf(masteredCard, newCard, lowMasteryCard, overdueCard),
      now = now
    )

    assertEquals(4, shuffled.size)
    // 1st priority: overdue
    assertEquals(1L, shuffled[0].id)
    // 2nd priority: low mastery
    assertEquals(2L, shuffled[1].id)
    // 3rd priority: new cards
    assertEquals(3L, shuffled[2].id)
    // 4th priority: mastered cards
    assertEquals(4L, shuffled[3].id)
  }

  @Test
  fun `test cross-domain tagging and tag parsing`() {
    val tags1 = DeckRepository.parseCardTags("concursos, fiscal #tcu auditoria")
    assertEquals(listOf("concursos", "fiscal", "tcu", "auditoria"), tags1)

    val card = com.example.data.model.FlashcardEntity(
      id = 10L,
      deckRaw = "L1::L2::L3",
      l1 = "L1", l2 = "L2", l3 = "L3",
      frontHtml = "Q", backHtml = "A",
      contentHash = "hash10",
      tags = "direito-constitucional jurisprudencia STF"
    )

    assertTrue(DeckRepository.hasTag(card, "STF"))
    assertTrue(DeckRepository.hasTag(card, "direito-constitucional"))
    assertTrue(DeckRepository.hasTag(card, "#jurisprudencia"))
    org.junit.Assert.assertFalse(DeckRepository.hasTag(card, "administracao"))

    val allTags = DeckRepository(
      Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext<Context>(),
        AppDatabase::class.java
      ).allowMainThreadQueries().build().flashcardDao()
    ).extractAllTags(listOf(card))

    assertEquals(listOf("STF", "direito-constitucional", "jurisprudencia"), allTags.sorted())
  }

  @Test
  fun `test configurable study timer limits and report formatting`() {
    val config = com.example.data.model.StudyTimerConfig(
      perCardLimit = com.example.data.model.PerCardTimeLimit.THIRTY_SEC,
      targetSessionMinutes = 15
    )
    assertEquals(30, config.perCardLimit.seconds)
    assertEquals(15, config.targetSessionMinutes)

    val breakdown = listOf(
      com.example.data.model.DeckTimeSpent(
        deckName = "Direito Constitucional",
        l1 = "Auditor",
        l2 = "Direito Constitucional",
        timeMillis = 45000L,
        cardsCount = 3
      ),
      com.example.data.model.DeckTimeSpent(
        deckName = "Direito Administrativo",
        l1 = "Auditor",
        l2 = "Direito Administrativo",
        timeMillis = 75000L,
        cardsCount = 5
      )
    )

    val report = com.example.data.model.SessionTimeReport(
      totalTimeMillis = 120000L,
      deckBreakdown = breakdown,
      averageSecondsPerCard = 15f
    )

    assertEquals("2m 0s", report.formattedTotalTime)
    assertEquals(2, report.deckBreakdown.size)
    assertEquals("Ritmo Moderado ⏱️", report.paceLabel)
  }
}

