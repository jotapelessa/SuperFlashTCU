package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.importer.CsvParseResult
import com.example.data.importer.SampleData
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun ImportDialog(
    isImporting: Boolean,
    importResult: CsvParseResult?,
    onImportCsv: (String) -> Unit,
    onDismissResult: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var csvText by remember { mutableStateOf("") }
    var showPasteField by remember { mutableStateOf(false) }

    var showMergeSection by remember { mutableStateOf(false) }
    var table1Text by remember { mutableStateOf("") }
    var table2Text by remember { mutableStateOf("") }
    var mergeValidationResult by remember { mutableStateOf<CsvMergeValidationResult?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
                    val content = reader.readText()
                    onImportCsv(content)
                }
            } catch (e: Exception) {
                // Handled in ViewModel
            }
        }
    }

    val table1PickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
                    table1Text = reader.readText()
                    mergeValidationResult = null
                }
            } catch (_: Exception) {}
        }
    }

    val table2PickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
                    table2Text = reader.readText()
                    mergeValidationResult = null
                }
            } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Importador Anki CSV (L1::L2::L3)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (isImporting) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Processando CSV, detectando delimitador e UTF-8...")
                        }
                    }
                } else if (importResult != null) {
                    // Result Summary
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val report = importResult.preImportReport
                        val isFullyDuplicate = report?.isFullyDuplicateDeck == true
                        val hasNewCards = importResult.validCards.isNotEmpty()

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                hasNewCards -> Color(0xFF10B981).copy(alpha = 0.15f)
                                isFullyDuplicate -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.errorContainer
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when {
                                        hasNewCards -> Icons.Default.CheckCircle
                                        isFullyDuplicate -> Icons.Default.CheckCircle
                                        else -> Icons.Default.Error
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        hasNewCards -> Color(0xFF059669)
                                        isFullyDuplicate -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = when {
                                            hasNewCards -> "Importação Concluída com Sucesso!"
                                            isFullyDuplicate -> "Baralho Já Existente Sincronizado"
                                            else -> "Nenhum Card Novo Importado"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            hasNewCards -> Color(0xFF059669)
                                            isFullyDuplicate -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.error
                                        }
                                    )
                                    Text(
                                        text = "${importResult.validCards.size} cards novos inseridos • ${importResult.skippedDuplicates} flashcards redundantes evitados",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        // Pre-Import Check Validation Card
                        if (report != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Validação Pré-Importação no Banco de Dados:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "• Registros analisados: ${report.totalIncomingRecords} (${report.validRecordsParsed} válidos)\n" +
                                                "• Flashcards redundantes ignorados: ${report.redundantCardsSkipped}\n" +
                                                "• Pastas L1 existentes reutilizadas: ${report.matchedExistingL1Folders.size} (Novas criadas: ${report.newL1FoldersToCreate.size})\n" +
                                                "• Pastas L2 existentes reutilizadas: ${report.matchedExistingL2Folders.size} (Novas criadas: ${report.newL2FoldersToCreate.size})\n" +
                                                "• Pastas L3 existentes reutilizadas: ${report.matchedExistingL3Folders.size} (Novas criadas: ${report.newL3FoldersToCreate.size})" +
                                                if (report.folderNormalizationsApplied > 0) "\n• Normalizações de pastas aplicadas: ${report.folderNormalizationsApplied}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (importResult.errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Avisos / Linhas Inválidas (${importResult.errors.size}):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(importResult.errors) { err ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "Linha ${err.rowNumber}: ${err.reason}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            if (err.rawPreview.isNotBlank()) {
                                                Text(
                                                    text = err.rawPreview,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onDismissResult()
                                onClose()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Fechar")
                        }
                    }
                } else {
                    // Actions: File picker, Paste text, or Load Sample
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Regras do Importador Inteligente:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "• Separa campo Deck por '::' (L1::L2::L3)\n" +
                                                "• L1 = Baralho geral (Home)\n" +
                                                "• L2 = Matéria / Disciplina com código de cores\n" +
                                                "• L3 = Tópico / Assunto específico\n" +
                                                "• Preserva HTML original e formatações completas\n" +
                                                "• Detecta UTF-8, UTF-8 BOM e delimitadores (,, ;, tab)\n" +
                                                "• Deduplicação inteligente de flashcards",
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = { filePickerLauncher.launch("*/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_select_csv_file"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Selecionar Arquivo CSV / TXT", fontWeight = FontWeight.Bold)
                            }
                        }

                        item {
                            OutlinedButton(
                                onClick = { onImportCsv(SampleData.sampleCsv) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_load_sample_csv"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Carregar Dados de Exemplo do TCU / TRF")
                            }
                        }

                        item {
                            OutlinedButton(
                                onClick = { showPasteField = !showPasteField },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (showPasteField) "Ocultar Campo de Texto" else "Colar Texto CSV Manualmente")
                            }
                        }

                        if (showPasteField) {
                            item {
                                Column {
                                    OutlinedTextField(
                                        value = csvText,
                                        onValueChange = { csvText = it },
                                        placeholder = { Text("Cole aqui o conteúdo CSV exportado do Anki...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (csvText.isNotBlank()) {
                                                onImportCsv(csvText)
                                            }
                                        },
                                        enabled = csvText.isNotBlank(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Processar Texto Colado")
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedButton(
                                onClick = { showMergeSection = !showMergeSection },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_toggle_csv_merge"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.tertiary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CallMerge, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (showMergeSection) "Ocultar Unificação de CSVs" else "Emergir / Unir 2 Tabelas CSV (Mesmo Formato)")
                            }
                        }

                        if (showMergeSection) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Emergência / Unificação de 2 Tabelas CSV:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Selecione ou cole duas tabelas CSV. O sistema verificará se ambas possuem a mesma quantidade e estrutura de colunas antes de uni-las.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Tabela 1
                                        Text(
                                            text = "Tabela CSV 1:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { table1PickerLauncher.launch("*/*") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Arquivo 1")
                                            }
                                        }
                                        OutlinedTextField(
                                            value = table1Text,
                                            onValueChange = {
                                                table1Text = it
                                                mergeValidationResult = null
                                            },
                                            placeholder = { Text("Conteúdo do CSV 1...") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Tabela 2
                                        Text(
                                            text = "Tabela CSV 2:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { table2PickerLauncher.launch("*/*") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Arquivo 2")
                                            }
                                        }
                                        OutlinedTextField(
                                            value = table2Text,
                                            onValueChange = {
                                                table2Text = it
                                                mergeValidationResult = null
                                            },
                                            placeholder = { Text("Conteúdo do CSV 2...") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Validation Error or Warning Card
                                        val res = mergeValidationResult
                                        if (res != null) {
                                            if (!res.isCompatible) {
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Text(
                                                            text = "⚠️ ALERTA DE INCOMPATIBILIDADE DETECTADO",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onErrorContainer
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = res.errorMessage ?: "As tabelas não possuem a mesma estrutura de colunas.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onErrorContainer
                                                        )
                                                    }
                                                }
                                            } else {
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Text(
                                                            text = "✅ TABELAS COMPATÍVEIS!",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Ambas as tabelas possuem ${res.colsCount1} colunas. Pronto para emergir e importar.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }

                                        Button(
                                            onClick = {
                                                val validation = validateAndMergeCsvs(table1Text, table2Text)
                                                mergeValidationResult = validation
                                                if (validation.isCompatible && !validation.mergedCsv.isNullOrBlank()) {
                                                    onImportCsv(validation.mergedCsv)
                                                }
                                            },
                                            enabled = table1Text.isNotBlank() && table2Text.isNotBlank(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(46.dp),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Validar e Emergir 2 Tabelas CSV")
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
}

data class CsvMergeValidationResult(
    val isCompatible: Boolean,
    val colsCount1: Int,
    val colsCount2: Int,
    val errorMessage: String? = null,
    val mergedCsv: String? = null
)

fun validateAndMergeCsvs(csv1: String, csv2: String): CsvMergeValidationResult {
    val lines1 = csv1.lines().filter { it.trim().isNotBlank() }
    val lines2 = csv2.lines().filter { it.trim().isNotBlank() }

    if (lines1.isEmpty() || lines2.isEmpty()) {
        return CsvMergeValidationResult(
            isCompatible = false,
            colsCount1 = 0,
            colsCount2 = 0,
            errorMessage = "Ambas as tabelas CSV precisam conter dados válidos."
        )
    }

    fun getColumnCount(line: String): Int {
        val delimiters = listOf('\t', ';', ',')
        val bestDelim = delimiters.maxByOrNull { d -> line.count { it == d } } ?: ','
        return line.split(bestDelim).size
    }

    val sampleLine1 = lines1.firstOrNull { !it.startsWith("#") } ?: lines1.first()
    val sampleLine2 = lines2.firstOrNull { !it.startsWith("#") } ?: lines2.first()

    val count1 = getColumnCount(sampleLine1)
    val count2 = getColumnCount(sampleLine2)

    if (count1 != count2) {
        return CsvMergeValidationResult(
            isCompatible = false,
            colsCount1 = count1,
            colsCount2 = count2,
            errorMessage = "Formato Incompatível: Tabela 1 possui $count1 colunas e Tabela 2 possui $count2 colunas. Ambas devem ter exatamente a mesma estrutura!"
        )
    }

    val mergedList = mutableListOf<String>()
    mergedList.addAll(lines1)

    val startIdx = if (lines1.isNotEmpty() && lines2.isNotEmpty() && lines1.first().trim() == lines2.first().trim()) 1 else 0
    for (i in startIdx until lines2.size) {
        mergedList.add(lines2[i])
    }

    return CsvMergeValidationResult(
        isCompatible = true,
        colsCount1 = count1,
        colsCount2 = count2,
        mergedCsv = mergedList.joinToString("\n")
    )
}
