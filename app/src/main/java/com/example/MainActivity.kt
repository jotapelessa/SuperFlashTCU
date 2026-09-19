package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CardEditorDialog
import com.example.ui.screens.DeckComparisonDialog
import com.example.ui.screens.ExportCsvDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImportDialog
import com.example.ui.screens.MoveL2ToL1Dialog
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudyConfigDialog
import com.example.ui.screens.StudyScreen
import com.example.ui.screens.SubDeckScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DeckViewModel

import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ui.screens.AiTutorScreen
import com.example.ui.components.AiAnalysisBottomSheet

class MainActivity : ComponentActivity() {

    private val viewModel: DeckViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()

            MyApplicationTheme(
                themeMode = themeMode,
                accentColor = accentColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnkiAppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DECKS("Baralhos", Icons.Filled.School, Icons.Outlined.School, "tab_decks"),
    TUTOR_AI("Tutor IA", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_tutor_ai"),
    STATISTICS("Estatísticas", Icons.Filled.BarChart, Icons.Outlined.BarChart, "tab_statistics"),
    SETTINGS("Configurações", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

enum class AppDestination {
    HOME,          // Exclusively L1 Overview Cards
    SUB_DECKS,     // Dedicated screen for L2 & L3 with list-based design & Dominio/Revisao
    STUDY_CONFIG,  // Fullscreen configuration screen for study sessions
    STUDY,         // Interactive study session with live status tracking
    ANALYTICS      // Progress visualization: study frequency & domain mastery charts
}

@Composable
fun AnkiAppNavigation(viewModel: DeckViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.DECKS) }
    var subDestination by rememberSaveable { mutableStateOf<AppDestination?>(null) }

    var showImportDialog by remember { mutableStateOf(false) }
    var studyConfigInitialL1 by remember { mutableStateOf<String?>(null) }
    var studyConfigInitialL2 by remember { mutableStateOf<String?>(null) }
    var studyConfigInitialL3 by remember { mutableStateOf<String?>(null) }
    var showAiAnalysisSheet by remember { mutableStateOf(false) }
    var showMoveL2ToL1Dialog by remember { mutableStateOf(false) }
    var showCompareDecksDialog by remember { mutableStateOf(false) }
    var showExportCsvDialog by remember { mutableStateOf(false) }

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()

    val supabaseUrl by viewModel.supabaseUrl.collectAsStateWithLifecycle()
    val supabaseKey by viewModel.supabaseKey.collectAsStateWithLifecycle()
    val supabaseAutoSync by viewModel.supabaseAutoSync.collectAsStateWithLifecycle()
    val supabaseSyncState by viewModel.supabaseSyncState.collectAsStateWithLifecycle()
    val supabaseIsLoading by viewModel.supabaseIsLoading.collectAsStateWithLifecycle()

    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()
    val aiAnalysisResult by viewModel.aiAnalysisResult.collectAsStateWithLifecycle()
    val aiErrorMessage by viewModel.aiErrorMessage.collectAsStateWithLifecycle()

    val l1Decks by viewModel.l1Decks.collectAsStateWithLifecycle()
    val selectedL1 by viewModel.selectedL1.collectAsStateWithLifecycle()
    val l2Disciplines by viewModel.l2Disciplines.collectAsStateWithLifecycle()
    val domainStats by viewModel.domainStats.collectAsStateWithLifecycle()
    val reviewStats by viewModel.reviewStats.collectAsStateWithLifecycle()
    val filteredCards by viewModel.filteredCards.collectAsStateWithLifecycle()
    val selectedL2Filter by viewModel.selectedL2Filter.collectAsStateWithLifecycle()
    val selectedL3Filter by viewModel.selectedL3Filter.collectAsStateWithLifecycle()

    val studyCards by viewModel.studyCards.collectAsStateWithLifecycle()
    val currentCardIndex by viewModel.currentCardIndex.collectAsStateWithLifecycle()
    val isAnswerRevealed by viewModel.isAnswerRevealed.collectAsStateWithLifecycle()
    val studyCompleted by viewModel.studyCompleted.collectAsStateWithLifecycle()
    val sessionStats by viewModel.sessionStats.collectAsStateWithLifecycle()

    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val importResult by viewModel.importResult.collectAsStateWithLifecycle()

    val progressReport by viewModel.progressReport.collectAsStateWithLifecycle()
    val isCardEditorOpen by viewModel.isCardEditorOpen.collectAsStateWithLifecycle()
    val cardBeingEdited by viewModel.cardBeingEdited.collectAsStateWithLifecycle()
    val allFolders by viewModel.allFolders.collectAsStateWithLifecycle()
    val allCards by viewModel.allCards.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val dailyStudyGoal by viewModel.dailyStudyGoal.collectAsStateWithLifecycle()
    val todayReviewedCount by viewModel.todayReviewedCount.collectAsStateWithLifecycle()
    val smartShuffleEnabled by viewModel.smartShuffleEnabled.collectAsStateWithLifecycle()
    val sessionTimerConfig by viewModel.sessionTimerConfig.collectAsStateWithLifecycle()
    val srsAlgorithm by viewModel.srsAlgorithm.collectAsStateWithLifecycle()
    val targetRetention by viewModel.targetRetention.collectAsStateWithLifecycle()
    val criticalBottlenecks by viewModel.criticalBottlenecks.collectAsStateWithLifecycle()
    val geminiApiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val geminiModelVersion by viewModel.geminiModelVersion.collectAsStateWithLifecycle()
    val geminiTelemetry by viewModel.geminiTelemetry.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (subDestination != AppDestination.STUDY && subDestination != AppDestination.STUDY_CONFIG) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_navigation")
                ) {
                    AppTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab && subDestination == null
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                selectedTab = tab
                                subDestination = null
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when {
                subDestination == AppDestination.STUDY_CONFIG -> {
                    StudyConfigDialog(
                        allCards = allCards,
                        existingFolders = allFolders,
                        initialL1 = studyConfigInitialL1,
                        initialL2 = studyConfigInitialL2,
                        initialL3 = studyConfigInitialL3,
                        availableTags = allTags,
                        initialTimerConfig = sessionTimerConfig,
                        onStartSession = { selectedCards, timerConfig ->
                            if (selectedCards.isNotEmpty()) {
                                viewModel.setSessionTimerConfig(timerConfig)
                                viewModel.startStudySession(selectedCards, timerConfig)
                                subDestination = AppDestination.STUDY
                            }
                        },
                        onDismiss = {
                            subDestination = if (selectedL1 != null) AppDestination.SUB_DECKS else null
                        }
                    )
                }

                subDestination == AppDestination.STUDY -> {
                    StudyScreen(
                        cards = studyCards,
                        currentIndex = currentCardIndex,
                        isAnswerRevealed = isAnswerRevealed,
                        isCompleted = studyCompleted,
                        sessionStats = sessionStats,
                        timerConfig = sessionTimerConfig,
                        srsAlgorithm = srsAlgorithm,
                        targetRetention = targetRetention,
                        onRevealAnswer = { viewModel.revealAnswer() },
                        onRateCard = { rating, timeSpent -> viewModel.rateCurrentCard(rating, timeSpent) },
                        onEditCard = { card ->
                            viewModel.openEditCard(card)
                        },
                        onFinish = {
                            viewModel.finishStudySession()
                            subDestination = null
                        }
                    )
                }

                subDestination == AppDestination.SUB_DECKS -> {
                    selectedL1?.let { l1 ->
                        SubDeckScreen(
                            l1 = l1,
                            l2Disciplines = l2Disciplines,
                            domainStats = domainStats,
                            reviewStats = reviewStats,
                            filteredCards = filteredCards,
                            selectedL2Filter = selectedL2Filter,
                            selectedL3Filter = selectedL3Filter,
                            onSelectL2Filter = { viewModel.setL2Filter(it) },
                            onSelectL3Filter = { viewModel.setL3Filter(it) },
                            onBack = {
                                viewModel.selectL1(null)
                                subDestination = null
                            },
                            onStudyCards = { cards ->
                                if (cards.isNotEmpty()) {
                                    viewModel.startStudySession(cards, sessionTimerConfig)
                                    subDestination = AppDestination.STUDY
                                }
                            },
                            onDeleteL1 = {
                                viewModel.deleteDeck(l1)
                                subDestination = null
                            },
                            onOpenAnalytics = {
                                viewModel.loadAnalytics(l1)
                                selectedTab = AppTab.STATISTICS
                                subDestination = null
                            },
                            onOpenCreateCard = { defaultL1, defaultL2 ->
                                viewModel.openCreateCard(defaultL1 = defaultL1, defaultL2 = defaultL2)
                            },
                            onEditCard = { card ->
                                viewModel.openEditCard(card)
                            },
                            onOpenStudyConfig = { targetL1, targetL2, targetL3 ->
                                studyConfigInitialL1 = targetL1
                                studyConfigInitialL2 = targetL2
                                studyConfigInitialL3 = targetL3
                                subDestination = AppDestination.STUDY_CONFIG
                            },
                            onMoveL3TopicsToNewL2 = { topicsToMove, newL2Name, iconKey, colorHex ->
                                viewModel.moveL3TopicsToNewL2(
                                    l1 = l1,
                                    topicsToMove = topicsToMove,
                                    newL2Name = newL2Name,
                                    iconKey = iconKey,
                                    colorHex = colorHex
                                )
                            },
                            onOpenMoveL2ToL1 = { showMoveL2ToL1Dialog = true },
                            onOpenCompareDecks = { showCompareDecksDialog = true },
                            onDeleteL2Discipline = { l1Target, l2Target -> viewModel.deleteL2Discipline(l1Target, l2Target) },
                            onUpdateL2Discipline = { l1Target, oldL2, newL2, iconKey, colorHex ->
                                viewModel.updateL2Discipline(l1Target, oldL2, newL2, iconKey, colorHex)
                            },
                            srsAlgorithm = srsAlgorithm
                        )
                    } ?: run {
                        subDestination = null
                    }
                }

                selectedTab == AppTab.DECKS -> {
                    HomeScreen(
                        l1Decks = l1Decks,
                        todayReviewed = todayReviewedCount,
                        dailyGoal = dailyStudyGoal,
                        allTags = allTags,
                        onSetDailyGoal = { viewModel.setDailyStudyGoal(it) },
                        onStudyByTag = { tag ->
                            viewModel.filterByTag(tag)
                            val tagged = allCards.filter { com.example.data.repository.DeckRepository.hasTag(it, tag) }
                            if (tagged.isNotEmpty()) {
                                viewModel.startStudySession(tagged, sessionTimerConfig)
                                subDestination = AppDestination.STUDY
                            }
                        },
                        onSelectL1 = { l1 ->
                            viewModel.selectL1(l1)
                            subDestination = AppDestination.SUB_DECKS
                        },
                        onQuickStudyL1 = { l1 ->
                            viewModel.selectL1(l1)
                            val cardsToStudy = allCards.filter { it.l1 == l1 }
                            if (cardsToStudy.isNotEmpty()) {
                                val dueCards = cardsToStudy.filter { it.dueTimestamp <= System.currentTimeMillis() }
                                val targetCards = if (dueCards.isNotEmpty()) dueCards else cardsToStudy
                                viewModel.startStudySession(targetCards, sessionTimerConfig)
                                subDestination = AppDestination.STUDY
                            }
                        },
                        onOpenImport = { showImportDialog = true },
                        onResetData = { viewModel.resetToDefault() },
                        onOpenAnalytics = {
                            viewModel.loadAnalytics(null)
                            selectedTab = AppTab.STATISTICS
                        },
                        onOpenCreateCard = {
                            viewModel.openCreateCard()
                        },
                        onOpenMoveL2ToL1 = { showMoveL2ToL1Dialog = true },
                        onOpenCompareDecks = { showCompareDecksDialog = true },
                        onUpdateL1Customization = { oldL1, newName, colorHex, courseName, coverUrl, isAiEnabled ->
                            viewModel.updateL1Customization(oldL1, newName, colorHex, courseName, coverUrl, isAiEnabled)
                        }
                    )
                }

                selectedTab == AppTab.TUTOR_AI -> {
                    AiTutorScreen(
                        isLoading = aiLoading,
                        analysisResult = aiAnalysisResult,
                        errorMessage = aiErrorMessage,
                        activeModelVersion = geminiModelVersion,
                        l1Decks = l1Decks,
                        allCards = allCards,
                        progressReport = progressReport,
                        criticalBottlenecks = criticalBottlenecks,
                        onAnalyze = { customQuestion ->
                            viewModel.runAiAnalysis(customQuestion)
                        },
                        onClearAnalysis = {
                            viewModel.clearAiAnalysis()
                        },
                        onToggleL1AiAnalysis = { l1, enabled ->
                            viewModel.toggleL1AiAnalysis(l1, enabled)
                        },
                        onStudyCriticalCards = { cards ->
                            if (cards.isNotEmpty()) {
                                viewModel.startStudySession(cards, sessionTimerConfig)
                                subDestination = AppDestination.STUDY
                            }
                        }
                    )
                }

                selectedTab == AppTab.STATISTICS -> {
                    AnalyticsScreen(
                        report = progressReport,
                        selectedL1Title = selectedL1,
                        onBack = {
                            selectedTab = AppTab.DECKS
                        },
                        onOpenAiAnalysis = {
                            selectedTab = AppTab.TUTOR_AI
                            if (aiAnalysisResult == null) {
                                viewModel.runAiAnalysis()
                            }
                        }
                    )
                }

                selectedTab == AppTab.SETTINGS -> {
                    SettingsScreen(
                        dailyGoal = dailyStudyGoal,
                        smartShuffleEnabled = smartShuffleEnabled,
                        timerConfig = sessionTimerConfig,
                        totalCardsCount = allCards.size,
                        totalDecksCount = l1Decks.size,
                        themeMode = themeMode,
                        accentColor = accentColor,
                        srsAlgorithm = srsAlgorithm,
                        targetRetention = targetRetention,
                        onSetSrsAlgorithm = { viewModel.setSrsAlgorithm(it) },
                        onSetTargetRetention = { viewModel.setTargetRetention(it) },
                        geminiApiKey = geminiApiKey,
                        geminiModelVersion = geminiModelVersion,
                        supabaseUrl = supabaseUrl,
                        supabaseKey = supabaseKey,
                        supabaseAutoSync = supabaseAutoSync,
                        supabaseSyncState = supabaseSyncState,
                        supabaseIsLoading = supabaseIsLoading,
                        onSetDailyGoal = { viewModel.setDailyStudyGoal(it) },
                        onSetSmartShuffle = { viewModel.setSmartShuffle(it) },
                        onSetTimerConfig = { viewModel.setSessionTimerConfig(it) },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetAccentColor = { viewModel.setAccentColor(it) },
                        onSetGeminiApiKey = { viewModel.setGeminiApiKey(it) },
                        onSetGeminiModelVersion = { viewModel.setGeminiModelVersion(it) },
                        geminiTelemetry = geminiTelemetry,
                        onTestGeminiApiKey = { viewModel.testGeminiApiKey() },
                        onResetGeminiTelemetry = { viewModel.resetGeminiDailyStats() },
                        onSetSupabaseUrl = { viewModel.setSupabaseUrl(it) },
                        onSetSupabaseKey = { viewModel.setSupabaseKey(it) },
                        onSetSupabaseAutoSync = { viewModel.setSupabaseAutoSync(it) },
                        onTestSupabaseConnection = { viewModel.testSupabaseConnection() },
                        onSyncToSupabase = { viewModel.syncToSupabase() },
                        onDownloadFromSupabase = { viewModel.downloadFromSupabase() },
                        onOpenImportCsv = { showImportDialog = true },
                        onOpenExportCsv = { showExportCsvDialog = true },
                        onResetData = { viewModel.resetToDefault() },
                        onResetStudyStats = { viewModel.resetAllStudyStats() },
                        onDeleteAllDecks = { viewModel.deleteAllDecksAndCards() },
                        onClearAllDataZero = { viewModel.clearAllDataAndResetZero() }
                    )
                }
            }
        }
    }

