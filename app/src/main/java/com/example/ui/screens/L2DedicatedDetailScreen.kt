package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.model.isDueForReview
import com.example.data.model.L2DisciplineSummary
import com.example.data.model.L3TopicSummary
import com.example.data.model.ReviewStats
import com.example.ui.components.DisciplinePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun L2DedicatedDetailScreen(
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
    onOpenStudyConfig: (defaultL1: String, defaultL2: String?, defaultL3: String?) -> Unit,
    onEditCard: (FlashcardEntity) -> Unit,
    onEditL2Discipline: (newName: String, iconKey: String, colorHex: String) -> Unit,
    onDeleteL2: () -> Unit,
    onUpdateDailyGoalPercentage: (Float) -> Unit = {},
    srsAlgorithm: String = "FSRS",
    activeL3Topic: String? = null,
    onRenameL3Topic: ((oldL3: String, newL3: String) -> Unit)? = null,
    onDeleteL3Topic: ((l3: String) -> Unit)? = null
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
                        onClick = { onOpenStudyConfig(l1, discipline.l2, null) },
                        modifier = Modifier.testTag("btn_config_study_l2")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Configurar Estudo desta Matéria"
                        )
                    }
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
                        onStudyCards = onStudyCards,
                        onUpdateDailyGoalPercentage = onUpdateDailyGoalPercentage,
                        activeL3Topic = activeL3Topic,
                        onRenameL3Topic = onRenameL3Topic,
                        onDeleteL3Topic = onDeleteL3Topic
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
                            val dueCards = l2Cards.filter { it.isDueForReview() }
                            onStudyCards(if (dueCards.isNotEmpty()) dueCards else l2Cards)
                        },
                        onOpenStudyConfig = { onOpenStudyConfig(l1, discipline.l2, null) },
                        onEditCard = onEditCard,
                        srsAlgorithm = srsAlgorithm
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
fun L2TopicsTabContent(
    discipline: L2DisciplineSummary,
    l2Cards: List<FlashcardEntity>,
    selectedL3Topics: List<Pair<String, String>>,
    onToggleSelectL3: (l2: String, l3: String) -> Unit,
    onClearL3Selection: () -> Unit,
    onOpenMoveDialog: () -> Unit,
    onStudyCards: (List<FlashcardEntity>) -> Unit,
    onUpdateDailyGoalPercentage: (Float) -> Unit = {},
    activeL3Topic: String? = null,
    onRenameL3Topic: ((oldL3: String, newL3: String) -> Unit)? = null,
    onDeleteL3Topic: ((l3: String) -> Unit)? = null
) {
    val disciplineColor = DisciplinePalette.parseColor(discipline.colorHex)
    val isSelectionMode = selectedL3Topics.isNotEmpty()

    var contextTopicForMenu by remember { mutableStateOf<L3TopicSummary?>(null) }
    var topicToRename by remember { mutableStateOf<L3TopicSummary?>(null) }
    var topicToDelete by remember { mutableStateOf<L3TopicSummary?>(null) }

    LaunchedEffect(discipline.l2) {
        onClearL3Selection()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = if (selectedL3Topics.isNotEmpty()) 100.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
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
                        // 1. TÍTULO COMPLETO (Largura total, nunca mais quebra letras)
                        Text(
                            text = discipline.l2,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 2. METADADOS E CHIPS ELEGANTES
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${discipline.totalCards} cards",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${discipline.topics.size} tópicos (L3)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 3. BOTÕES DE AÇÃO EXPANDIDOS E BALANCEADOS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onStudyCards(l2Cards) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_study_l2_all")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Estudar Tudo", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 13.sp)
                            }

                            if (!activeL3Topic.isNullOrBlank()) {
                                FilledTonalButton(
                                    onClick = {
                                        val topicCards = l2Cards.filter { it.l3 == activeL3Topic }
                                        onStudyCards(if (topicCards.isNotEmpty()) topicCards else l2Cards)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(46.dp)
                                        .testTag("btn_study_active_l3")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Subtópico da Vez", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 13.sp)
                                }
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

            // Card Consolidado de Metas Diárias
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Meta Diária da Matéria",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Chips 5% | 7.5% | 10%
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(5f, 7.5f, 10f).forEach { pct ->
                                    val isSelected = kotlin.math.abs(discipline.dailyGoalPercentage - pct) < 0.1f
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onUpdateDailyGoalPercentage(pct) },
                                        label = {
                                            Text(
                                                text = if (pct == 7.5f) "7.5%" else "${pct.toInt()}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estudados hoje: ${discipline.reviewedToday} / ${discipline.dailyTargetCards} cards",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (discipline.isDailyGoalMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "%.0f%%".format(discipline.dailyProgressPercentage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (discipline.isDailyGoalMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
                        }

                        val animatedL2Progress by animateFloatAsState(
                            targetValue = (discipline.dailyProgressPercentage / 100f).coerceIn(0f, 1f),
                            animationSpec = tween(durationMillis = 600)
                        )
                        LinearProgressIndicator(
                            progress = { animatedL2Progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (discipline.isDailyGoalMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )

                        if (discipline.isDailyGoalMet) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Parabéns! Meta total desta disciplina atingida hoje!",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tópicos de Estudo (L3)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toque longo para opções",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                    val isActive = (topic.l3 == activeL3Topic)
                    L3TopicRow(
                        topic = topic,
                        disciplineColor = disciplineColor,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        isActive = isActive,
                        onToggleSelect = { onToggleSelectL3(discipline.l2, topic.l3) },
                        onStudy = {
                            val topicCards = l2Cards.filter { it.l3 == topic.l3 }
                            onStudyCards(if (topicCards.isNotEmpty()) topicCards else l2Cards)
                        },
                        onLongClick = {
                            contextTopicForMenu = topic
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
                            Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mover para L2")
                        }
                    }
                }
            }
        }

        contextTopicForMenu?.let { topic ->
            TopicContextMenuDialog(
                topic = topic,
                onDismiss = { contextTopicForMenu = null },
                onStudy = {
                    val topicCards = l2Cards.filter { it.l3 == topic.l3 }
                    onStudyCards(if (topicCards.isNotEmpty()) topicCards else l2Cards)
                },
                onRename = {
                    topicToRename = topic
                },
                onMove = {
                    onToggleSelectL3(discipline.l2, topic.l3)
                    onOpenMoveDialog()
                },
                onDelete = {
                    topicToDelete = topic
                },
                onSelectMultiple = {
                    onToggleSelectL3(discipline.l2, topic.l3)
                }
            )
        }

        topicToRename?.let { topic ->
            RenameL3TopicDialog(
                currentL3Name = topic.l3,
                onDismiss = { topicToRename = null },
                onConfirm = { newName ->
                    topicToRename = null
                    onRenameL3Topic?.invoke(topic.l3, newName)
                }
            )
        }

        topicToDelete?.let { topic ->
            DeleteL3ConfirmDialog(
                topic = topic,
                onDismiss = { topicToDelete = null },
                onConfirm = {
                    topicToDelete = null
                    onDeleteL3Topic?.invoke(topic.l3)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun L3TopicRow(
    topic: L3TopicSummary,
    disciplineColor: Color,
    isSelected: Boolean,
    isSelectionMode: Boolean = false,
    isActive: Boolean = false,
    onToggleSelect: () -> Unit,
    onStudy: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelect()
                    } else {
                        onStudy()
                    }
                },
                onLongClick = onLongClick
            )
            .testTag("l3_row_${topic.l3}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else if (isActive) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = BorderStroke(
            width = if (isSelected) 2.dp else if (isActive) 1.5.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else if (isActive) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            } else {
                disciplineColor.copy(alpha = 0.25f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "🎯 DA VEZ",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

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

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (topic.isDailyGoalMet) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Meta Batida! (${topic.reviewedToday}/${topic.dailyTargetCards}) ✅",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    } else {
                        Text(
                            text = "Meta diária: ${topic.reviewedToday}/${topic.dailyTargetCards} cards (${topic.dailyGoalPercentage.toInt()}%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (!topic.isDailyGoalMet && topic.dailyTargetCards > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val animatedL3Progress by animateFloatAsState(
                        targetValue = (topic.dailyProgressPercentage / 100f).coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 500)
                    )
                    LinearProgressIndicator(
                        progress = { animatedL3Progress },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
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

@Composable
fun TopicContextMenuDialog(
    topic: L3TopicSummary,
    onDismiss: () -> Unit,
    onStudy: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onSelectMultiple: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = topic.l3,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${topic.totalCards} flashcards no subtópico",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ListItem(
                    headlineContent = { Text("Estudar este Subtópico", fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onDismiss(); onStudy() }
                )
                ListItem(
                    headlineContent = { Text("Renomear Subtópico") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable { onDismiss(); onRename() }
                )
                ListItem(
                    headlineContent = { Text("Mover para outra Matéria (L2)") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null) },
                    modifier = Modifier.clickable { onDismiss(); onMove() }
                )
                ListItem(
                    headlineContent = { Text("Selecionar Múltiplos (Lote)") },
                    leadingContent = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    modifier = Modifier.clickable { onDismiss(); onSelectMultiple() }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                ListItem(
                    headlineContent = { Text("Excluir Subtópico", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { onDismiss(); onDelete() }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun RenameL3TopicDialog(
    currentL3Name: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentL3Name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renomear Subtópico L3") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Altere o nome deste subtópico para todos os seus flashcards:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nome do Subtópico L3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank()) {
                        onConfirm(newName.trim())
                    }
                },
                enabled = newName.isNotBlank() && newName.trim() != currentL3Name
            ) {
                Text("Salvar")
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
fun DeleteL3ConfirmDialog(
    topic: L3TopicSummary,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Subtópico L3?") },
        text = {
            Text("Esta ação removerá o subtópico '${topic.l3}' e todos os seus ${topic.totalCards} cards associados.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
