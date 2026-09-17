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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onSetGeminiApiKey: (String) -> Unit = {},
    onSetGeminiModelVersion: (String) -> Unit = {},
    onSetSupabaseUrl: (String) -> Unit = {},
    onSetSupabaseKey: (String) -> Unit = {},
    onSetSupabaseAutoSync: (Boolean) -> Unit = {},
    onTestSupabaseConnection: () -> Unit = {},
    onSyncToSupabase: () -> Unit = {},
    onDownloadFromSupabase: () -> Unit = {},
    onOpenImportCsv: () -> Unit,
    onOpenExportCsv: () -> Unit = {},
    onResetData: () -> Unit,
    onClearAllDataZero: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var goalText by remember(dailyGoal) { mutableStateOf(dailyGoal.toString()) }
    var expandedLimitMenu by remember { mutableStateOf(false) }
    var selectedLimit by remember(timerConfig.perCardLimit) { mutableStateOf(timerConfig.perCardLimit) }
    var targetMinutes by remember(timerConfig.targetSessionMinutes) { mutableIntStateOf(timerConfig.targetSessionMinutes) }

    var apiKeyInput by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
    var showApiKey by remember { mutableStateOf(false) }

    var supabaseUrlInput by remember(supabaseUrl) { mutableStateOf(supabaseUrl) }
    var supabaseKeyInput by remember(supabaseKey) { mutableStateOf(supabaseKey) }
    var showSupabaseKey by remember { mutableStateOf(false) }

    var expandedModelMenu by remember { mutableStateOf(false) }
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
                            text = "Cor de Destaque do Sistema (Paleta Quadrada - 22 Cores)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AccentColorOption.entries.forEach { option ->
                            val isSelected = accentColor == option
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSetAccentColor(option) }
                                    .padding(2.dp)
                                    .testTag("color_option_${option.name.lowercase()}")
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = option.previewColor,
                                    border = if (isSelected) {
                                        BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                                    } else {
                                        BorderStroke(1.dp, Color.Black.copy(alpha = 0.15f))
                                    },
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    fontSize = 10.sp
                                )
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
                    Text(
                        text = "Duração Alvo da Sessão (Minutos)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(5, 10, 15, 20, 30, 45, 60).forEach { mins ->
                            val isSelected = targetMinutes == mins
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    targetMinutes = mins
                                    onSetTimerConfig(
                                        StudyTimerConfig(
                                            perCardLimit = selectedLimit,
                                            targetSessionMinutes = mins
                                        )
                                    )
                                },
                                label = { Text("$mins min", fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    var customMinutesText by remember(targetMinutes) { mutableStateOf(targetMinutes.toString()) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = customMinutesText,
                            onValueChange = { customMinutesText = it.filter { char -> char.isDigit() } },
                            label = { Text("Duração customizada") },
                            suffix = { Text("minutos") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_settings_custom_minutes")
                        )

                        Button(
                            onClick = {
                                val parsed = customMinutesText.toIntOrNull() ?: targetMinutes
                                if (parsed > 0) {
                                    targetMinutes = parsed
                                    onSetTimerConfig(
                                        StudyTimerConfig(
                                            perCardLimit = selectedLimit,
                                            targetSessionMinutes = parsed
                                        )
                                    )
                                }
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

                    // Zero / Delete All
                    Button(
                        onClick = { showZeroConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_settings_clear_all_zero"),
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zerar Aplicação (Apagar Decks e Estatísticas)", fontWeight = FontWeight.Bold)
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
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showZeroConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showZeroConfirmDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Apagar Todos os Dados?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Esta ação irá excluir permanentemente todos os decks, flashcards, histórico de revisões e estatísticas de progresso. O aplicativo ficará zerado e pronto para importar novos dados CSV.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showZeroConfirmDialog = false
                        onClearAllDataZero()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sim, Zerar Aplicação")
                }
            },
            dismissButton = {
                TextButton(onClick = { showZeroConfirmDialog = false }) {
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