    if (showMoveL2ToL1Dialog) {
        MoveL2ToL1Dialog(
            allCards = allCards,
            l1Decks = l1Decks,
            initialSelectedL1 = selectedL1,
            onConfirmMove = { pairs, targetL1 ->
                viewModel.moveMultipleL2ToL1(pairs, targetL1)
                showMoveL2ToL1Dialog = false
            },
            onDismiss = { showMoveL2ToL1Dialog = false }
        )
    }

    if (showCompareDecksDialog) {
        DeckComparisonDialog(
            allCards = allCards,
            initialL1 = selectedL1,
            initialL2 = selectedL2Filter,
            onDeleteL2Discipline = { l1Target, l2Target -> viewModel.deleteL2Discipline(l1Target, l2Target) },
            onDismiss = { showCompareDecksDialog = false }
        )
    }

    if (showExportCsvDialog) {
        ExportCsvDialog(
            l1Decks = l1Decks,
            initialL1 = selectedL1,
            onGenerateCsv = { l1Target, callback ->
                viewModel.exportL1ToCsv(l1Target, callback)
            },
            onDismiss = { showExportCsvDialog = false }
        )
    }

    if (showAiAnalysisSheet) {
        AiAnalysisBottomSheet(
            isLoading = aiLoading,
            analysisResult = aiAnalysisResult,
            errorMessage = aiErrorMessage,
            onAnalyze = { question ->
                viewModel.runAiAnalysis(question)
            },
            onDismiss = {
                showAiAnalysisSheet = false
            }
        )
    }

    if (showImportDialog) {
        ImportDialog(
            isImporting = isImporting,
            importResult = importResult,
            onImportCsv = { csv -> viewModel.importCsv(csv) },
            onDismissResult = { viewModel.dismissImportResult() },
            onClose = { showImportDialog = false }
        )
    }

    if (isCardEditorOpen) {
        cardBeingEdited?.let { card ->
            CardEditorDialog(
                card = card,
                existingFolders = allFolders,
                onSave = { updatedCard ->
                    viewModel.saveCard(updatedCard)
                },
                onDelete = { cardToDelete ->
                    viewModel.deleteCard(cardToDelete)
                },
                onDismiss = {
                    viewModel.closeCardEditor()
                }
            )
        }
    }
}

