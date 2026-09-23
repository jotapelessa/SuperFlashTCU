package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DomainStats
import com.example.data.model.FlashcardEntity
import com.example.data.model.L2DisciplineSummary
import com.example.data.model.L3TopicSummary
import com.example.data.model.ReviewStats
import com.example.ui.components.DisciplinePalette
import com.example.ui.components.HtmlText
import com.example.ui.components.CardPreviewItem
import com.example.ui.components.HierarchyFilterSelector
import com.example.ui.components.ReviewMetricBox
import com.example.ui.components.DomainCategoryBadge
import com.example.ui.components.L1DisciplinesOverviewContent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateListOf
import androidx.activity.compose.BackHandler
import androidx.compose.ui.draw.rotate
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubDeckScreen(
    l1: String,
    l2Disciplines: List<L2DisciplineSummary>,
    domainStats: DomainStats,
    reviewStats: ReviewStats,
    filteredCards: List<FlashcardEntity>,
    selectedL2Filter: String?,
    selectedL3Filter: String?,
    onSelectL2Filter: (String?) -> Unit,
    onSelectL3Filter: (String?) -> Unit,
    onBack: () -> Unit,
    onStudyCards: (List<FlashcardEntity>) -> Unit,
    onDeleteL1: () -> Unit,
    onOpenAnalytics: () -> Unit = {},
    onOpenCreateCard: (defaultL1: String, defaultL2: String?) -> Unit = { _, _ -> },
    onEditCard: (FlashcardEntity) -> Unit = {},
    onOpenStudyConfig: (defaultL1: String, defaultL2: String?, defaultL3: String?) -> Unit = { _, _, _ -> },
    onMoveL3TopicsToNewL2: (topicsToMove: List<Pair<String, String>>, newL2Name: String, iconKey: String, colorHex: String) -> Unit = { _, _, _, _ -> },
    onOpenMoveL2ToL1: () -> Unit = {},
    onOpenCompareDecks: () -> Unit = {},
    onDeleteL2Discipline: (l1: String, l2: String) -> Unit = { _, _ -> },
    onUpdateL2Discipline: (l1: String, oldL2: String, newL2: String, iconKey: String, colorHex: String) -> Unit = { _, _, _, _, _ -> },
    srsAlgorithm: String = "FSRS",
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedL2Detail by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedL2Tab by rememberSaveable(selectedL2Detail) { mutableIntStateOf(0) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    val selectedL3Topics = remember { mutableStateListOf<Pair<String, String>>() }

    // Dedicated L2 Screen (Nível L2)
    if (selectedL2Detail != null) {
        val currentDiscipline = l2Disciplines.find { it.l2 == selectedL2Detail }
        if (currentDiscipline != null) {
            val l2Cards = remember(filteredCards, currentDiscipline.l2) {
                filteredCards.filter { it.l2 == currentDiscipline.l2 }
            }
            val l2ReviewStats = remember(l2Cards) {
                calculateL2ReviewStats(l2Cards)
            }
            val l2DomainStats = remember(l2Cards) {
                calculateL2DomainStats(l2Cards)
            }

            BackHandler(enabled = true) {
                selectedL2Detail = null
            }

            L2DedicatedDetailScreen(
                l1 = l1,
                discipline = currentDiscipline,
                selectedL2Tab = selectedL2Tab,
                onSelectTab = { selectedL2Tab = it },
                l2Cards = l2Cards,
                l2ReviewStats = l2ReviewStats,
                l2DomainStats = l2DomainStats,
                selectedL3Topics = selectedL3Topics,
                onBackToL1 = { selectedL2Detail = null },
                onToggleSelectL3 = { l2, l3 ->
                    val pair = Pair(l2, l3)
                    if (selectedL3Topics.contains(pair)) {
                        selectedL3Topics.remove(pair)
                    } else {
                        selectedL3Topics.add(pair)
                    }
                },
                onClearL3Selection = { selectedL3Topics.clear() },
                onOpenMoveDialog = { showMoveDialog = true },
                onStudyCards = onStudyCards,
                onOpenCreateCard = onOpenCreateCard,
                onOpenStudyConfig = onOpenStudyConfig,
                onEditCard = onEditCard,
                onEditL2Discipline = { newName, iconKey, colorHex ->
                    onUpdateL2Discipline(l1, currentDiscipline.l2, newName, iconKey, colorHex)
                    selectedL2Detail = newName
                },
                onDeleteL2 = {
                    onDeleteL2Discipline(l1, currentDiscipline.l2)
                    selectedL2Detail = null
                },
                srsAlgorithm = srsAlgorithm
            )

            if (showMoveDialog && selectedL3Topics.isNotEmpty()) {
                MoveL3ToL2Dialog(
                    selectedTopicsCount = selectedL3Topics.size,
                    existingL2Names = l2Disciplines.map { it.l2 },
                    onDismiss = { showMoveDialog = false },
                    onConfirm = { newL2Name, iconKey, colorHex ->
                        showMoveDialog = false
                        val topicsToMove = selectedL3Topics.toList()
                        selectedL3Topics.clear()
                        onMoveL3TopicsToNewL2(topicsToMove, newL2Name, iconKey, colorHex)
                    }
                )
            }
            return
        } else {
            selectedL2Detail = null
        }
    }

    // L1 Screen Overview (Nível L1)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = l1,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${l2Disciplines.size} matérias • ${filteredCards.size} cards",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_home")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar para Baralhos L1"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenCompareDecks,
                        modifier = Modifier.testTag("btn_compare_subdecks")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Comparar Baralhos L2"
                        )
                    }
                    IconButton(
                        onClick = onOpenMoveL2ToL1,
                        modifier = Modifier.testTag("btn_move_l2_bulk")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DriveFileMove,
                            contentDescription = "Mover Baralhos L2 para novo L1"
                        )
                    }
                    IconButton(
                        onClick = onOpenAnalytics,
                        modifier = Modifier.testTag("btn_subdeck_analytics")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Progresso & Estatísticas"
                        )
                    }
                    IconButton(
                        onClick = { onOpenCreateCard(l1, selectedL2Filter) },
                        modifier = Modifier.testTag("btn_add_card_subdeck")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Novo Flashcard"
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("btn_delete_l1")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir baralho L1",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Three Tabs: Disciplinas (L2), Revisão Geral, Domínio Geral
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Disciplinas (L2)", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_structure")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Revisão Geral", fontWeight = FontWeight.SemiBold)
                            if (reviewStats.dueNow > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${reviewStats.dueNow}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_review")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Domínio Geral", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_domain")
                )
            }

            when (selectedTab) {
                0 -> {
                    // Item 3.1: Visualização limpa com Atalhos L2 para as áreas dedicadas
                    L1DisciplinesOverviewContent(
                        l1 = l1,
                        l2Disciplines = l2Disciplines,
                        totalCardsCount = filteredCards.size,
                        domainStats = domainStats,
                        onSelectL2 = { selectedL2Detail = it },
                        onStudyAllL1 = { onStudyCards(filteredCards) }
                    )
                }
                1 -> {
                    // Review Tab: filtered and aggregated review stats & review session trigger
                    ReviewTabContent(
                        reviewStats = reviewStats,
                        filteredCards = filteredCards,
                        l2Disciplines = l2Disciplines,
                        selectedL2Filter = selectedL2Filter,
                        selectedL3Filter = selectedL3Filter,
                        onSelectL2Filter = onSelectL2Filter,
                        onSelectL3Filter = onSelectL3Filter,
                        onStartReviewSession = {
                            val dueCards = filteredCards.filter { it.dueTimestamp <= System.currentTimeMillis() }
                            onStudyCards(if (dueCards.isNotEmpty()) dueCards else filteredCards)
                        },
                        onOpenStudyConfig = { onOpenStudyConfig(l1, selectedL2Filter, selectedL3Filter) },
                        onEditCard = onEditCard,
                        srsAlgorithm = srsAlgorithm
                    )
                }
                2 -> {
                    // Domain Tab: filtered and aggregated domain mastery stats
                    DomainTabContent(
                        domainStats = domainStats,
                        filteredCards = filteredCards,
                        l2Disciplines = l2Disciplines,
                        selectedL2Filter = selectedL2Filter,
                        selectedL3Filter = selectedL3Filter,
                        onSelectL2Filter = onSelectL2Filter,
                        onSelectL3Filter = onSelectL3Filter,
                        onStudyNewCards = {
                            val newCards = filteredCards.filter { it.reps == 0 }
                            onStudyCards(if (newCards.isNotEmpty()) newCards else filteredCards)
                        },
                        onEditCard = onEditCard
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Baralho L1?") },
            text = { Text("Esta ação removerá '$l1' e todos os seus sub-decks L2/L3 e flashcards associados.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteL1()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showMoveDialog && selectedL3Topics.isNotEmpty()) {
        MoveL3ToL2Dialog(
            selectedTopicsCount = selectedL3Topics.size,
            existingL2Names = l2Disciplines.map { it.l2 },
            onDismiss = { showMoveDialog = false },
            onConfirm = { newL2Name, iconKey, colorHex ->
                showMoveDialog = false
                val topicsToMove = selectedL3Topics.toList()
                selectedL3Topics.clear()
                onMoveL3TopicsToNewL2(topicsToMove, newL2Name, iconKey, colorHex)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FUNÇÕES AUXILIARES DE CÁLCULO DE ESTATÍSTICAS DEDICADAS DO L2
// ─────────────────────────────────────────────────────────────────────────────

private fun calculateL2DomainStats(cards: List<FlashcardEntity>): DomainStats {
    if (cards.isEmpty()) return DomainStats()
    val total = cards.size
    val mastered = cards.count { it.masteryLevel == 2 }
    val learning = cards.count { it.masteryLevel == 1 }
    val newCards = cards.count { it.masteryLevel == 0 && it.reps == 0 }
    val percentage = ((mastered * 1.0f + learning * 0.4f) / total) * 100f
    return DomainStats(
        total = total,
        mastered = mastered,
        learning = learning,
        newCards = newCards,
        masteryPercentage = percentage
    )
}

private fun calculateL2ReviewStats(cards: List<FlashcardEntity>): ReviewStats {
    if (cards.isEmpty()) return ReviewStats()
    val now = System.currentTimeMillis()
    val oneDay = 24L * 3600L * 1000L
    val dueNow = cards.count { it.reps > 0 && it.dueTimestamp <= now }
    val dueToday = cards.count { it.reps > 0 && it.dueTimestamp <= (now + oneDay) }
    val reviewed = cards.filter { it.reps > 0 }
    val totalReviews = reviewed.sumOf { it.reps }
    val totalLapses = reviewed.sumOf { it.lapses }
    val retention = if (totalReviews > 0) {
        kotlin.math.max(0f, (1f - (totalLapses.toFloat() / totalReviews)) * 100f)
    } else {
        100f
    }
    val avgInterval = if (reviewed.isNotEmpty()) {
        reviewed.map { it.intervalDays }.average().toFloat()
    } else 0f

    return ReviewStats(
        dueNow = dueNow,
        dueToday = dueToday,
        totalReviewed = totalReviews,
        retentionRate = retention,
        averageIntervalDays = avgInterval
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTES DA VISÃO GERAL DO L1 (ATALHOS L2)
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// TELAS DE ABAS: REVISÃO GERAL E DOMÍNIO GERAL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun ReviewTabContent(
    reviewStats: ReviewStats,
    filteredCards: List<FlashcardEntity>,
    l2Disciplines: List<L2DisciplineSummary>,
    selectedL2Filter: String?,
    selectedL3Filter: String?,
    onSelectL2Filter: (String?) -> Unit,
    onSelectL3Filter: (String?) -> Unit,
    onStartReviewSession: () -> Unit,
    onOpenStudyConfig: () -> Unit = {},
    onEditCard: (FlashcardEntity) -> Unit = {},
    srsAlgorithm: String = "FSRS"
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hierarchy Filter Chips
        item {
            HierarchyFilterSelector(
                l2Disciplines = l2Disciplines,
                selectedL2 = selectedL2Filter,
                selectedL3 = selectedL3Filter,
                onSelectL2 = onSelectL2Filter,
                onSelectL3 = onSelectL3Filter
            )
        }

        // Review Metrics Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Progresso de Revisão Espaçada (${if (srsAlgorithm.equals("FSRS", ignoreCase = true)) "FSRS-5" else "SM-2"})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReviewMetricBox(
                            title = "Pendentes Agora",
                            value = "${reviewStats.dueNow}",
                            color = if (reviewStats.dueNow > 0) MaterialTheme.colorScheme.error else Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ReviewMetricBox(
                            title = "Para Hoje",
                            value = "${reviewStats.dueToday}",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ReviewMetricBox(
                            title = "Taxa Retenção",
                            value = "${reviewStats.retentionRate.toInt()}%",
                            color = Color(0xFF059669),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onStartReviewSession,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_start_review"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (reviewStats.dueNow > 0) "Revisar (${reviewStats.dueNow})" else "Revisar Todos (${filteredCards.size})",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenStudyConfig,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("btn_custom_study_config"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Configurar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // List of Cards in this Hierarchy
        item {
            Text(
                text = "Cards no Nível Selecionado (${filteredCards.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(filteredCards, key = { it.id }) { card ->
            CardPreviewItem(card = card, onEdit = { onEditCard(card) })
        }
    }
}

@Composable
internal fun DomainTabContent(
    domainStats: DomainStats,
    filteredCards: List<FlashcardEntity>,
    l2Disciplines: List<L2DisciplineSummary>,
    selectedL2Filter: String?,
    selectedL3Filter: String?,
    onSelectL2Filter: (String?) -> Unit,
    onSelectL3Filter: (String?) -> Unit,
    onStudyNewCards: () -> Unit,
    onEditCard: (FlashcardEntity) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hierarchy Filter Chips
        item {
            HierarchyFilterSelector(
                l2Disciplines = l2Disciplines,
                selectedL2 = selectedL2Filter,
                selectedL3 = selectedL3Filter,
                onSelectL2 = onSelectL2Filter,
                onSelectL3 = onSelectL3Filter
            )
        }

        // Domain Mastery Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Nível de Domínio da Matéria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${domainStats.total} cards analisados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (domainStats.masteryPercentage >= 70f) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${domainStats.masteryPercentage.toInt()}%",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (domainStats.masteryPercentage >= 70f) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { (domainStats.masteryPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF10B981),
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Breakdown Rows: Dominados, Em Aprendizado, Não Iniciados
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DomainCategoryBadge(
                            label = "Dominados",
                            count = domainStats.mastered,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DomainCategoryBadge(
                            label = "Aprendendo",
                            count = domainStats.learning,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DomainCategoryBadge(
                            label = "Novos",
                            count = domainStats.newCards,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (domainStats.newCards > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = onStudyNewCards,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_study_new"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aprender Novos Cards (${domainStats.newCards})")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Detalhamento dos Flashcards",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(filteredCards, key = { it.id }) { card ->
            CardPreviewItem(card = card, onEdit = { onEditCard(card) })
        }
    }
}
