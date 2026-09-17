package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.HierarchyFolderTuple
import com.example.data.model.FlashcardEntity
import com.example.ui.components.DisciplinePalette
import com.example.ui.components.HtmlText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorDialog(
    card: FlashcardEntity,
    existingFolders: List<HierarchyFolderTuple>,
    availableTags: List<String> = emptyList(),
    onSave: (FlashcardEntity) -> Unit,
    onDelete: (FlashcardEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val isNewCard = card.id == 0L

    var l1 by remember { mutableStateOf(card.l1) }
    var l2 by remember { mutableStateOf(card.l2) }
    var l3 by remember { mutableStateOf(card.l3) }
    var frontHtml by remember { mutableStateOf(card.frontHtml) }
    var backHtml by remember { mutableStateOf(card.backHtml) }
    var tags by remember { mutableStateOf(card.tags) }
    var newTagInput by remember { mutableStateOf("") }

    val activeTagList = remember(tags) {
        if (tags.isBlank()) emptyList()
        else tags.split(Regex("[,\\s]+"))
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
            .distinct()
    }

    var selectedPreviewTab by remember { mutableIntStateOf(0) } // 0 = Edição, 1 = Pré-visualização
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Dropdown suggestions
    val l1Suggestions = remember(existingFolders) {
        existingFolders.map { it.l1 }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val l2Suggestions = remember(existingFolders, l1) {
        existingFolders.filter { it.l1.equals(l1, ignoreCase = true) }
            .map { it.l2 }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val l3Suggestions = remember(existingFolders, l1, l2) {
        existingFolders.filter { it.l1.equals(l1, ignoreCase = true) && it.l2.equals(l2, ignoreCase = true) }
            .map { it.l3 }.filter { it.isNotBlank() }.distinct().sorted()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("card_editor_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isNewCard) "Novo Flashcard" else "Editar Flashcard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isNewCard) "Defina a hierarquia L1/L2/L3 e os conteúdos" else "Atualize os campos e a classificação hierárquica",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_card_editor")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Section: Hierarchy L1 / L2 / L3
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Hierarquia do Baralho",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // L1 Field with suggestions
                            HierarchyInputField(
                                label = "L1 • Baralho Principal",
                                value = l1,
                                onValueChange = { l1 = it },
                                suggestions = l1Suggestions,
                                placeholder = "Ex: Auditor TCU/TCEs/TCDF - PARTE 1",
                                testTag = "input_card_l1"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // L2 Field with suggestions
                            HierarchyInputField(
                                label = "L2 • Matéria / Disciplina",
                                value = l2,
                                onValueChange = { l2 = it },
                                suggestions = l2Suggestions,
                                placeholder = "Ex: Direito Constitucional",
                                testTag = "input_card_l2"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // L3 Field with suggestions
                            HierarchyInputField(
                                label = "L3 • Assunto / Subtópico",
                                value = l3,
                                onValueChange = { l3 = it },
                                suggestions = l3Suggestions,
                                placeholder = "Ex: Controle de Constitucionalidade",
                                testTag = "input_card_l3"
                            )
                        }
                    }

                    // Mode Toggle: Editor vs HTML Preview
                    PrimaryTabRow(
                        selectedTabIndex = selectedPreviewTab,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Tab(
                            selected = selectedPreviewTab == 0,
                            onClick = { selectedPreviewTab = 0 },
                            text = { Text("Editor", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("tab_editor_input")
                        )
                        Tab(
                            selected = selectedPreviewTab == 1,
                            onClick = { selectedPreviewTab = 1 },
                            text = { Text("Pré-Visualização", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("tab_editor_preview")
                        )
                    }

                    if (selectedPreviewTab == 0) {
                        // FRONT QUESTION EDITOR
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Frente (Pergunta / Enunciado)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                // Quick HTML insertion buttons
                                QuickHtmlTagToolbar(
                                    onInsertTag = { prefix, suffix ->
                                        frontHtml = "$frontHtml$prefix$suffix"
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = frontHtml,
                                onValueChange = { frontHtml = it },
                                placeholder = { Text("Digite a pergunta do flashcard (suporta HTML)...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .testTag("input_card_front"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                        }

                        // BACK ANSWER & JUSTIFICATION EDITOR
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Verso (Resposta & Fundamentação)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                QuickHtmlTagToolbar(
                                    onInsertTag = { prefix, suffix ->
                                        backHtml = "$backHtml$prefix$suffix"
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = backHtml,
                                onValueChange = { backHtml = it },
                                placeholder = { Text("Digite a resposta detalhada e fundamentação jurídica/técnica...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .testTag("input_card_back"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                        }

                        // TAG MANAGEMENT SECTION
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tag_management_section")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Gerenciamento de Tags (Cross-domain)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (activeTagList.isNotEmpty()) {
                                        Text(
                                            text = "${activeTagList.size} tag(s)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Active Tags Chip Row
                                if (activeTagList.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        activeTagList.forEach { tagItem ->
                                            InputChip(
                                                selected = true,
                                                onClick = {
                                                    val updated = activeTagList.filter { it != tagItem }
                                                    tags = updated.joinToString(" ")
                                                },
                                                label = { Text("#$tagItem", fontWeight = FontWeight.Medium) },
                                                trailingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remover tag",
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                },
                                                colors = InputChipDefaults.inputChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                modifier = Modifier.testTag("tag_chip_$tagItem")
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // Input row to add new tag
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newTagInput,
                                        onValueChange = { input ->
                                            if (input.endsWith(",") || input.endsWith(" ") || input.endsWith("\n")) {
                                                val clean = input.trim().removeSuffix(",").removePrefix("#").trim()
                                                if (clean.isNotBlank()) {
                                                    val updated = (activeTagList + clean).distinct()
                                                    tags = updated.joinToString(" ")
                                                    newTagInput = ""
                                                }
                                            } else {
                                                newTagInput = input
                                            }
                                        },
                                        placeholder = { Text("Nova tag... (ex: prazos)") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_new_tag"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    FilledTonalButton(
                                        onClick = {
                                            val clean = newTagInput.trim().removePrefix("#")
                                            if (clean.isNotBlank()) {
                                                val updated = (activeTagList + clean).distinct()
                                                tags = updated.joinToString(" ")
                                                newTagInput = ""
                                            }
                                        },
                                        enabled = newTagInput.isNotBlank(),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("btn_add_tag")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add")
                                    }
                                }

                                // Suggested tags row from other decks
                                val unassignedSuggestions = remember(availableTags, activeTagList) {
                                    availableTags.filter { it.isNotBlank() && it !in activeTagList }
                                }
                                if (unassignedSuggestions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tags disponíveis no acervo (toque para adicionar):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        unassignedSuggestions.take(12).forEach { suggestion ->
                                            AssistChip(
                                                onClick = {
                                                    val updated = (activeTagList + suggestion).distinct()
                                                    tags = updated.joinToString(" ")
                                                },
                                                label = { Text("+$suggestion", style = MaterialTheme.typography.labelSmall) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                ),
                                                modifier = Modifier.testTag("tag_suggestion_$suggestion")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // LIVE PREVIEW TAB
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "FRENTE DO FLASHCARD:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (frontHtml.isBlank()) {
                                    Text(
                                        text = "(Nenhum conteúdo digitado na frente)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    HtmlText(
                                        html = frontHtml,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "VERSO (RESPOSTA):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (backHtml.isBlank()) {
                                    Text(
                                        text = "(Nenhum conteúdo digitado no verso)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    HtmlText(
                                        html = backHtml,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Validation Error Message
                    if (validationError != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = validationError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isNewCard) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.testTag("btn_delete_card")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excluir")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_cancel_edit_card")
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                if (l1.isBlank()) {
                                    validationError = "Informe o Baralho Principal (L1)."
                                    return@Button
                                }
                                if (l2.isBlank()) {
                                    validationError = "Informe a Matéria/Disciplina (L2)."
                                    return@Button
                                }
                                if (frontHtml.isBlank()) {
                                    validationError = "A frente da pergunta não pode estar vazia."
                                    return@Button
                                }
                                if (backHtml.isBlank()) {
                                    validationError = "A resposta do flashcard não pode estar vazia."
                                    return@Button
                                }

                                validationError = null
                                val cleanL1 = l1.trim()
                                val cleanL2 = l2.trim()
                                val cleanL3 = l3.trim().ifBlank { "Tópico Geral" }

                                val updated = card.copy(
                                    l1 = cleanL1,
                                    l2 = cleanL2,
                                    l3 = cleanL3,
                                    deckRaw = "$cleanL1 :: $cleanL2 :: $cleanL3",
                                    frontHtml = frontHtml.trim(),
                                    backHtml = backHtml.trim(),
                                    tags = tags.trim()
                                )
                                onSave(updated)
                            },
                            modifier = Modifier.testTag("btn_save_card")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isNewCard) "Criar Flashcard" else "Salvar Alterações", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir este flashcard?") },
            text = { Text("Esta ação removerá o flashcard permanentemente da base de dados.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(card)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_delete_card")
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HierarchyInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    placeholder: String,
    testTag: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .testTag(testTag),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    if (suggestions.isNotEmpty()) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )

            if (suggestions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    suggestions.take(8).forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                onValueChange(suggestion)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Quick suggestions chips below if user hasn't typed anything
        if (suggestions.isNotEmpty() && value.isBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.take(4).forEach { suggestion ->
                    FilterChip(
                        selected = false,
                        onClick = { onValueChange(suggestion) },
                        label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickHtmlTagToolbar(
    onInsertTag: (String, String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.clickable { onInsertTag("<b>", "</b>") }
        ) {
            Text(
                text = "<b>B</b>",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.clickable { onInsertTag("<i>", "</i>") }
        ) {
            Text(
                text = "<i>I</i>",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFEF4444).copy(alpha = 0.15f),
            modifier = Modifier.clickable { onInsertTag("<span style=\"color: rgb(255, 0, 0);\">", "</span>") }
        ) {
            Text(
                text = "Cor",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.clickable { onInsertTag("<br>", "") }
        ) {
            Text(
                text = "Quebra",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
