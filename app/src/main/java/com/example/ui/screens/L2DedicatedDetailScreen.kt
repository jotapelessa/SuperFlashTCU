package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    srsAlgorithm: String = "FSRS"
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
                            Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, modifier = Modifier.size(18.dp))
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
fun L3TopicRow(
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
