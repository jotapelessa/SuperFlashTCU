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
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    val selectedL3Topics = remember { mutableStateListOf<Pair<String, String>>() }
    val expandedL2Map = remember { mutableStateMapOf<String, Boolean>() }

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
                            text = "Estrutura Hierárquica L2 & L3",
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
            // Three Tabs: Estrutura (L2/L3), Revisão, Domínio
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Estrutura (L2/L3)", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_structure")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Revisão", fontWeight = FontWeight.SemiBold)
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
                    text = { Text("Domínio", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_domain")
                )
            }

            when (selectedTab) {
                0 -> {
                    // Structure: List-based design of L2 Disciplines & L3 Topics
                    StructureTabContent(
                        l2Disciplines = l2Disciplines,
                        expandedL2Map = expandedL2Map,
                        selectedL3Topics = selectedL3Topics,
                        onToggleExpandL2 = { l2 ->
                            expandedL2Map[l2] = !(expandedL2Map[l2] ?: true)
                        },
                        onToggleSelectL3 = { l2, l3 ->
                            val pair = Pair(l2, l3)
                            if (selectedL3Topics.contains(pair)) {
                                selectedL3Topics.remove(pair)
                            } else {
                                selectedL3Topics.add(pair)
                            }
                        },
                        onSelectAllL3InL2 = { l2, topics ->
                            val pairs = topics.map { Pair(l2, it.l3) }
                            if (selectedL3Topics.containsAll(pairs)) {
                                selectedL3Topics.removeAll(pairs.toSet())
                            } else {
                                pairs.forEach { if (!selectedL3Topics.contains(it)) selectedL3Topics.add(it) }
                            }
                        },
                        onClearL3Selection = { selectedL3Topics.clear() },
                        onOpenMoveDialog = { showMoveDialog = true },
                        onStudyL2 = { discipline ->
                            val cardsToStudy = filteredCards.filter { it.l2 == discipline.l2 }
                            onStudyCards(cardsToStudy)
                        },
                        onStudyL3 = { topic ->
                            val cardsToStudy = filteredCards.filter { it.l2 == topic.l2 && it.l3 == topic.l3 }
                            onStudyCards(cardsToStudy)
                        },
                        onStudyAllL1 = {
                            onStudyCards(filteredCards)
                        },
                        onDeleteL2 = { l2Name ->
                            onDeleteL2Discipline(l1, l2Name)
                        }
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

@Composable
private fun StructureTabContent(
    l2Disciplines: List<L2DisciplineSummary>,
    expandedL2Map: Map<String, Boolean>,
    selectedL3Topics: List<Pair<String, String>>,
    onToggleExpandL2: (String) -> Unit,
    onToggleSelectL3: (l2: String, l3: String) -> Unit,
    onSelectAllL3InL2: (l2: String, topics: List<L3TopicSummary>) -> Unit,
    onClearL3Selection: () -> Unit,
    onOpenMoveDialog: () -> Unit,
    onStudyL2: (L2DisciplineSummary) -> Unit,
    onStudyL3: (L3TopicSummary) -> Unit,
    onStudyAllL1: () -> Unit,
    onDeleteL2: ((String) -> Unit)? = null
) {
    var l2ToDelete by remember { mutableStateOf<L2DisciplineSummary?>(null) }

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
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Visão Completa do Baralho",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${l2Disciplines.size} matérias (L2) • ${l2Disciplines.sumOf { it.l3Count }} tópicos (L3)",
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
                                        text = "${selectedL3Topics.size} tópico(s) L3 marcado(s)",
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

            items(l2Disciplines, key = { it.l2 }) { discipline ->
                val isExpanded = expandedL2Map[discipline.l2] ?: true
                val disciplineColor = DisciplinePalette.parseColor(discipline.colorHex)
                val iconVector = DisciplinePalette.getIconVector(discipline.iconKey)
                val l3PairsInDiscipline = discipline.topics.map { Pair(discipline.l2, it.l3) }
                val isAllL3SelectedInDiscipline = discipline.topics.isNotEmpty() && selectedL3Topics.containsAll(l3PairsInDiscipline)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("l2_card_${discipline.l2}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // L2 Discipline Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleExpandL2(discipline.l2) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(disciplineColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = discipline.l2,
                                    tint = disciplineColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = disciplineColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "L2 MATÉRIA",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = disciplineColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${discipline.totalCards} cards",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (discipline.dueCards > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${discipline.dueCards} a revisar",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = discipline.l2,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (discipline.topics.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSelectAllL3InL2(discipline.l2, discipline.topics) },
                                    modifier = Modifier.testTag("btn_select_all_l3_${discipline.l2}")
                                ) {
                                    Icon(
                                        imageVector = if (isAllL3SelectedInDiscipline) Icons.Default.CheckCircle else Icons.Default.Checklist,
                                        contentDescription = "Selecionar todos os tópicos deste L2",
                                        tint = if (isAllL3SelectedInDiscipline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onStudyL2(discipline) },
                                modifier = Modifier.testTag("btn_study_l2_${discipline.l2}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Estudar Matéria",
                                    tint = disciplineColor
                                )
                            }

                            if (onDeleteL2 != null) {
                                IconButton(
                                    onClick = { l2ToDelete = discipline },
                                    modifier = Modifier.testTag("btn_delete_l2_${discipline.l2}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Deletar Matéria L2",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            IconButton(onClick = { onToggleExpandL2(discipline.l2) }) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Nested L3 Sub-decks List
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (discipline.topics.isEmpty()) {
                                    Text(
                                        text = "Nenhum subtópico L3 específico cadastrado",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                } else {
                                    discipline.topics.forEach { topic ->
                                        val isSelected = selectedL3Topics.contains(Pair(discipline.l2, topic.l3))
                                        L3TopicRow(
                                            topic = topic,
                                            disciplineColor = disciplineColor,
                                            isSelected = isSelected,
                                            onToggleSelect = { onToggleSelectL3(discipline.l2, topic.l3) },
                                            onStudy = { onStudyL3(topic) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

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

        // Delete L2 Discipline Confirmation Dialog
        l2ToDelete?.let { disc ->
            AlertDialog(
                onDismissRequest = { l2ToDelete = null },
                title = { Text("Excluir Matéria L2 Repetida?") },
                text = {
                    Text("Deseja realmente excluir a matéria '${disc.l2}' e todos os seus ${disc.totalCards} cards?\n\nEsta ação excluirá as matérias iguais/repetidas do banco de dados permanentemente.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteL2?.invoke(disc.l2)
                            l2ToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Excluir Matéria")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { l2ToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
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

                Column {
                    Text(
                        text = "Ícone do Baralho L2:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DisciplinePalette.selectableIcons.forEach { item ->
                            val isSelected = selectedIconKey == item.key
                            Surface(
                                onClick = { selectedIconKey = item.key },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = item.label.split("/").first().trim(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = "Cor de Destaque:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DisciplinePalette.selectableColors.forEach { hex ->
                            val color = DisciplinePalette.parseColor(hex)
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColorHex = hex }
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    ),
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
