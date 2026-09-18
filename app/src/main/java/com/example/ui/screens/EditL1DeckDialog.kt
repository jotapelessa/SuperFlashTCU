package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.L1DeckSummary
import com.example.ui.components.DisciplinePalette

data class CoverPreset(
    val title: String,
    val url: String
)

val PRESET_COVERS = listOf(
    CoverPreset("Direito / Jurídico", "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=800&q=80"),
    CoverPreset("Medicina / Saúde", "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=800&q=80"),
    CoverPreset("Tecnologia & Exatas", "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80"),
    CoverPreset("Humanas & Leitura", "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?auto=format&fit=crop&w=800&q=80"),
    CoverPreset("Estudos & Workspace", "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?auto=format&fit=crop&w=800&q=80"),
    CoverPreset("Café & Foco", "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=800&q=80")
)

val PRESET_COLORS = listOf(
    "#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6",
    "#EC4899", "#06B6D4", "#64748B", "#1E293B", "#D97706"
)

@Composable
fun EditL1DeckDialog(
    deck: L1DeckSummary,
    onConfirmSave: (oldL1Name: String, newL1Name: String, cardColorHex: String?, courseName: String?, coverUrl: String?, isAiEnabled: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var l1NameInput by remember { mutableStateOf(deck.l1) }
    var courseNameInput by remember { mutableStateOf(deck.courseName ?: "") }
    var selectedColorHex by remember {
        mutableStateOf(deck.cardColorHex ?: DisciplinePalette.getColorForDiscipline(deck.l1))
    }
    var coverUrlInput by remember { mutableStateOf(deck.coverUrl ?: "") }
    var isAiEnabledInput by remember { mutableStateOf(deck.isAiAnalysisEnabled) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUploadingImage by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingImage = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val coversDir = File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
                    val destFile = File(coversDir, "deck_l1_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        coverUrlInput = destFile.absolutePath
                        isUploadingImage = false
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        coverUrlInput = uri.toString()
                        isUploadingImage = false
                    }
                }
            }
        }
    }

    val parsedColor = remember(selectedColorHex) { DisciplinePalette.parseColor(selectedColorHex) }
    val isCoverActive = coverUrlInput.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Editar Baralho L1",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Personalize o nome, cursinho, cor e cover do card",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_edit_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preview Card Section
                    Text(
                        text = "Pré-visualização do Card L1:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            if (isCoverActive) {
                                AsyncImage(
                                    model = coverUrlInput,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Dark Gradient Scrim overlay for high contrast legibility
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.50f),
                                                    Color.Black.copy(alpha = 0.82f)
                                                )
                                            )
                                        )
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    parsedColor.copy(alpha = 0.15f),
                                                    parsedColor.copy(alpha = 0.30f)
                                                )
                                            )
                                        )
                                )
                            }

                            // Content inside Card Preview
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCoverActive) Color.Black.copy(alpha = 0.4f) else parsedColor.copy(alpha = 0.2f),
                                        border = if (isCoverActive) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) else null
                                    ) {
                                        Text(
                                            text = "L1 BARALHO",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                shadow = if (isCoverActive) Shadow(Color.Black, Offset(2f, 2f), 6f) else null
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCoverActive) Color.White else parsedColor
                                        )
                                    }

                                    if (courseNameInput.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = parsedColor,
                                            shadowElevation = 2.dp
                                        ) {
                                            Text(
                                                text = courseNameInput,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = l1NameInput.ifBlank { "Nome do Baralho L1" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 18.sp,
                                            shadow = if (isCoverActive) Shadow(Color.Black, Offset(2f, 2f), 8f) else null
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCoverActive) Color.White else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${deck.l2Count} matérias (L2) • ${deck.totalCards} cards",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            shadow = if (isCoverActive) Shadow(Color.Black, Offset(1f, 1f), 4f) else null
                                        ),
                                        color = if (isCoverActive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 1. Rename Input Field
                    OutlinedTextField(
                        value = l1NameInput,
                        onValueChange = { l1NameInput = it },
                        label = { Text("Nome do Baralho L1") },
                        placeholder = { Text("Ex: Direito Constitucional") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_l1_name")
                    )

                    // 2. Course Name / Cursinho Input
                    OutlinedTextField(
                        value = courseNameInput,
                        onValueChange = { courseNameInput = it },
                        label = { Text("Nome do Cursinho / Professor / Categoria") },
                        placeholder = { Text("Ex: Estratégia, Gran Cursos, Prova OAB") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_l1_course")
                    )

                    // 3. Card Accent Color Palette Picker (50 Cores em Grade Contígua 5x10)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = parsedColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Cor de Destaque do Card (50 cores):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        ) {
                            DisciplinePalette.selectableColors.chunked(10).forEach { rowColors ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    rowColors.forEach { hex ->
                                        val color = DisciplinePalette.parseColor(hex)
                                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .background(color)
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 0.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent
                                                )
                                                .clickable { selectedColorHex = hex },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Cover Image Presets, Upload da Galeria & Custom URL Input
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Imagem Cover do Card:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (coverUrlInput.isNotBlank()) {
                                TextButton(
                                    onClick = { coverUrlInput = "" },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remover Cover", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Botão para Upload da Galeria do Smartphone (Item 3.6)
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUploadingImage) "Importando da galeria..." else "Escolher Imagem da Galeria do Smartphone",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        // Presets Row
                        Text(
                            text = "Ou selecione uma imagem de capa pronta:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(PRESET_COVERS) { preset ->
                                val isSelected = coverUrlInput == preset.url

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .height(80.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { coverUrlInput = preset.url }
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = preset.url,
                                            contentDescription = preset.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                                    )
                                                )
                                        )
                                        Text(
                                            text = preset.title,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(6.dp),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }

                        // Custom URL Input
                        OutlinedTextField(
                            value = coverUrlInput,
                            onValueChange = { coverUrlInput = it },
                            label = { Text("Link da Imagem Cover Personalizada") },
                            placeholder = { Text("https://exemplo.com/imagem.jpg") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_edit_l1_cover_url")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Seção de Ativação / Desativação da Análise da IA
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAiEnabledInput) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = if (isAiEnabledInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Análise do Tutor AI Gemini",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isAiEnabledInput)
                                            "Ativo: O Gemini avaliará este baralho nos diagnósticos e planos de estudo."
                                        else
                                            "Pausado: As estatísticas deste baralho serão ignoradas pela IA.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = isAiEnabledInput,
                                    onCheckedChange = { isAiEnabledInput = it },
                                    modifier = Modifier.testTag("switch_l1_ai_analysis")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Actions
                val isValid = l1NameInput.isNotBlank()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (isValid) {
                                onConfirmSave(
                                    deck.l1,
                                    l1NameInput.trim(),
                                    selectedColorHex,
                                    courseNameInput.trim().ifBlank { null },
                                    coverUrlInput.trim().ifBlank { null },
                                    isAiEnabledInput
                                )
                            }
                        },
                        enabled = isValid,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_save_l1_edit")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salvar Alterações")
                    }
                }
            }
        }
    }
}
