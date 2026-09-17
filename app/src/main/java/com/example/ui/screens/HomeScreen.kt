package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.L1DeckSummary
import com.example.ui.components.DeckCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    l1Decks: List<L1DeckSummary>,
    todayReviewed: Int = 0,
    dailyGoal: Int = 20,
    allTags: List<String> = emptyList(),
    onSetDailyGoal: (Int) -> Unit = {},
    onStudyByTag: (String) -> Unit = {},
    onSelectL1: (String) -> Unit,
    onQuickStudyL1: (String) -> Unit,
    onOpenImport: () -> Unit,
    onResetData: () -> Unit,
    onOpenAnalytics: () -> Unit = {},
    onOpenCreateCard: () -> Unit = {},
    onOpenMoveL2ToL1: () -> Unit = {},
    onOpenCompareDecks: () -> Unit = {},
    onUpdateL1Customization: (oldL1: String, newName: String, colorHex: String?, courseName: String?, coverUrl: String?) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showGoalDialog by remember { mutableStateOf(false) }
    var deckToEdit by remember { mutableStateOf<L1DeckSummary?>(null) }

    val filteredDecks = remember(l1Decks, searchQuery) {
        if (searchQuery.isBlank()) {
            l1Decks
        } else {
            l1Decks.filter { it.l1.contains(searchQuery, ignoreCase = true) }
        }
    }

    val totalCards = remember(l1Decks) { l1Decks.sumOf { it.totalCards } }
    val totalDue = remember(l1Decks) { l1Decks.sumOf { it.dueCards } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SuperFlash",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = onOpenCompareDecks,
                        modifier = Modifier.testTag("btn_home_compare_decks")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Comparar Baralhos L2"
                        )
                    }
                    IconButton(
                        onClick = onOpenMoveL2ToL1,
                        modifier = Modifier.testTag("btn_home_move_l2")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DriveFileMove,
                            contentDescription = "Mover Decks L2"
                        )
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filtrar baralhos L1...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_l1_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )
            }

            // Section Title - Baralhos L1 (Listagem de Baralhos no topo)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Baralhos L1 (${filteredDecks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toque para abrir L2 e L3",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Deck List (Listagem de baralhos no topo)
            if (filteredDecks.isEmpty()) {
                item {
                    EmptyDecksCard(onOpenImport = onOpenImport)
                }
            } else {
                items(filteredDecks, key = { it.l1 }) { deck ->
                    DeckCard(
                        deck = deck,
                        onClick = { onSelectL1(deck.l1) },
                        onQuickStudy = { onQuickStudyL1(deck.l1) },
                        onEditDeck = { deckToEdit = it }
                    )
                }
            }

            // Cross-Domain Tag Quick Study Filter Bar
            if (allTags.isNotEmpty()) {
                item {
                    CrossDomainTagFilterBar(
                        tags = allTags,
                        onTagClick = onStudyByTag
                    )
                }
            }

            // Summary Dashboard Header (Resumo de estatísticas disposto abaixo)
            item {
                StatsOverviewHeader(
                    totalDecks = l1Decks.size,
                    totalCards = totalCards,
                    totalDue = totalDue,
                    onOpenAnalytics = onOpenAnalytics
                )
            }

            // Daily Study Goal Card (Meta diária disposta abaixo)
            item {
                DailyGoalProgressCard(
                    todayReviewed = todayReviewed,
                    dailyGoal = dailyGoal,
                    onAdjustGoal = { showGoalDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Daily Goal Adjustment Dialog
    if (showGoalDialog) {
        DailyGoalDialog(
            currentGoal = dailyGoal,
            onSave = { newGoal ->
                onSetDailyGoal(newGoal)
                showGoalDialog = false
            },
            onDismiss = { showGoalDialog = false }
        )
    }

    // Edit L1 Deck Dialog
    deckToEdit?.let { deck ->
        EditL1DeckDialog(
            deck = deck,
            onConfirmSave = { oldL1, newL1, colorHex, courseName, coverUrl ->
                onUpdateL1Customization(oldL1, newL1, colorHex, courseName, coverUrl)
                deckToEdit = null
            },
            onDismiss = { deckToEdit = null }
        )
    }
}

@Composable
private fun DailyGoalProgressCard(
    todayReviewed: Int,
    dailyGoal: Int,
    onAdjustGoal: () -> Unit
) {
    val progress = if (dailyGoal > 0) {
        (todayReviewed.toFloat() / dailyGoal).coerceIn(0f, 1f)
    } else 0f
    val percent = (progress * 100).toInt()
    val isCompleted = todayReviewed >= dailyGoal && dailyGoal > 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_goal_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular Progress Ring Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("daily_goal_progress_ring"),
                    color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Meta atingida",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details and Goal action
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Meta Diária de Estudos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "$todayReviewed de $dailyGoal cards revisados hoje",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isCompleted) {
                        "🎉 Parabéns! Meta de hoje cumprida!"
                    } else {
                        val remaining = maxOf(0, dailyGoal - todayReviewed)
                        "Faltam $remaining cards para bater a meta"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onAdjustGoal,
                    modifier = Modifier.testTag("btn_adjust_goal")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Ajustar Meta", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun CrossDomainTagFilterBar(
    tags: List<String>,
    onTagClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tag_filter_row")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ESTUDAR POR TAG (CROSS-DOMAIN)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    FilterChip(
                        selected = false,
                        onClick = { onTagClick(tag) },
                        label = { Text("#$tag") },
                        modifier = Modifier.testTag("tag_chip_$tag")
                    )
                }
            }
        }
    }
}

