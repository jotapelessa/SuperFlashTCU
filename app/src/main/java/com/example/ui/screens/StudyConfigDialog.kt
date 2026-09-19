package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.data.local.HierarchyFolderTuple
import com.example.data.model.FlashcardEntity
import com.example.data.model.PerCardTimeLimit
import com.example.data.model.StudyFilterMode
import com.example.data.model.StudyTimerConfig
import com.example.data.repository.DeckRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyConfigDialog(
    allCards: List<FlashcardEntity>,
    existingFolders: List<HierarchyFolderTuple>,
    initialL1: String?,
    initialL2: String? = null,
    initialL3: String? = null,
    availableTags: List<String> = emptyList(),
    initialTag: String? = null,
    initialSmartShuffle: Boolean = true,
    initialTimerConfig: StudyTimerConfig = StudyTimerConfig(),
    onStartSession: (List<FlashcardEntity>, StudyTimerConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedL1 by remember { mutableStateOf(initialL1 ?: "") }
    var selectedL2 by remember { mutableStateOf(initialL2 ?: "") }
    var selectedL3 by remember { mutableStateOf(initialL3 ?: "") }
    var selectedTag by remember { mutableStateOf(initialTag ?: "") }
    var selectedMode by remember { mutableStateOf(StudyFilterMode.DUE_ONLY) }
    var selectedLimit by remember { mutableIntStateOf(20) }
    var smartShuffleEnabled by remember { mutableStateOf(initialSmartShuffle) }
    var selectedPerCardLimit by remember { mutableStateOf(initialTimerConfig.perCardLimit) }
    var selectedTargetMinutes by remember { mutableIntStateOf(initialTimerConfig.targetSessionMinutes) }

    val l1List = remember(existingFolders) {
        existingFolders.map { it.l1 }.distinct().sorted()
    }
    val l2List = remember(existingFolders, selectedL1) {
        if (selectedL1.isBlank()) {
            existingFolders.map { it.l2 }.distinct().sorted()
        } else {
            existingFolders.filter { it.l1 == selectedL1 }.map { it.l2 }.distinct().sorted()
        }
    }
    val l3List = remember(existingFolders, selectedL1, selectedL2) {
        existingFolders.filter {
            (selectedL1.isBlank() || it.l1 == selectedL1) &&
            (selectedL2.isBlank() || it.l2 == selectedL2)
        }.map { it.l3 }.distinct().sorted()
    }

    // Live preview of matching cards with Tag filter and Smart Shuffle
    val matchingCards by remember(
        allCards,
        selectedL1,
        selectedL2,
        selectedL3,
        selectedTag,
        selectedMode,
        selectedLimit,
        smartShuffleEnabled
    ) {
        derivedStateOf {
            val now = System.currentTimeMillis()
            var filtered = allCards

            if (selectedL1.isNotBlank()) {
                filtered = filtered.filter { it.l1 == selectedL1 }
            }
            if (selectedL2.isNotBlank()) {
                filtered = filtered.filter { it.l2 == selectedL2 }
            }
            if (selectedL3.isNotBlank()) {
                filtered = filtered.filter { it.l3 == selectedL3 }
            }

            // Cross-Domain Tag Filter
            if (selectedTag.isNotBlank()) {
                filtered = filtered.filter { DeckRepository.hasTag(it, selectedTag) }
            }

            filtered = when (selectedMode) {
                StudyFilterMode.ALL -> filtered
                StudyFilterMode.DUE_ONLY -> {
                    val due = filtered.filter { it.dueTimestamp <= now }
                    if (due.isNotEmpty()) due else filtered
                }
                StudyFilterMode.NEW_AND_LEARNING -> {
                    val candidate = filtered.filter { it.masteryLevel < 2 || it.reps == 0 }
                    if (candidate.isNotEmpty()) candidate else filtered
                }
                StudyFilterMode.DIFFICULT_ONLY -> {
                    val hard = filtered.filter { it.lapses > 0 || it.easeFactor < 2.4f }
                    if (hard.isNotEmpty()) hard else filtered
                }
            }

            // Smart Shuffle prioritization
            val ordered = if (smartShuffleEnabled) {
                DeckRepository.applySmartShuffle(filtered, now)
            } else {
                filtered
            }

            if (selectedLimit > 0) ordered.take(selectedLimit) else ordered
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Configurar Estudo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_study_config")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("study_config_dialog")
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Selecione o escopo hierárquico e o modo de revisão desejado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scope Hierarchy Filters
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ESCOPO DE HIERARQUIA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // L1 Selector
                        DropdownHierarchySelector(
                            label = "L1 Baralho",
                            currentValue = selectedL1.ifBlank { "Todos os Baralhos" },
                            options = listOf("Todos os Baralhos") + l1List,
                            onSelect = { selectedL1 = if (it == "Todos os Baralhos") "" else it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // L2 Selector
                        DropdownHierarchySelector(
                            label = "L2 Matéria / Disciplina",
                            currentValue = selectedL2.ifBlank { "Todas as Matérias" },
                            options = listOf("Todas as Matérias") + l2List,
                            onSelect = { selectedL2 = if (it == "Todas as Matérias") "" else it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // L3 Selector
                        DropdownHierarchySelector(
                            label = "L3 Subtópico / Assunto",
                            currentValue = selectedL3.ifBlank { "Todos os Subtópicos" },
                            options = listOf("Todos os Subtópicos") + l3List,
                            onSelect = { selectedL3 = if (it == "Todos os Subtópicos") "" else it }
                        )
                    }
                }

                // Cross-Domain Tag Filter Section
                if (availableTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                                    text = "FILTRO POR TAG (CROSS-DOMAIN)",
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
                                FilterChip(
                                    selected = selectedTag.isBlank(),
                                    onClick = { selectedTag = "" },
                                    label = { Text("Todas as tags") },
                                    modifier = Modifier.testTag("filter_tag_chip_all")
                                )
                                availableTags.forEach { tag ->
                                    FilterChip(
                                        selected = selectedTag.equals(tag, ignoreCase = true),
                                        onClick = {
                                            selectedTag = if (selectedTag.equals(tag, ignoreCase = true)) "" else tag
                                        },
                                        label = { Text("#$tag") },
                                        modifier = Modifier.testTag("filter_tag_chip_$tag")
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Selection
                Text(
                    text = "Modo de Foco",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StudyFilterMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(text = mode.label, fontWeight = FontWeight.Bold)
                                    Text(text = mode.description, style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Batch limit
                Text(
                    text = "Quantidade de Cards",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 20, 50, -1).forEach { limit ->
                        FilterChip(
                            selected = selectedLimit == limit,
                            onClick = { selectedLimit = limit },
                            label = { Text(if (limit == -1) "Sem limite" else "$limit cards") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smart Shuffle Toggle Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Smart Shuffle",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Prioriza cards vencidos e de menor domínio antes de introduzir novos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = smartShuffleEnabled,
                            onCheckedChange = { smartShuffleEnabled = it },
                            modifier = Modifier.testTag("switch_smart_shuffle")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Configurable Timer Section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Temporizador da Sessão",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Per-card limit
                        Text(
                            text = "Tempo Limite por Card:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PerCardTimeLimit.entries.forEach { limit ->
                                FilterChip(
                                    selected = selectedPerCardLimit == limit,
                                    onClick = { selectedPerCardLimit = limit },
                                    label = { Text(limit.label) },
                                    modifier = Modifier.testTag("timer_limit_pill_${limit.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Target session minutes
                        Text(
                            text = "Meta de Duração da Sessão:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0 to "Livre", 5 to "5 min", 10 to "10 min", 15 to "15 min", 25 to "25 min (Pomodoro)").forEach { (mins, label) ->
                                FilterChip(
                                    selected = selectedTargetMinutes == mins,
                                    onClick = { selectedTargetMinutes = mins },
                                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                    modifier = Modifier.testTag("target_min_pill_$mins")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Matching preview & Launch Button (Padronizado e sem overflow)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${matchingCards.size} cards selecionados",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (smartShuffleEnabled) "Smart Shuffle ativado" else "Ordem padrão",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (matchingCards.isNotEmpty()) {
                                    val timerConfig = StudyTimerConfig(
                                        perCardLimit = selectedPerCardLimit,
                                        targetSessionMinutes = selectedTargetMinutes
                                    )
                                    onStartSession(matchingCards, timerConfig)
                                }
                            },
                            enabled = matchingCards.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_start_filtered_study")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Estudo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownHierarchySelector(
    label: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = currentValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
