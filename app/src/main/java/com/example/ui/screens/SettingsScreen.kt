package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.viewmodel.DeckViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PerCardTimeLimit
import com.example.data.model.StudyTimerConfig
import com.example.ui.theme.AccentColorOption
import com.example.ui.theme.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    dailyGoal: Int,
    smartShuffleEnabled: Boolean,
    timerConfig: StudyTimerConfig,
    totalCardsCount: Int,
    totalDecksCount: Int,
    themeMode: AppThemeMode,
    accentColor: AccentColorOption,
    geminiApiKey: String = "",
    geminiModelVersion: String = "gemini-flash-latest",
    supabaseUrl: String = "",
    supabaseKey: String = "",
    supabaseAutoSync: Boolean = false,
    supabaseSyncState: String? = null,
    supabaseIsLoading: Boolean = false,
    onSetDailyGoal: (Int) -> Unit,
    onSetSmartShuffle: (Boolean) -> Unit,
    onSetTimerConfig: (StudyTimerConfig) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetAccentColor: (AccentColorOption) -> Unit,
    srsAlgorithm: String = "FSRS",
    targetRetention: Float = 0.90f,
    onSetSrsAlgorithm: (String) -> Unit = {},
    onSetTargetRetention: (Float) -> Unit = {},
    onSetGeminiApiKey: (String) -> Unit = {},
    onSetGeminiModelVersion: (String) -> Unit = {},
    geminiTelemetry: DeckViewModel.GeminiTelemetryState = DeckViewModel.GeminiTelemetryState(),
    onTestGeminiApiKey: () -> Unit = {},
    onResetGeminiTelemetry: () -> Unit = {},
    onSetSupabaseUrl: (String) -> Unit = {},
    onSetSupabaseKey: (String) -> Unit = {},
    onSetSupabaseAutoSync: (Boolean) -> Unit = {},
    onTestSupabaseConnection: () -> Unit = {},
    onSyncToSupabase: () -> Unit = {},
    onDownloadFromSupabase: () -> Unit = {},
    onOpenImportCsv: () -> Unit,
    onOpenExportCsv: () -> Unit = {},
    onResetData: () -> Unit,
    onResetStudyStats: () -> Unit = {},
    onDeleteAllDecks: () -> Unit = {},
    onClearAllDataZero: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var goalText by remember(dailyGoal) { mutableStateOf(dailyGoal.toString()) }
    var expandedLimitMenu by remember { mutableStateOf(false) }
    var selectedLimit by remember(timerConfig.perCardLimit) { mutableStateOf(timerConfig.perCardLimit) }
    var targetMinutes by remember(timerConfig.targetSessionMinutes) { mutableIntStateOf(timerConfig.targetSessionMinutes) }
    var customMinutesText by remember(timerConfig.targetSessionMinutes) {
        mutableStateOf(if (timerConfig.targetSessionMinutes > 0) timerConfig.targetSessionMinutes.toString() else "")
    }

    var apiKeyInput by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
    var showApiKey by remember { mutableStateOf(false) }

    var supabaseUrlInput by remember(supabaseUrl) { mutableStateOf(supabaseUrl) }
    var supabaseKeyInput by remember(supabaseKey) { mutableStateOf(supabaseKey) }
    var showSupabaseKey by remember { mutableStateOf(false) }

    var expandedModelMenu by remember { mutableStateOf(false) }
    var showResetStatsConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteDecksConfirmDialog by remember { mutableStateOf(false) }
    var showZeroConfirmDialog by remember { mutableStateOf(false) }

    val modelOptions = listOf(
        "gemini-flash-latest" to "Gemini Flash Latest (Recomendado)",
        "gemini-flash-lite-latest" to "Gemini Flash Lite (Ultra Rápido & Alta Disponibilidade)",
        "gemini-3.5-flash" to "Gemini 3.5 Flash",
        "gemini-3.7-flash" to "Gemini 3.7 Flash",
        "gemini-3.8-flash" to "Gemini 3.8 Flash"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Configurações",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Personalização visual, metas e dados do sistema",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("settings_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SuperFlash Hub",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$totalCardsCount Flashcards • $totalDecksCount Baralhos L1",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Meta diária: $dailyGoal cards/dia",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 1. Aparência, Tema Claro/Escuro & Cor de Destaque
            item {
                SettingsSectionCard(
                    title = "Aparência e Personalização",
                    icon = Icons.Default.Palette
                ) {
                    // Theme Mode Selector
                    Text(
                        text = "Modo de Exibição",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            val isSelected = themeMode == mode
                            val cardBg = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            }
                            val contentColor = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSetThemeMode(mode) }
                                    .testTag("chip_theme_${mode.name.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                color = cardBg,
                                border = if (isSelected) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 12.dp, horizontal = 6.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = mode.icon,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Accent Color Palette Selector (Square items, 20+ pre-registered colors)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cor de Destaque do Sistema (50 Cores)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grade Contígua de 50 Cores (5 Linhas de 10 Quadrados Justapostos, Sem Espaços e Sem Nomes)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val allColors = AccentColorOption.entries
                            val rows = allColors.chunked(10)
                            rows.forEach { rowColors ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowColors.forEach { option ->
                                        val isSelected = accentColor == option
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .background(option.previewColor)
                                                .clickable { onSetAccentColor(option) }
                                                .testTag("color_option_${option.name.lowercase()}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Black.copy(alpha = 0.5f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Meta Diária Card
            item {
                SettingsSectionCard(
                    title = "Meta Diária de Estudos",
                    icon = Icons.Default.Flag
                ) {
                    Text(
                        text = "Ajuste o número de cards que deseja revisar por dia para manter sua sequência ativa:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Goal Preset Chips
                    Text(
                        text = "Atalhos Rápidos:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(20, 50, 100, 200, 500).forEach { preset ->
                            val isSelected = goalText == preset.toString()
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    goalText = preset.toString()
                                    onSetDailyGoal(preset)
                                },
                                label = { Text("$preset") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = goalText,
                            onValueChange = { goalText = it.filter { char -> char.isDigit() } },
                            label = { Text("Meta customizada") },
                            suffix = { Text("cards/dia") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_settings_daily_goal")
                        )

                        Button(
                            onClick = {
                                val parsed = goalText.toIntOrNull() ?: dailyGoal
                                if (parsed > 0) {
                                    onSetDailyGoal(parsed)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_save_settings_daily_goal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salvar")
                        }
                    }
                }
            }

            // 3. Algoritmo Smart Shuffle & Temporizador
            item {
                SettingsSectionCard(
                    title = "Sessões & Algoritmo de Priorização",
                    icon = Icons.Default.Tune
                ) {
                    // Smart Shuffle Switch Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Smart Shuffle",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Prioriza cards vencidos e com menor nível de domínio antes de exibir cards novos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = smartShuffleEnabled,
                                onCheckedChange = { onSetSmartShuffle(it) },
                                modifier = Modifier.testTag("switch_settings_smart_shuffle")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Per Card Limit Dropdown
                    Text(
                        text = "Tempo Máximo por Card",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedLimitMenu,
                        onExpandedChange = { expandedLimitMenu = !expandedLimitMenu }
                    ) {
                        OutlinedTextField(
                            value = selectedLimit.label,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLimitMenu) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .testTag("dropdown_settings_per_card_limit")
                        )

                        ExposedDropdownMenu(
                            expanded = expandedLimitMenu,
                            onDismissRequest = { expandedLimitMenu = false }
                        ) {
                            PerCardTimeLimit.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        selectedLimit = option
                                        expandedLimitMenu = false
                                        onSetTimerConfig(
                                            StudyTimerConfig(
                                                perCardLimit = option,
                                                targetSessionMinutes = targetMinutes
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target Session Minutes Presets with FlowRow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Duração Alvo da Sessão",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (targetMinutes > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Ativo: $targetMinutes min",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Sem meta fixa",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val standardPresets = listOf(0, 5, 10, 15, 20, 30, 45, 60)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        standardPresets.forEach { mins ->
                            val isSelected = targetMinutes == mins
                            val labelText = if (mins == 0) "Sem limite" else "$mins min"
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    targetMinutes = mins
                                    customMinutesText = if (mins == 0) "" else mins.toString()
                                    onSetTimerConfig(
                                        StudyTimerConfig(
                                            perCardLimit = selectedLimit,
                                            targetSessionMinutes = mins
                                        )
                                    )
                                },
                                label = { Text(labelText, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        if (targetMinutes > 0 && targetMinutes !in standardPresets) {
                            FilterChip(
                                selected = true,
                                onClick = {},
                                label = { Text("$targetMinutes min (Personalizado)", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = customMinutesText,
                            onValueChange = { input ->
                                customMinutesText = input.filter { it.isDigit() }.take(3)
                            },
                            label = { Text("Duração personalizada") },
                            suffix = { Text("minutos") },
                            placeholder = { Text("Ex: 25") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_settings_custom_minutes")
                        )

                        Button(
                            onClick = {
                                val parsed = customMinutesText.toIntOrNull() ?: 0
                                targetMinutes = parsed
                                onSetTimerConfig(
                                    StudyTimerConfig(
                                        perCardLimit = selectedLimit,
                                        targetSessionMinutes = parsed
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_save_settings_custom_minutes")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salvar")
                        }
                    }
                }
            }

            // 3.1. Algoritmo de Repetição Espaçada (SRS: FSRS-5 vs SM-2)
            item {
                SettingsSectionCard(
                    title = "Algoritmo de Repetição Espaçada (SRS)",
                    icon = Icons.Default.Speed
                ) {
                    Text(
                        text = "Escolha o motor mnemônico de agendamento dos flashcards e a taxa de retenção esperada:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Motor de Agendamento:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val algorithms = listOf(
                            "FSRS" to "FSRS-5 (Recomendado ⚡)",
                            "SM2" to "SM-2 Clássico (Anki 2.0)"
                        )

                        algorithms.forEach { (algoKey, algoLabel) ->
                            val isSelected = srsAlgorithm.equals(algoKey, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSetSrsAlgorithm(algoKey) }
                                    .testTag("chip_srs_algo_${algoKey.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                border = if (isSelected) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = algoLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (srsAlgorithm.equals("FSRS", ignoreCase = true)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ciência da Memória DSR (FSRS-5):",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "O modelo DSR (Dificuldade, Estabilidade e Recuperação) reduz até 30% da carga diária de revisões e elimina o travamento de facilidade (Ease Hell).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Taxa de Retenção Desejada (Target Retention):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val retentionOptions = listOf(
                            0.85f to "85% (Equilibrado)",
                            0.90f to "90% (Padrão Ouro)",
                            0.95f to "95% (Reta Final TCU)"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            retentionOptions.forEach { (retVal, retLabel) ->
                                val isSelected = kotlin.math.abs(targetRetention - retVal) < 0.02f
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSetTargetRetention(retVal) },
                                    label = {
                                        Text(
                                            text = retLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "O SM-2 clássico utiliza multiplicadores rígidos de facilidade (Ease Factor 1.3 a 2.5). Os intervalos crescem por fatores fixos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. Importação e Gerenciamento de Dados
            item {
                SettingsSectionCard(
                    title = "Gerenciamento de Dados e Acervo",
                    icon = Icons.Default.Storage
                ) {
                    // Data Summary Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$totalCardsCount",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Total Flashcards",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$totalDecksCount",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Baralhos L1",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Import & Backup Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenImportCsv,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_settings_import_csv"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importar CSV", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onOpenExportCsv,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_settings_export_csv"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup CSV L1", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reset Sample Data
                    OutlinedButton(
                        onClick = onResetData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_settings_reset_data"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restaurar Dados Iniciais de Exemplo")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Ações Individuais de Gerenciamento (Apagar Decks vs Zerar Estatísticas)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Botão 1: Zerar Apenas Estatísticas
                        Button(
                            onClick = { showResetStatsConfirmDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_settings_reset_stats_only"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                contentColor = Color(0xFFD97706)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Zerar Estatísticas", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Botão 2: Apagar Apenas Decks
                        Button(
                            onClick = { showDeleteDecksConfirmDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_settings_delete_decks_only"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apagar Decks", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 5. Banco de Dados e Sincronização Supabase
            item {
                SettingsSectionCard(
                    title = "Banco de Dados & Nuvem (Supabase)",
                    icon = Icons.Default.CloudUpload
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sincronize baralhos completos, estatísticas de revisões, progresso e configurações no seu banco de dados Supabase.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Indicator Box
                    if (!supabaseSyncState.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = supabaseSyncState,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = "URL do Projeto Supabase:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = supabaseUrlInput,
                        onValueChange = {
                            supabaseUrlInput = it
                            onSetSupabaseUrl(it)
                        },
                        placeholder = { Text("https://xyzcompany.supabase.co") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_settings_supabase_url"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Chave da API Supabase (Anon Key):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = supabaseKeyInput,
                        onValueChange = {
                            supabaseKeyInput = it
                            onSetSupabaseKey(it)
                        },
                        placeholder = { Text("eyJhY2Nlc3NfdG9rZW4i...") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showSupabaseKey = !showSupabaseKey }) {
                                Icon(
                                    imageVector = if (showSupabaseKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Alternar Visibilidade"
                                )
                            }
                        },
                        visualTransformation = if (showSupabaseKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_settings_supabase_key"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTestSupabaseConnection,
                            enabled = !supabaseIsLoading && supabaseUrlInput.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_test_supabase"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Testar Conexão")
                        }

                        Button(
                            onClick = onSyncToSupabase,
                            enabled = !supabaseIsLoading && supabaseUrlInput.isNotBlank() && supabaseKeyInput.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_upload_supabase"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Enviar Dados")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = onDownloadFromSupabase,
                        enabled = !supabaseIsLoading && supabaseUrlInput.isNotBlank() && supabaseKeyInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_download_supabase"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Baixar / Restaurar Nuvem Supabase")
                    }
                }
            }

            // 5. Configuração do Gemini AI (API Key e Modelo)
            item {
                SettingsSectionCard(
                    title = "Inteligência Artificial (Gemini AI)",
                    icon = Icons.Default.AutoAwesome
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "A IA Gemini gera diagnósticos personalizados de retenção e sugestões de estudo na aba Estatísticas/Progresso.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Chave da API do Gemini (API Key):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            onSetGeminiApiKey(it)
                        },
                        placeholder = { Text("Insira sua Gemini API Key (ex: AIzaSy...)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Alternar Visibilidade"
                                )
                            }
                        },
                        visualTransformation = if (showApiKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_settings_gemini_api_key"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Modelo Gemini Preferido:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val selectedModelLabel = modelOptions.find { it.first == geminiModelVersion }?.second ?: geminiModelVersion

                    ExposedDropdownMenuBox(
                        expanded = expandedModelMenu,
                        onExpandedChange = { expandedModelMenu = !expandedModelMenu }
                    ) {
                        OutlinedTextField(
                            value = selectedModelLabel,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModelMenu) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .testTag("dropdown_settings_gemini_model")
                        )

                        ExposedDropdownMenu(
                            expanded = expandedModelMenu,
                            onDismissRequest = { expandedModelMenu = false }
                        ) {
                            modelOptions.forEach { (verCode, verLabel) ->
                                DropdownMenuItem(
                                    text = { Text(verLabel) },
                                    onClick = {
                                        onSetGeminiModelVersion(verCode)
                                        expandedModelMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Painel Completo de Telemetria e Monitor de Quotas da API Key
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_gemini_telemetry_panel"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header do Painel
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Telemetria & Quota da API Key",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                val isCustomKey = apiKeyInput.trim().isNotBlank()
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCustomKey) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = if (isCustomKey) "Chave Própria" else "Chave Padrão",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCustomKey) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 1. Quotas Diárias e Horas para Reset
                            val (hoursUntilReset, minutesUntilReset) = calculateTimeUntilPacificMidnight()
                            val maxDailyRequests = 1500
                            val reqCount = geminiTelemetry.requestsToday
                            val quotaProgress = (reqCount.toFloat() / maxDailyRequests).coerceIn(0f, 1f)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Requisições Diárias (Free Tier):",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "$reqCount / $maxDailyRequests RPD",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    androidx.compose.material3.LinearProgressIndicator(
                                        progress = { quotaProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⏱️ Reseta em: ${hoursUntilReset}h ${minutesUntilReset}m",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "04:00 BRT (00:00 PT)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Consumo de Tokens e Classificação de Uso
                            val tokensToday = geminiTelemetry.tokensToday
                            val usageLevel = when {
                                tokensToday < 50_000 -> Triple("Uso Leve 🟢", "Muito econômico, sem risco de saturação.", Color(0xFF10B981))
                                tokensToday < 200_000 -> Triple("Uso Moderado 🟡", "Consumo normal para estudos diários.", Color(0xFFF59E0B))
                                else -> Triple("Uso Intenso 🔴", "Consumo elevado no dia. Acompanhe a cota.", Color(0xFFEF4444))
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = usageLevel.third.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, usageLevel.third.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Tokens Utilizados Hoje:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = String.format("%,d tokens", tokensToday),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = usageLevel.third
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = usageLevel.third.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = usageLevel.first,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = usageLevel.third,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = usageLevel.second,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 3. Detalhamento da Última Requisição
                            if (geminiTelemetry.lastLatencyMs > 0L) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Última Análise Executada:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Entrada: ${geminiTelemetry.lastPromptTokens} tokens",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Resposta: ${geminiTelemetry.lastResponseTokens} tokens",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Latência: ${geminiTelemetry.lastLatencyMs}ms",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. Teste de Conexão e Feedback
                            if (geminiTelemetry.testResult != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (geminiTelemetry.testResult.startsWith("✅")) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = geminiTelemetry.testResult,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Botões de Ação
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onTestGeminiApiKey,
                                    enabled = !geminiTelemetry.isTestingKey,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (geminiTelemetry.isTestingKey) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Testando...", style = MaterialTheme.typography.labelSmall)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Testar Chave", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                OutlinedButton(
                                    onClick = onResetGeminiTelemetry,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Zerar Quota", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showResetStatsConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetStatsConfirmDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Zerar Estatísticas de Estudo?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Esta ação irá zerar todas as revisões, retenção de aprendizagem, streaks e contagens de estudo.\n\n⚠️ SEUS BARALHOS E FLASHCARDS SERÃO 100% PRESERVADOS INTACTOS. Todos os cartões voltarão ao estado inicial de 'Novos'.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetStatsConfirmDialog = false
                        onResetStudyStats()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sim, Zerar Estatísticas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetStatsConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteDecksConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDecksConfirmDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Apagar Todos os Baralhos?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Esta ação irá excluir permanentemente todos os flashcards e baralhos (L1, L2 e L3) cadastrados no dispositivo. Esta ação é irreversível.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDecksConfirmDialog = false
                        onDeleteAllDecks()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sim, Apagar Baralhos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDecksConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

private fun calculateTimeUntilPacificMidnight(): Pair<Int, Int> {
    val pacificTz = java.util.TimeZone.getTimeZone("America/Los_Angeles")
    val calNow = java.util.Calendar.getInstance(pacificTz)
    val calNext = java.util.Calendar.getInstance(pacificTz).apply {
        add(java.util.Calendar.DAY_OF_YEAR, 1)
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val diffMillis = (calNext.timeInMillis - calNow.timeInMillis).coerceAtLeast(0L)
    val hours = (diffMillis / (1000 * 60 * 60)).toInt()
    val minutes = ((diffMillis / (1000 * 60)) % 60).toInt()
    return Pair(hours, minutes)
}