@Composable
fun DailyGoalDialog(
    currentGoal: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal.toString()) }
    var selectedPreset by remember { mutableIntStateOf(currentGoal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Definir Meta Diária", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Quantos cards você pretende revisar todos os dias?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 20, 30, 50, 100).forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = {
                                selectedPreset = preset
                                goalText = preset.toString()
                            },
                            label = { Text("$preset cards") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = goalText,
                    onValueChange = {
                        val filtered = it.filter { char -> char.isDigit() }
                        goalText = filtered
                        selectedPreset = filtered.toIntOrNull() ?: 0
                    },
                    label = { Text("Número de cards por dia") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_daily_goal")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = goalText.toIntOrNull() ?: currentGoal
                    if (parsed > 0) {
                        onSave(parsed)
                    }
                },
                modifier = Modifier.testTag("btn_save_daily_goal")
            ) {
                Text("Salvar Meta")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_daily_goal")
            ) {
                Text("Cancelar")
            }
        },
        modifier = Modifier.testTag("daily_goal_dialog")
    )
}

@Composable
private fun StatsOverviewHeader(
    totalDecks: Int,
    totalCards: Int,
    totalDue: Int,
    onOpenAnalytics: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stats_overview_header"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Visão Global",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenAnalytics() }
                        .testTag("btn_open_progress_details")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ver Gráficos",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderStatItem(
                    count = totalDecks.toString(),
                    label = "Baralhos L1",
                    badgeColor = MaterialTheme.colorScheme.primary
                )
                HeaderStatItem(
                    count = totalCards.toString(),
                    label = "Total de Cards",
                    badgeColor = MaterialTheme.colorScheme.secondary
                )
                HeaderStatItem(
                    count = totalDue.toString(),
                    label = "Vencidos",
                    badgeColor = if (totalDue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun HeaderStatItem(
    count: String,
    label: String,
    badgeColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = badgeColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyDecksCard(onOpenImport: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nenhum baralho encontrado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Importe um arquivo CSV do Anki com o campo Deck separado por '::' (L1::L2::L3)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOpenImport,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_empty_import_csv")
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importar Arquivo CSV")
            }
        }
    }
}

