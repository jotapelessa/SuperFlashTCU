package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.FlashcardEntity
import com.example.data.model.isDueForReview
import com.example.data.model.isNew
import com.example.data.repository.DeckRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuditV2ParityTest {

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

    private fun createCard(
        id: Long = 1L,
        reps: Int = 0,
        dueTimestamp: Long = 0L,
        stability: Float = 0f,
        lastReviewedTimestamp: Long = 0L
    ): FlashcardEntity {
        return FlashcardEntity(
            id = id,
            deckRaw = "TCU :: CEX :: Aula 01",
            l1 = "TCU",
            l2 = "CEX",
            l3 = "Aula 01",
            frontHtml = "Pergunta $id",
            backHtml = "Resposta $id",
            contentHash = "hash_$id",
            reps = reps,
            dueTimestamp = dueTimestamp,
            stability = stability,
            lastReviewedTimestamp = lastReviewedTimestamp
        )
    }

    @Test
    fun testDueFilterExcludesNewCards() = runTest {
        val now = System.currentTimeMillis()

        // Card novo (nunca visto, reps = 0, mas com dueTimestamp <= now como inserido pelo importador Anki)
        val newCard = createCard(id = 1L, reps = 0, dueTimestamp = now - 1000)
        // Card em revisão vencido (reps > 0 e dueTimestamp <= now)
        val overdueReviewCard = createCard(id = 2L, reps = 3, dueTimestamp = now - 5000)
        // Card em revisão futuro (reps > 0 e dueTimestamp > now)
        val futureReviewCard = createCard(id = 3L, reps = 2, dueTimestamp = now + 86400000L)

        // 1. Verificação no predicado canônico Kotlin
        assertTrue("Card com reps == 0 deve ser novo", newCard.isNew())
        assertFalse("Card novo NUNCA pode ser considerado vencido", newCard.isDueForReview(now))

        assertFalse("Card com reps > 0 não é novo", overdueReviewCard.isNew())
        assertTrue("Card revisado com data passada DEVE ser considerado vencido", overdueReviewCard.isDueForReview(now))

        assertFalse("Card futuro não pode ser considerado vencido", futureReviewCard.isDueForReview(now))

        // 2. Verificação na query SQL Room (FlashcardDao.getDueFlashcardsByL1)
        db.flashcardDao().insertAll(listOf(newCard, overdueReviewCard, futureReviewCard))

        val dueFromDao = db.flashcardDao().getDueFlashcardsByL1("TCU", now).first()
        assertEquals("Apenas o card revisado com reps > 0 deve retornar da query SQL de vencidos", 1, dueFromDao.size)
        assertEquals(2L, dueFromDao[0].id)
    }

    @Test
    fun testFSRSGraduationReturnsUpdatedEntityDirectly() = runTest {
        val card = createCard(id = 10L, reps = 1, dueTimestamp = System.currentTimeMillis())
        db.flashcardDao().insertFlashcard(card)

        // Simula revisão FSRS com Good (rating = 3)
        val updated = repository.recordReview(
            card = card,
            rating = 3,
            algorithm = "FSRS",
            targetRetention = 0.90,
            reviewDurationMs = 4500L
        )

        // O retorno de recordReview deve ser a entidade exata e atualizada
        assertEquals(2, updated.reps)
        assertTrue("Estabilidade FSRS deve ser calculada e > 0", updated.stability > 0f)
        assertTrue("Dificuldade FSRS deve ser calculada e > 0", updated.difficulty > 0f)

        // Valida que o masteryLevel só vira 2 se stability >= 21.0f
        if (updated.stability < 21.0f) {
            assertEquals("Card em aprendizado não pode reportar masteryLevel = 2", 1, updated.masteryLevel)
        } else {
            assertEquals("Card com estabilidade >= 21 deve reportar masteryLevel = 2", 2, updated.masteryLevel)
        }
    }

    @Test
    fun testFuzzingJitterPreservesAnkiDayCutoff() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 14, 30, 0)
        }
        val studyTimestamp = calendar.timeInMillis
        val intervalDays = 10

        // Com applyFuzz = false: cálculo determinístico
        val deterministicDue = repository.calculateNextDueDate(intervalDays, studyTimestamp, applyFuzz = false)
        val detCal = Calendar.getInstance().apply { timeInMillis = deterministicDue }
        assertEquals("Deve estar cravado às 04:00 AM", 4, detCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, detCal.get(Calendar.MINUTE))
        assertEquals(25, detCal.get(Calendar.DAY_OF_MONTH)) // 15 + 10 = 25

        // Com applyFuzz = true: jitter em torno de ±5% (para 10 dias, delta = 1 dia -> entre 9 e 11 dias)
        val fuzzedDue = repository.calculateNextDueDate(intervalDays, studyTimestamp, applyFuzz = true)
        val fuzzCal = Calendar.getInstance().apply { timeInMillis = fuzzedDue }
        assertEquals("Mesmo com fuzz, DEVE respeitar rigorosamente o corte das 04:00 AM", 4, fuzzCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, fuzzCal.get(Calendar.MINUTE))

        val diffDays = (fuzzCal.get(Calendar.DAY_OF_MONTH) - 15)
        assertTrue("Intervalo com jitter deve oscilar suavemente entre 9 e 11 dias", diffDays in 9..11)
    }

    @Test
    fun testWeeklyFrequencyAlignedWithAnkiDayCutoff() {
        // Terça-feira, 20 de Outubro de 2026 às 01:30 da madrugada
        val tuesdayDawnCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 20, 1, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dawnTimestamp = tuesdayDawnCal.timeInMillis

        // Para o Anki (corte às 04:00 AM), 01:30 da madrugada de terça-feira ainda é a SEGUNDA-FEIRA de estudos
        val ankiDayStart = repository.getAnkiDayStart(dawnTimestamp)
        val ankiStartCal = Calendar.getInstance().apply { timeInMillis = ankiDayStart }
        assertEquals(Calendar.MONDAY, ankiStartCal.get(Calendar.DAY_OF_WEEK))
        assertEquals(19, ankiStartCal.get(Calendar.DAY_OF_MONTH)) // Segunda, dia 19
        assertEquals(4, ankiStartCal.get(Calendar.HOUR_OF_DAY))

        // Cria log de revisão nessa madrugada
        val log = com.example.data.model.ReviewLogEntity(
            id = 100L,
            cardId = 1L,
            l1 = "TCU",
            l2 = "CEX",
            l3 = "Aula 01",
            rating = 3,
            reviewDurationMs = 5000L,
            timestamp = dawnTimestamp
        )

        val card = createCard(id = 1L, reps = 1, lastReviewedTimestamp = dawnTimestamp)
        val report = repository.calculateProgressReport(
            cards = listOf(card),
            reviewLogs = listOf(log),
            now = dawnTimestamp
        )

        // No gráfico de 7 dias, a barra de SEGUNDA (primeiro elemento) deve contabilizar essa revisão
        val mondayStat = report.studyFrequency7Days.firstOrNull { it.dayLabel == "SEG" }
        val tuesdayStat = report.studyFrequency7Days.firstOrNull { it.dayLabel == "TER" }

        assertTrue("A barra de SEG deve existir", mondayStat != null)
        assertTrue("A barra de TER deve existir", tuesdayStat != null)
        assertEquals("Revisão da madrugada (01:30) DEVE ser computada na SEGUNDA de estudos", 1, mondayStat!!.reviewsCount)
        assertEquals("A TERÇA de estudos (que só inicia às 04:00 AM) deve estar com 0 revisões", 0, tuesdayStat!!.reviewsCount)
        assertTrue("A SEGUNDA de estudos deve ser considerada 'isToday' pelo horário de 01:30", mondayStat.isToday)
        assertFalse("A TERÇA civil ainda não é o 'isToday' do Anki às 01:30", tuesdayStat.isToday)
    }

    @Test
    fun testReviewLogsPreservedInRepositoryAndRestore() = runTest {
        val now = System.currentTimeMillis()
        val logs = listOf(
            com.example.data.model.ReviewLogEntity(
                id = 1L,
                cardId = 10L,
                l1 = "TCU",
                l2 = "CEX",
                l3 = "Aula 01",
                rating = 3,
                reviewDurationMs = 4000L,
                timestamp = now - 10000L
            ),
            com.example.data.model.ReviewLogEntity(
                id = 2L,
                cardId = 11L,
                l1 = "TCU",
                l2 = "AFO",
                l3 = "Receita",
                rating = 4,
                reviewDurationMs = 3000L,
                timestamp = now - 5000L
            )
        )

        repository.restoreReviewLogs(logs)
        val retrieved = repository.getAllReviewLogsSync()

        assertEquals("Ambos os logs de revisão devem ser persistidos no Room", 2, retrieved.size)
        assertEquals(10L, retrieved[0].cardId)
        assertEquals("TCU", retrieved[0].l1)
        assertEquals(3, retrieved[0].rating)
    }

    @Test
    fun testReviewStatsTracksReviewedCardsAndTotalReviewsCount() {
        val card1 = createCard(id = 1L, reps = 2)
        val card2 = createCard(id = 2L, reps = 1)
        val card3 = createCard(id = 3L, reps = 0)

        val stats = repository.calculateReviewStats(listOf(card1, card2, card3))

        assertEquals("Cards únicos revisados deve ser 2", 2, stats.totalReviewed)
        assertEquals("Total de revisões acumuladas deve ser 3", 3, stats.totalReviewsCount)
    }

    @Test
    fun testDisciplinePaletteAll17TcuAcronymsAndNoCollisionWithEstatistica() {
        // Teste de colisão clássica: 'Estatística' contém 'ti' no meio da palavra
        val estatisticaColor = com.example.ui.components.DisciplinePalette.getColorForDiscipline("Estatística")
        val tiColor = com.example.ui.components.DisciplinePalette.getColorForDiscipline("TI")

        assertEquals("#4F46E5", estatisticaColor)
        assertEquals("#0891B2", tiColor)
        assertNotEquals("Estatística NÃO pode receber a cor da disciplina TI", estatisticaColor, tiColor)

        val estatisticaIcon = com.example.ui.components.DisciplinePalette.getIconKeyForDiscipline("Estatística")
        val tiIcon = com.example.ui.components.DisciplinePalette.getIconKeyForDiscipline("TI")
        assertEquals("Timeline", estatisticaIcon)
        assertEquals("Analytics", tiIcon)

        // Teste das 17 siglas oficiais do TCU
        val tcuDisciplines = listOf(
            "CEX" to "#7C3AED",
            "AFO" to "#9333EA",
            "AUD" to "#16A34A",
            "CON" to "#2563EB",
            "ADM" to "#D97706",
            "CPB" to "#CA8A04",
            "CGE" to "#B45309",
            "TI"  to "#0891B2",
            "ECO" to "#059669",
            "POR" to "#3B82F6",
            "ING" to "#0D9488",
            "RLM" to "#6366F1",
            "EST" to "#4F46E5",
            "CIV" to "#1D4ED8",
            "CPC" to "#0E7490",
            "PEN" to "#DC2626",
            "PUB" to "#EA580C"
        )

        for ((acronym, expectedHex) in tcuDisciplines) {
            val color = com.example.ui.components.DisciplinePalette.getColorForDiscipline(acronym)
            assertEquals("Cor para a sigla $acronym deve ser $expectedHex", expectedHex, color)

            val iconKey = com.example.ui.components.DisciplinePalette.getIconKeyForDiscipline(acronym)
            assertNotEquals("Sigla $acronym não deve receber o ícone genérico School", "School", iconKey)
        }
    }

    @Test
    fun testCloudRestoreDataSupportsSettingsAndLogs() {
        val card = createCard(id = 101L, reps = 5)
        val log = com.example.data.model.ReviewLogEntity(1L, 101L, "TCU", "CEX", "Aula 01", 3, 4000L, 1729000000000L)
        val settings = """{"dailyGoal":50,"srsAlgorithm":"FSRS","customPrefs":{"goal_l2_TCU::CEX":10.0}}"""

        val restoreData = com.example.data.remote.CloudRestoreData(
            cards = listOf(card),
            logs = listOf(log),
            settingsJson = settings
        )

        assertEquals("Cards devem ser preservados", 1, restoreData.cards.size)
        assertEquals("Logs devem ser preservados", 1, restoreData.logs.size)
        assertEquals(settings, restoreData.settingsJson)
        assertTrue(restoreData.settingsJson!!.contains("goal_l2_TCU::CEX"))
    }

    @Test
    fun testStreakUsesAnkiDayStartForLateNightReviews() {
        // Terça-feira, 20 de Outubro de 2026 às 02:00 AM da madrugada
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 20, 2, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val lateNightTimestamp = cal.timeInMillis

        // Cria log de revisão nessa madrugada
        val log = com.example.data.model.ReviewLogEntity(
            id = 50L,
            cardId = 1L,
            l1 = "TCU",
            l2 = "CEX",
            l3 = "Aula 01",
            rating = 3,
            reviewDurationMs = 4000L,
            timestamp = lateNightTimestamp
        )

        val card = createCard(id = 1L, reps = 1, lastReviewedTimestamp = lateNightTimestamp)
        val report = repository.calculateProgressReport(
            cards = listOf(card),
            reviewLogs = listOf(log),
            now = lateNightTimestamp
        )

        // Às 02:00 AM, a revisão pertence ao dia Anki corrente, resultando em streak ativo >= 1
        assertTrue("Streak deve ser >= 1 mesmo para estudos na madrugada", report.currentStreakDays >= 1)
    }

    @Test
    fun testResetAllCardStatsClearsReviewLogs() = runTest {
        val now = System.currentTimeMillis()
        val card = createCard(id = 1L, reps = 3)
        db.flashcardDao().insertAll(listOf(card))

        val log = com.example.data.model.ReviewLogEntity(1L, 1L, "TCU", "CEX", "Aula 01", 3, 2000L, now)
        db.reviewLogDao().insertReviewLog(log)

        assertEquals("Deve ter 1 log antes do reset", 1, repository.getAllReviewLogsSync().size)

        repository.resetAllCardStats()

        assertEquals("Tabela de review_logs DEVE ser zerada após resetAllCardStats", 0, repository.getAllReviewLogsSync().size)
    }

    @Test
    fun testDeleteDeckCascadesToReviewLogs() = runTest {
        val now = System.currentTimeMillis()
        val logTcu = com.example.data.model.ReviewLogEntity(1L, 1L, "TCU", "CEX", "Aula 01", 3, 2000L, now)
        val logOab = com.example.data.model.ReviewLogEntity(2L, 2L, "OAB", "Ética", "Aula 01", 4, 3000L, now)
        db.reviewLogDao().insertAll(listOf(logTcu, logOab))

        assertEquals(2, repository.getAllReviewLogsSync().size)

        repository.deleteDeck("TCU")

        val remaining = repository.getAllReviewLogsSync()
        assertEquals("Deve restar apenas o log do baralho OAB", 1, remaining.size)
        assertEquals("OAB", remaining[0].l1)
    }

    @Test
    fun testAnkiCsvImporterDelimiterRobustAgainstHtmlAndCss() {
        // CSV com muitas entidades HTML e estilos CSS inline que terminam com ';'
        val csvWithHtml = """
            Deck,Tipo de Nota,Frente,Verso,Etiquetas
            "TCU :: Controle :: Art. 71","Básico","O que diz o art. 71?","<span style=""color:#2563eb; font-weight:bold;"">O TCU possui competência para:&nbsp; julgar contas;&nbsp; apreciar contas;&nbsp; fiscalizar recursos;</span>","tcu,art71"
            "TCU :: Controle :: Art. 70","Básico","Quem presta contas?","Qualquer pessoa física ou jurídica que utilize bens públicos.&nbsp;&quot;Princípio da Indisponibilidade&quot;;","tcu,art70"
        """.trimIndent()

        val result = com.example.data.importer.AnkiCsvImporter.parseAnkiCsv(csvWithHtml)
        assertEquals("Delimitador deve ser vírgula e 2 cartões devem ser parseados com sucesso", 2, result.validCards.size)
        assertEquals(0, result.errors.size)
        assertEquals("TCU", result.validCards[0].l1)
        assertEquals("Controle", result.validCards[0].l2)
        assertEquals("Art. 71", result.validCards[0].l3)
    }

    @Test
    fun testDeleteL2DisciplineCascadesToReviewLogs() = runTest {
        val now = System.currentTimeMillis()
        val logCex = com.example.data.model.ReviewLogEntity(1L, 1L, "TCU", "Controle Externo", "Tópico 1", 3, 2000L, now)
        val logAfo = com.example.data.model.ReviewLogEntity(2L, 2L, "TCU", "AFO", "Tópico 2", 4, 3000L, now)
        db.reviewLogDao().insertAll(listOf(logCex, logAfo))

        assertEquals(2, repository.getAllReviewLogsSync().size)

        repository.deleteL2Discipline("TCU", "Controle Externo")

        val remaining = repository.getAllReviewLogsSync()
        assertEquals("Apenas os logs de AFO devem restar", 1, remaining.size)
        assertEquals("AFO", remaining[0].l2)
    }

    @Test
    fun testRenameL1AndL2CascadesToReviewLogs() = runTest {
        val now = System.currentTimeMillis()
        val log = com.example.data.model.ReviewLogEntity(1L, 1L, "TCU", "CEX", "Aula 01", 3, 2000L, now)
        db.reviewLogDao().insertReviewLog(log)

        repository.renameL1Deck("TCU", "TCU-Auditor")
        var logs = repository.getAllReviewLogsSync()
        assertEquals("TCU-Auditor", logs[0].l1)

        repository.renameL2Discipline("TCU-Auditor", "CEX", "Controle-Externo")
        logs = repository.getAllReviewLogsSync()
        assertEquals("Controle-Externo", logs[0].l2)
    }

    @Test
    fun testFuzzingOnlyAppliesToSevenDaysOrMore() {
        val now = System.currentTimeMillis()
        // Para intervalos curtos (< 7 dias), calculateNextDueDate não deve sofrer jitter aleatório
        val dueShort = repository.calculateNextDueDate(intervalDays = 3, now = now, applyFuzz = true)
        val dueWithoutFuzz = repository.calculateNextDueDate(intervalDays = 3, now = now, applyFuzz = false)

        assertEquals("Intervalo de 3 dias NÃO deve sofrer fuzzing aleatório para manter paridade com botão FSRS", dueWithoutFuzz, dueShort)
    }

    @Test
    fun testFullBackupJsonExportAndImportPreservesFsrsVariablesAndLogs() = runTest {
        val now = System.currentTimeMillis()
        val card = FlashcardEntity(
            id = 55L,
            deckRaw = "TCU :: CEX :: Aula 01",
            l1 = "TCU",
            l2 = "CEX",
            l3 = "Aula 01",
            frontHtml = "<p>Quem julga as contas?</p>",
            backHtml = "<p>O TCU</p>",
            contentHash = "hash_tc_55",
            intervalDays = 21,
            reps = 6,
            lapses = 1,
            masteryLevel = 2,
            dueTimestamp = now + 86400000L * 21,
            stability = 18.75f,
            difficulty = 3.42f
        )
        db.flashcardDao().insertFlashcard(card)

        val log = com.example.data.model.ReviewLogEntity(
            id = 101L,
            cardId = 55L,
            l1 = "TCU",
            l2 = "CEX",
            l3 = "Aula 01",
            rating = 3,
            reviewDurationMs = 4500L,
            timestamp = now
        )
        db.reviewLogDao().insertReviewLog(log)

        // 1. Exporta backup completo offline JSON
        val exportedJson = repository.exportFullBackupJson()
        assertTrue("JSON de backup deve conter chave master_backup", exportedJson.contains("cards"))
        assertTrue("JSON deve conter frontHtml", exportedJson.contains("Quem julga as contas?"))

        // 2. Limpa totalmente o banco de dados Room
        repository.clearAll()
        assertEquals(0, repository.getAllFlashcardsSync().size)
        assertEquals(0, repository.getAllReviewLogsSync().size)

        // 3. Restaura backup completo a partir do JSON exportado
        val (restoredCards, restoredLogs) = repository.importFullBackupJson(exportedJson)
        assertEquals(1, restoredCards)
        assertEquals(1, restoredLogs)

        // 4. Valida preservação matemática 100% fiel de variáveis FSRS-5 e histórico
        val cardsFromDb = repository.getAllFlashcardsSync()
        val logsFromDb = repository.getAllReviewLogsSync()

        assertEquals(1, cardsFromDb.size)
        val restoredCard = cardsFromDb[0]
        assertEquals(55L, restoredCard.id)
        assertEquals(18.75f, restoredCard.stability, 0.001f)
        assertEquals(3.42f, restoredCard.difficulty, 0.001f)
        assertEquals(21, restoredCard.intervalDays)
        assertEquals(6, restoredCard.reps)
        assertEquals(1, restoredCard.lapses)
        assertEquals(2, restoredCard.masteryLevel)

        assertEquals(1, logsFromDb.size)
        assertEquals(101L, logsFromDb[0].id)
        assertEquals(4500L, logsFromDb[0].reviewDurationMs)
    }

    @Test
    fun testAtomicRoomRestoreSnapshot() = runTest {
        val card1 = createCard(id = 100L, reps = 2)
        val card2 = createCard(id = 200L, reps = 4)
        val log1 = com.example.data.model.ReviewLogEntity(501L, 100L, "TCU", "CEX", "A1", 3, 2000L, System.currentTimeMillis())

        repository.restoreDatabaseSnapshot(listOf(card1, card2), listOf(log1))

        assertEquals(2, repository.getAllFlashcardsSync().size)
        assertEquals(1, repository.getAllReviewLogsSync().size)
    }
}
