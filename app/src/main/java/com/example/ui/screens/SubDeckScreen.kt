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
    onOpenStudyConfig: (defaultL1: String) -> Unit = {},
    onMoveL3TopicsToNewL2: (topicsToMove: List<Pair<String, String>>, newL2Name: String, iconKey: String, colorHex: String) -> Unit = { _, _, _, _ -> },
    onOpenMoveL2ToL1: () -> Unit = {},
    onOpenCompareDecks: () -> Unit = {},
    onDeleteL2Discipline: (l1: String, l2: String) -> Unit = { _, _ -> },
    onUpdateL2Discipline: (l1: String, oldL2: String, newL2: String, iconKey: String, colorHex: String) -> Unit = { _, _, _, _, _ -> },
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
                }
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
                        onOpenStudyConfig = { onOpenStudyConfig(l1) },
                        onEditCard = onEditCard
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

@Composable
private fun L1DisciplinesOverviewContent(
    l1: String,
    l2Disciplines: List<L2DisciplineSummary>,
    totalCardsCount: Int,
    domainStats: DomainStats,
    onSelectL2: (String) -> Unit,
    onStudyAllL1: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Visão Completa do Baralho",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${l2Disciplines.size} matérias (L2) • ${l2Disciplines.sumOf { it.l3Count }} tópicos (L3) • $totalCardsCount cards",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onStudyAllL1,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_study_all_l1")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Estudar Tudo")
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Toque em uma matéria para acessar seus tópicos L3 e abas dedicadas de revisão e domínio.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Disciplinas do Baralho (${l2Disciplines.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        items(l2Disciplines, key = { it.l2 }) { discipline ->
            L2DisciplineShortcutCard(
                discipline = discipline,
                onClick = { onSelectL2(discipline.l2) }
            )
        }
    }
}

