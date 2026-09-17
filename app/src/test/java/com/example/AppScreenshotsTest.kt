package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.DayStudyStat
import com.example.data.model.DomainMasteryStat
import com.example.data.model.DomainStats
import com.example.data.model.FlashcardEntity
import com.example.data.model.L1DeckSummary
import com.example.data.model.L2DisciplineSummary
import com.example.data.model.L3TopicSummary
import com.example.data.model.ReviewStats
import com.example.data.model.SessionLiveStats
import com.example.data.model.StudyProgressReport
import com.example.data.model.StudyTimerConfig
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StudyScreen
import com.example.ui.screens.SubDeckScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class AppScreenshotsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleL1Decks = listOf(
    L1DeckSummary(
      l1 = "Auditor TCU / Controle Externo",
      totalCards = 142,
      dueCards = 12,
      masteredCards = 68,
      learningCards = 44,
      newCards = 30,
      l2Count = 3,
      l3Count = 9,
      disciplineColors = listOf("#2563EB", "#7C3AED", "#059669"),
      courseName = "Concurso TCU - Auditor Federal de Controle Externo"
    ),
    L1DeckSummary(
      l1 = "Direito Administrativo Essencial",
      totalCards = 86,
      dueCards = 5,
      masteredCards = 42,
      learningCards = 28,
      newCards = 16,
      l2Count = 2,
      l3Count = 6,
      disciplineColors = listOf("#D97706", "#DC2626"),
      courseName = "Carreiras Fiscais & Controle"
    ),
    L1DeckSummary(
      l1 = "Tecnologia da Informação & Governança",
      totalCards = 64,
      dueCards = 0,
      masteredCards = 35,
      learningCards = 20,
      newCards = 9,
      l2Count = 2,
      l3Count = 5,
      disciplineColors = listOf("#0284C7", "#4F46E5"),
      courseName = "COBIT 2019, ITIL 4 & LGPD"
    )
  )

  private val sampleCards = listOf(
    FlashcardEntity(
      id = 1L,
      deckRaw = "Auditor TCU / Controle Externo::Direito Constitucional::Controle de Constitucionalidade",
      l1 = "Auditor TCU / Controle Externo",
      l2 = "Direito Constitucional",
      l3 = "Controle de Constitucionalidade",
      frontHtml = "<b>(CESPE / TCU - Auditor)</b><br><br>Qual é a natureza jurídica do controle exercido pelo <u>Tribunal de Contas da União</u> sobre a legalidade dos atos de concessão inicial de aposentadorias, reformas e pensões?",
      backHtml = "<b>Natureza de Ato Complexo</b>.<br><br>A aposentadoria somente se aperfeiçoa com o registro no TCU (<b>Súmula Vinculante nº 3</b>).<br><br><small>O prazo decadencial de 5 anos conta-se a partir da chegada do processo ao Tribunal (Tema 445/STF).</small>",
      contentHash = "hash1",
      reps = 4,
      intervalDays = 6,
      masteryLevel = 1,
      dueTimestamp = System.currentTimeMillis() - 1000L,
      tags = "TCU STF Constitucional Controle"
    )
  )

  @Test
  fun capture_01_home_screen() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          HomeScreen(
            l1Decks = sampleL1Decks,
            todayReviewed = 15,
            dailyGoal = 20,
            allTags = listOf("Controle Externo", "Direito Constitucional", "Jurisprudência", "TI", "Licitações"),
            onSelectL1 = {},
            onQuickStudyL1 = {},
            onOpenImport = {},
            onResetData = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/01_home_screen.png")
  }

  @Test
  fun capture_02_study_screen_question() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          StudyScreen(
            cards = sampleCards,
            currentIndex = 0,
            isAnswerRevealed = false,
            isCompleted = false,
            sessionStats = SessionLiveStats(
              totalSessionCards = 10,
              completedCards = 3,
              againCount = 0,
              hardCount = 1,
              goodCount = 2,
              easyCount = 0
            ),
            timerConfig = StudyTimerConfig(),
            onRevealAnswer = {},
            onRateCard = { _, _ -> },
            onEditCard = {},
            onFinish = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/02_study_question.png")
  }

  @Test
  fun capture_03_study_screen_answer() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          StudyScreen(
            cards = sampleCards,
            currentIndex = 0,
            isAnswerRevealed = true,
            isCompleted = false,
            sessionStats = SessionLiveStats(
              totalSessionCards = 10,
              completedCards = 3,
              againCount = 0,
              hardCount = 1,
              goodCount = 2,
              easyCount = 0
            ),
            timerConfig = StudyTimerConfig(),
            onRevealAnswer = {},
            onRateCard = { _, _ -> },
            onEditCard = {},
            onFinish = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/03_study_answer.png")
  }

  @Test
  fun capture_04_subdecks_screen() {
    val l2Disciplines = listOf(
      L2DisciplineSummary(
        l1 = "Auditor TCU / Controle Externo",
        l2 = "Direito Constitucional",
        totalCards = 54,
        dueCards = 5,
        masteredCards = 28,
        learningCards = 18,
        newCards = 8,
        l3Count = 3,
        colorHex = "#2563EB",
        iconKey = "School",
        topics = listOf(
          L3TopicSummary(
            l1 = "Auditor TCU / Controle Externo",
            l2 = "Direito Constitucional",
            l3 = "Controle de Constitucionalidade",
            totalCards = 24,
            dueCards = 3,
            masteredCards = 14,
            learningCards = 7,
            newCards = 3,
            colorHex = "#2563EB"
          ),
          L3TopicSummary(
            l1 = "Auditor TCU / Controle Externo",
            l2 = "Direito Constitucional",
            l3 = "Organização dos Poderes & Funções Essenciais",
            totalCards = 30,
            dueCards = 2,
            masteredCards = 14,
            learningCards = 11,
            newCards = 5,
            colorHex = "#2563EB"
          )
        )
      ),
      L2DisciplineSummary(
        l1 = "Auditor TCU / Controle Externo",
        l2 = "Auditoria Governamental & Normas TCU",
        totalCards = 48,
        dueCards = 4,
        masteredCards = 22,
        learningCards = 16,
        newCards = 10,
        l3Count = 2,
        colorHex = "#059669",
        iconKey = "CheckCircle"
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          SubDeckScreen(
            l1 = "Auditor TCU / Controle Externo",
            l2Disciplines = l2Disciplines,
            domainStats = DomainStats(total = 142, mastered = 68, learning = 44, newCards = 30, masteryPercentage = 63.4f),
            reviewStats = ReviewStats(dueNow = 12, dueToday = 18, totalReviewed = 124, retentionRate = 88.5f, averageIntervalDays = 14.2f),
            filteredCards = sampleCards,
            selectedL2Filter = null,
            selectedL3Filter = null,
            onSelectL2Filter = {},
            onSelectL3Filter = {},
            onBack = {},
            onStudyCards = {},
            onDeleteL1 = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/04_subdecks_screen.png")
  }

  @Test
  fun capture_05_analytics_screen() {
    val report = StudyProgressReport(
      totalCards = 292,
      totalReviews = 418,
      currentStreakDays = 7,
      overallMasteryPercentage = 67.8f,
      dueNowCount = 17,
      learningCount = 92,
      masteredCount = 145,
      newCount = 55,
      studyFrequency7Days = listOf(
        DayStudyStat("Seg", "11/09", 1726000000000L, reviewsCount = 25, uniqueCardsCount = 20),
        DayStudyStat("Ter", "12/09", 1726086400000L, reviewsCount = 34, uniqueCardsCount = 28),
        DayStudyStat("Qua", "13/09", 1726172800000L, reviewsCount = 42, uniqueCardsCount = 35),
        DayStudyStat("Qui", "14/09", 1726259200000L, reviewsCount = 18, uniqueCardsCount = 15),
        DayStudyStat("Sex", "15/09", 1726345600000L, reviewsCount = 50, uniqueCardsCount = 40),
        DayStudyStat("Sáb", "16/09", 1726432000000L, reviewsCount = 30, uniqueCardsCount = 26),
        DayStudyStat("Dom", "17/09", 1726518400000L, reviewsCount = 28, uniqueCardsCount = 22, isToday = true)
      ),
      domainMasteryList = listOf(
        DomainMasteryStat("Direito Constitucional", "#2563EB", totalCards = 54, masteredCards = 28, learningCards = 18, newCards = 8, masteryPercentage = 65.2f),
        DomainMasteryStat("Auditoria Governamental", "#059669", totalCards = 48, masteredCards = 22, learningCards = 16, newCards = 10, masteryPercentage = 59.1f),
        DomainMasteryStat("Direito Administrativo", "#D97706", totalCards = 86, masteredCards = 42, learningCards = 28, newCards = 16, masteryPercentage = 61.9f)
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          AnalyticsScreen(
            report = report,
            selectedL1Title = null,
            onBack = {},
            onOpenAiAnalysis = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/05_analytics_screen.png")
  }
}
