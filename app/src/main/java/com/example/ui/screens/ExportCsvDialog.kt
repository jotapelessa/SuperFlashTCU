package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.L1DeckSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExportCsvDialog(
    l1Decks: List<L1DeckSummary>,
    initialL1: String? = null,
    onGenerateCsv: (l1Target: String?, onResult: (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedL1Target by remember {
        mutableStateOf(initialL1 ?: "TODOS")
    }
    var expandedDropdown by remember { mutableStateOf(false) }

    var csvResultContent by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val cleanDeckName = remember(selectedL1Target) {
        if (selectedL1Target == "TODOS") "todos_baralhos" else selectedL1Target.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
    }

    val defaultFileName = remember(cleanDeckName) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        "superflash_${cleanDeckName}_$dateStr.csv"
    }

    // Storage Access Framework launcher to save real .csv file directly to storage
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(csvResultContent.toByteArray(Charsets.UTF_8))
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Arquivo .csv salvo com sucesso!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Erro ao salvar arquivo CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Share physical .csv file via FileProvider
    fun shareCsvFile() {
        scope.launch(Dispatchers.IO) {
            try {
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val file = File(exportDir, defaultFileName)
                file.writeText(csvResultContent, Charsets.UTF_8)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Backup Anki CSV - $selectedL1Target")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                withContext(Dispatchers.Main) {
                    context.startActivity(Intent.createChooser(intent, "Salvar/Compartilhar Arquivo .CSV"))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erro ao compartilhar arquivo CSV: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(selectedL1Target) {
        isGenerating = true
        onGenerateCsv(if (selectedL1Target == "TODOS") null else selectedL1Target) { csv ->
            csvResultContent = csv
            isGenerating = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Exportação de Arquivo .CSV",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Gere e salve um arquivo .csv real no seu dispositivo, totalmente compatível com Anki e planilhas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selector for Deck L1
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (selectedL1Target == "TODOS") "Todos os Baralhos L1 (Backup Completo)" else selectedL1Target,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecione o Baralho para Exportar") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos os Baralhos L1 (Backup Completo)", fontWeight = FontWeight.Bold) },
                            onClick = {
                                selectedL1Target = "TODOS"
                                expandedDropdown = false
                            }
                        )
                        l1Decks.forEach { summary ->
                            DropdownMenuItem(
                                text = { Text(summary.l1) },
                                onClick = {
                                    selectedL1Target = summary.l1
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val linesCount = remember(csvResultContent) {
                    csvResultContent.lineSequence().filter { it.isNotBlank() }.count()
                }

                Text(
                    text = "Prévia do Arquivo .CSV ($linesCount linhas):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 160.dp)
                            .padding(8.dp)
                    ) {
                        if (isGenerating) {
                            Text(
                                text = "Gerando arquivo CSV...",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn {
                                item {
                                    Text(
                                        text = csvResultContent.take(2500) + if (csvResultContent.length > 2500) "\n... [conteúdo truncado para prévia]" else "",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Primary: Save real .csv file via Storage Access Framework
                Button(
                    onClick = { saveFileLauncher.launch(defaultFileName) },
                    enabled = csvResultContent.isNotBlank() && !isGenerating,
                    modifier = Modifier.testTag("btn_save_csv_file")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salvar .CSV")
                }

                // Secondary: Share physical .csv file via FileProvider
                OutlinedButton(
                    onClick = { shareCsvFile() },
                    enabled = csvResultContent.isNotBlank() && !isGenerating,
                    modifier = Modifier.testTag("btn_share_csv_file")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartilhar .CSV")
                }

                // Tertiary: Copy text to clipboard
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Anki CSV", csvResultContent)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Conteúdo CSV copiado!", Toast.LENGTH_SHORT).show()
                    },
                    enabled = csvResultContent.isNotBlank() && !isGenerating,
                    modifier = Modifier.testTag("btn_copy_csv")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar Texto")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