@Composable
private fun L2DisciplineShortcutCard(
    discipline: L2DisciplineSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val disciplineColor = DisciplinePalette.parseColor(discipline.colorHex)
    val iconVector = DisciplinePalette.getIconVector(discipline.iconKey)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("l2_card_${discipline.l2}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(disciplineColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = discipline.l2,
                        tint = disciplineColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = discipline.l2,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${discipline.totalCards} cards",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${discipline.l3Count} tópicos",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (discipline.dueCards > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "⚠️ ${discipline.dueCards} a revisar",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "✓ Em dia",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Acessar Matéria",
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(180f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Barra de Progresso de Domínio da Matéria
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Domínio da Disciplina",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${discipline.masteryPercentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = disciplineColor
                    )
                }
                LinearProgressIndicator(
                    progress = { (discipline.masteryPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = disciplineColor,
                    trackColor = disciplineColor.copy(alpha = 0.15f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ÁREA DEDICADA DA MATÉRIA L2 (COM SUAS PRÓPRIAS ABAS: TÓPICOS L3, REVISÃO, DOMÍNIO)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun L2DedicatedDetailScreen(
    l1: String,
    discipline: L2DisciplineSummary,
    selectedL2Tab: Int,
    onSelectTab: (Int) -> Unit,
    l2Cards: List<FlashcardEntity>,
    l2ReviewStats: ReviewStats,
    l2DomainStats: DomainStats,
    selectedL3Topics: List<Pair<String, String>>,
    onBackToL1: () -> Unit,
    onToggleSelectL3: (l2: String, l3: String) -> Unit,
    onClearL3Selection: () -> Unit,
    onOpenMoveDialog: () -> Unit,
    onStudyCards: (List<FlashcardEntity>) -> Unit,
    onOpenCreateCard: (defaultL1: String, defaultL2: String?) -> Unit,
    onOpenStudyConfig: (defaultL1: String) -> Unit,
    onEditCard: (FlashcardEntity) -> Unit,
    onEditL2Discipline: (newName: String, iconKey: String, colorHex: String) -> Unit,
    onDeleteL2: () -> Unit
) {
    var showEditL2Dialog by remember { mutableStateOf(false) }
    var showDeleteL2Confirm by remember { mutableStateOf(false) }
    val disciplineColor = DisciplinePalette.parseColor(discipline.colorHex)
    val iconVector = DisciplinePalette.getIconVector(discipline.iconKey)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(disciplineColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = discipline.l2,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "Baralho L1: $l1",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToL1,
                        modifier = Modifier.testTag("btn_back_to_l1")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar para Lista de Matérias L1"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditL2Dialog = true },
                        modifier = Modifier.testTag("btn_edit_l2_discipline")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Matéria L2 (Nome, Cor e Ícone)"
                        )
                    }
                    IconButton(
                        onClick = { onOpenCreateCard(l1, discipline.l2) },
                        modifier = Modifier.testTag("btn_add_card_l2")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Novo Card nesta Matéria"
                        )
                    }
                    IconButton(
                        onClick = { showDeleteL2Confirm = true },
                        modifier = Modifier.testTag("btn_delete_l2")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Matéria L2",
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
            // Três Abas Dedicadas Exclusivas deste L2
            PrimaryTabRow(
                selectedTabIndex = selectedL2Tab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedL2Tab == 0,
                    onClick = { onSelectTab(0) },
                    text = { Text("Tópicos (L3)", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_l2_topics")
                )
                Tab(
                    selected = selectedL2Tab == 1,
                    onClick = { onSelectTab(1) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Revisão", fontWeight = FontWeight.SemiBold)
                            if (l2ReviewStats.dueNow > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${l2ReviewStats.dueNow}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_l2_review")
                )
                Tab(
                    selected = selectedL2Tab == 2,
                    onClick = { onSelectTab(2) },
                    text = { Text("Domínio", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_l2_domain")
                )
            }

            when (selectedL2Tab) {
                0 -> {
                    // Aba 1: Tópicos L3 pertencentes a este L2
                    L2TopicsTabContent(
                        discipline = discipline,
                        l2Cards = l2Cards,
                        selectedL3Topics = selectedL3Topics,
                        onToggleSelectL3 = onToggleSelectL3,
                        onClearL3Selection = onClearL3Selection,
                        onOpenMoveDialog = onOpenMoveDialog,
                        onStudyCards = onStudyCards
                    )
                }
                1 -> {
                    // Aba 2: Revisão Dedicada deste L2
                    ReviewTabContent(
                        reviewStats = l2ReviewStats,
                        filteredCards = l2Cards,
                        l2Disciplines = listOf(discipline),
                        selectedL2Filter = discipline.l2,
                        selectedL3Filter = null,
                        onSelectL2Filter = {},
                        onSelectL3Filter = {},
                        onStartReviewSession = {
                            val dueCards = l2Cards.filter { it.dueTimestamp <= System.currentTimeMillis() }
                            onStudyCards(if (dueCards.isNotEmpty()) dueCards else l2Cards)
                        },
                        onOpenStudyConfig = { onOpenStudyConfig(l1) },
                        onEditCard = onEditCard
                    )
                }
                2 -> {
                    // Aba 3: Domínio Dedicado deste L2
                    DomainTabContent(
                        domainStats = l2DomainStats,
                        filteredCards = l2Cards,
                        l2Disciplines = listOf(discipline),
                        selectedL2Filter = discipline.l2,
                        selectedL3Filter = null,
                        onSelectL2Filter = {},
                        onSelectL3Filter = {},
                        onStudyNewCards = {
                            val newCards = l2Cards.filter { it.reps == 0 }
                            onStudyCards(if (newCards.isNotEmpty()) newCards else l2Cards)
                        },
                        onEditCard = onEditCard
                    )
                }
            }
        }
    }

    if (showDeleteL2Confirm) {
        AlertDialog(
            onDismissRequest = { showDeleteL2Confirm = false },
            title = { Text("Excluir Matéria L2?") },
            text = { Text("Deseja realmente excluir '${discipline.l2}' e todos os seus ${discipline.totalCards} cards?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteL2Confirm = false
                        onDeleteL2()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteL2Confirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showEditL2Dialog) {
        EditL2DisciplineDialog(
            currentL2Name = discipline.l2,
            currentColorHex = discipline.colorHex,
            currentIconKey = discipline.iconKey,
            onDismiss = { showEditL2Dialog = false },
            onConfirm = { newName, iconKey, colorHex ->
                showEditL2Dialog = false
                onEditL2Discipline(newName, iconKey, colorHex)
            }
        )
    }
}

@Composable
private fun L2TopicsTabContent(
    discipline: L2DisciplineSummary,
    l2Cards: List<FlashcardEntity>,
    selectedL3Topics: List<Pair<String, String>>,
    onToggleSelectL3: (l2: String, l3: String) -> Unit,
    onClearL3Selection: () -> Unit,
    onOpenMoveDialog: () -> Unit,
    onStudyCards: (List<FlashcardEntity>) -> Unit
) {
    val disciplineColor = DisciplinePalette.parseColor(discipline.colorHex)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = if (selectedL3Topics.isNotEmpty()) 100.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = discipline.l2,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${discipline.totalCards} cards • ${discipline.topics.size} tópicos (L3)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { onStudyCards(l2Cards) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_study_l2_all")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Estudar Matéria")
                            }
                        }

                        if (selectedL3Topics.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedL3Topics.size} tópico(s) L3 selecionado(s)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    TextButton(onClick = onClearL3Selection) {
                                        Text("Desmarcar Todos", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Tópicos de Estudo (L3)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (discipline.topics.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum subtópico L3 cadastrado nesta matéria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(discipline.topics, key = { it.l3 }) { topic ->
                    val isSelected = selectedL3Topics.contains(Pair(discipline.l2, topic.l3))
                    L3TopicRow(
                        topic = topic,
                        disciplineColor = disciplineColor,
                        isSelected = isSelected,
                        onToggleSelect = { onToggleSelectL3(discipline.l2, topic.l3) },
                        onStudy = {
                            val topicCards = l2Cards.filter { it.l3 == topic.l3 }
                            onStudyCards(if (topicCards.isNotEmpty()) topicCards else l2Cards)
                        }
                    )
                }
            }
        }

        // Barra de Ação Flutuante para Mover Tópicos Selecionados
        if (selectedL3Topics.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedL3Topics.size} selecionado(s)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tópicos L3 marcados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onClearL3Selection) {
                            Text("Limpar")
                        }

                        Button(
                            onClick = onOpenMoveDialog,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_move_l3_to_l2")
                        ) {
                            Icon(Icons.Default.DriveFileMove, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mover para L2")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun L3TopicRow(
    topic: L3TopicSummary,
    disciplineColor: Color,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onStudy: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() }
            .testTag("l3_row_${topic.l3}"),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else disciplineColor.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = "L3 TÓPICO",
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${topic.totalCards} cards",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (topic.dueCards > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${topic.dueCards} devido(s)",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = topic.l3,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = onStudy,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_study_l3_${topic.l3}")
            ) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Estudar", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoveL3ToL2Dialog(
    selectedTopicsCount: Int,
    existingL2Names: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (newL2Name: String, iconKey: String, colorHex: String) -> Unit
) {
    var deckName by remember { mutableStateOf("") }
    var selectedIconKey by remember { mutableStateOf("Folder") }
    var selectedColorHex by remember { mutableStateOf("#2563EB") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Mover Tópicos para Deck L2",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$selectedTopicsCount tópico(s) L3 selecionado(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("Nome do Baralho L2") },
                    placeholder = { Text("Ex: Direito Constitucional Avançado") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (existingL2Names.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Ou selecione um Deck L2 existente:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            existingL2Names.distinct().forEach { name ->
                                FilterChip(
                                    selected = deckName == name,
                                    onClick = {
                                        deckName = name
                                        selectedColorHex = DisciplinePalette.getColorForDiscipline(name)
                                        selectedIconKey = DisciplinePalette.getIconKeyForDiscipline(name)
                                    },
                                    label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Ícone do Baralho L2 (50 Ícones em Matriz Contígua 5x10)
                Column {
                    Text(
                        text = "Ícone do Baralho L2 (50 ícones):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        DisciplinePalette.selectableIcons.chunked(10).forEach { rowIcons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                rowIcons.forEach { item ->
                                    val isSelected = selectedIconKey == item.key
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceContainerLow
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 0.5.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            )
                                            .clickable { selectedIconKey = item.key },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Cor de Destaque (50 Cores em Matriz Contígua 5x10)
                Column {
                    Text(
                        text = "Cor de Destaque (50 cores):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        DisciplinePalette.selectableColors.chunked(10).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                rowColors.forEach { hex ->
                                    val color = DisciplinePalette.parseColor(hex)
                                    val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 0.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent
                                            )
                                            .clickable { selectedColorHex = hex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selecionado",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Prévia do Baralho L2",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DisciplinePalette.parseColor(selectedColorHex).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = DisciplinePalette.getIconVector(selectedIconKey),
                                    contentDescription = null,
                                    tint = DisciplinePalette.parseColor(selectedColorHex),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = deckName.ifBlank { "Nome do Baralho L2" },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Receberá $selectedTopicsCount tópico(s) L3",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (deckName.isNotBlank()) {
                        onConfirm(deckName, selectedIconKey, selectedColorHex)
                    }
                },
                enabled = deckName.isNotBlank()
            ) {
                Text("Confirmar Mover")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditL2DisciplineDialog(
    currentL2Name: String,
    currentColorHex: String,
    currentIconKey: String,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, iconKey: String, colorHex: String) -> Unit
) {
    var l2Name by remember { mutableStateOf(currentL2Name) }
    var selectedColorHex by remember { mutableStateOf(currentColorHex) }
    var selectedIconKey by remember { mutableStateOf(currentIconKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Editar Matéria (L2)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Personalize o nome, ícone e cor desta disciplina para todos os cards associados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = l2Name,
                    onValueChange = { l2Name = it },
                    label = { Text("Nome da Matéria L2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Ícone da Matéria (50 Ícones em Matriz Contígua 5x10 - Item 3.8)
                Column {
                    Text(
                        text = "Ícone da Matéria (50 ícones):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        DisciplinePalette.selectableIcons.chunked(10).forEach { rowIcons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                rowIcons.forEach { item ->
                                    val isSelected = selectedIconKey == item.key
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceContainerLow
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 0.5.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            )
                                            .clickable { selectedIconKey = item.key },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Cor de Destaque (50 Cores em Matriz Contígua 5x10 - Item 3.7)
                Column {
                    Text(
                        text = "Cor de Destaque (50 cores):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        DisciplinePalette.selectableColors.chunked(10).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                rowColors.forEach { hex ->
                                    val color = DisciplinePalette.parseColor(hex)
                                    val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 0.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent
                                            )
                                            .clickable { selectedColorHex = hex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selecionado",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Prévia do Card L2 atualizado
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Prévia Atualizada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DisciplinePalette.parseColor(selectedColorHex).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = DisciplinePalette.getIconVector(selectedIconKey),
                                    contentDescription = null,
                                    tint = DisciplinePalette.parseColor(selectedColorHex),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = l2Name.ifBlank { "Nome da Matéria L2" },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Estilo e identificador visual",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (l2Name.isNotBlank()) {
                        onConfirm(l2Name.trim(), selectedIconKey, selectedColorHex)
                    }
                },
                enabled = l2Name.isNotBlank()
            ) {
                Text("Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ReviewTabContent(
    reviewStats: ReviewStats,
    filteredCards: List<FlashcardEntity>,
    l2Disciplines: List<L2DisciplineSummary>,
    selectedL2Filter: String?,
    selectedL3Filter: String?,
    onSelectL2Filter: (String?) -> Unit,
    onSelectL3Filter: (String?) -> Unit,
    onStartReviewSession: () -> Unit,
    onOpenStudyConfig: () -> Unit = {},
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
                        text = "Progresso de Revisão Espaçada (SM-2)",
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
private fun DomainTabContent(
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

@Composable
private fun HierarchyFilterSelector(
    l2Disciplines: List<L2DisciplineSummary>,
    selectedL2: String?,
    selectedL3: String?,
    onSelectL2: (String?) -> Unit,
    onSelectL3: (String?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Filtrar Nível:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Row of L2 Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedL2 == null,
                onClick = { onSelectL2(null) },
                label = { Text("Toda a L1 (Geral)") },
                colors = FilterChipDefaults.filterChipColors()
            )

            l2Disciplines.forEach { discipline ->
                val isSelected = selectedL2 == discipline.l2
                val color = DisciplinePalette.parseColor(discipline.colorHex)

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onSelectL2(if (isSelected) null else discipline.l2)
                    },
                    label = { Text(discipline.l2) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                )
            }
        }

        // If L2 selected, show L3 topics chips
        if (selectedL2 != null) {
            val discipline = l2Disciplines.find { it.l2 == selectedL2 }
            if (discipline != null && discipline.topics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tópico L3 específico:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedL3 == null,
                        onClick = { onSelectL3(null) },
                        label = { Text("Todos os tópicos") }
                    )
                    discipline.topics.forEach { topic ->
                        val isSelected = selectedL3 == topic.l3
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onSelectL3(if (isSelected) null else topic.l3)
                            },
                            label = { Text(topic.l3.take(28) + if (topic.l3.length > 28) "…" else "") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewMetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DomainCategoryBadge(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CardPreviewItem(
    card: FlashcardEntity,
    onEdit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val disciplineColor = DisciplinePalette.parseColor(DisciplinePalette.getColorForDiscipline(card.l2))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Discipline and Topic Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(disciplineColor)
                    )
                    Text(
                        text = card.l2,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = disciplineColor
                    )
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = card.l3,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                }

                // Mastery Status Badge + Edit Action
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (badgeText, badgeColor) = when (card.masteryLevel) {
                        2 -> "Dominado" to Color(0xFF059669)
                        1 -> "Aprendendo" to Color(0xFFD97706)
                        else -> "Novo" to Color(0xFF6B7280)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 4.dp)
                            .testTag("btn_edit_card_${card.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar este Card",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Front question with HTML preservation
            HtmlText(
                html = card.frontHtml,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )

            // If tapped/expanded, show back answer with full HTML!
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "RESPOSTA & FUNDAMENTAÇÃO:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    HtmlText(
                        html = card.backHtml,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (card.tags.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tags: ${card.tags}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
