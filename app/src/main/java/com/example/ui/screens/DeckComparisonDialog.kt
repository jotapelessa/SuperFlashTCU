package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FlashcardEntity
import com.example.ui.components.DisciplinePalette

/**
 * Normalizes text by removing HTML tags, entities, and whitespace noise
 * to accurately detect identical flashcard contents across different decks.
 */
private fun normalizeContent(text: String): String {
    return text
        .replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&quot;", "\"")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(Regex("\\s+"), " ")
        .trim()
        .lowercase()
}

/**
 * Generates a normalized content key for a flashcard based on its front and back text.
 */
private fun getCardFingerprint(card: FlashcardEntity): String {
    val cleanFront = normalizeContent(card.frontHtml)
    val cleanBack = normalizeContent(card.backHtml)
    return if (cleanFront.isNotBlank() || cleanBack.isNotBlank()) {
        "$cleanFront|||$cleanBack"
    } else {
        "${card.frontHtml.trim().lowercase()}|||${card.backHtml.trim().lowercase()}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckComparisonDialog(
    allCards: List<FlashcardEntity>,
    initialL1: String? = null,
    initialL2: String? = null,
    onDeleteL2Discipline: ((l1: String, l2: String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var showDeleteADialog by remember { mutableStateOf(false) }
    var showDeleteBDialog by remember { mutableStateOf(false) }
    val l1L2Pairs = remember(allCards) {
        allCards.map { Pair(it.l1, it.l2) }.distinct().sortedBy { "${it.first} :: ${it.second}" }
    }

    var selectedPairA by remember {
        val initial = if (initialL1 != null && initialL2 != null) Pair(initialL1, initialL2) else null
        mutableStateOf(initial ?: l1L2Pairs.firstOrNull() ?: Pair("Geral", "Geral"))
    }

    var selectedPairB by remember {
        val fallbackB = l1L2Pairs.firstOrNull { it != selectedPairA } ?: selectedPairA
        mutableStateOf(fallbackB)
    }

    var expandedDropdownA by remember { mutableStateOf(false) }
    var expandedDropdownB by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Cards in Deck A and Deck B
    val cardsA = remember(allCards, selectedPairA) {
        allCards.filter { it.l1 == selectedPairA.first && it.l2 == selectedPairA.second }
    }
    val cardsB = remember(allCards, selectedPairB) {
        allCards.filter { it.l1 == selectedPairB.first && it.l2 == selectedPairB.second }
    }

    // Map cards to normalized fingerprints
    val fingerprintsA = remember(cardsA) {
        cardsA.associateBy { getCardFingerprint(it) }
    }
    val fingerprintsB = remember(cardsB) {
        cardsB.associateBy { getCardFingerprint(it) }
    }

    // Intersect keys to find cards with identical content
    val commonFingerprints = remember(fingerprintsA, fingerprintsB) {
        fingerprintsA.keys.intersect(fingerprintsB.keys)
    }

    val commonCards = remember(cardsA, commonFingerprints) {
        cardsA.filter { commonFingerprints.contains(getCardFingerprint(it)) }
    }
    val uniqueCardsA = remember(cardsA, commonFingerprints) {
        cardsA.filter { !commonFingerprints.contains(getCardFingerprint(it)) }
    }
    val uniqueCardsB = remember(cardsB, commonFingerprints) {
        cardsB.filter { !commonFingerprints.contains(getCardFingerprint(it)) }
    }

    // Calculate overlap percentage based on the smaller deck size (relative duplicate ratio)
    val overlapPercentage = remember(cardsA, cardsB, commonCards) {
        val minSize = minOf(cardsA.size, cardsB.size)
        if (minSize > 0) {
            (commonCards.size.toFloat() / minSize.toFloat()) * 100f
        } else 0f
    }

    var activeTab by remember { mutableIntStateOf(0) } // 0: Comum, 1: Únicos A, 2: Únicos B

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Compare,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Comparador de Baralhos L2",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Identificação inteligente de duplicatas e sobreposição de cards",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_compare_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar"
                        )
                    }
                }

                // Deck Selectors Container (Stacked vertically)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Selector Deck A
                        ExposedDropdownMenuBox(
                            expanded = expandedDropdownA,
                            onExpandedChange = { expandedDropdownA = !expandedDropdownA },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val iconKeyA = DisciplinePalette.getIconKeyForDiscipline(selectedPairA.second)
                            val iconA = DisciplinePalette.getIconVector(iconKeyA)
                            val colorHexA = DisciplinePalette.getColorForDiscipline(selectedPairA.second)
                            val parsedColorA = DisciplinePalette.parseColor(colorHexA)

                            OutlinedTextField(
                                value = "${selectedPairA.second} — (${selectedPairA.first})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Baralho A", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = iconA,
                                        contentDescription = null,
                                        tint = parsedColorA,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdownA) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedDropdownA,
                                onDismissRequest = { expandedDropdownA = false }
                            ) {
                                l1L2Pairs.forEach { pair ->
                                    DropdownMenuItem(
                                        text = { Text("${pair.second} — (${pair.first})", fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            selectedPairA = pair
                                            expandedDropdownA = false
                                        }
                                    )
                                }
                            }
                        }

                        // Swap Button Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        val temp = selectedPairA
                                        selectedPairA = selectedPairB
                                        selectedPairB = temp
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "Inverter baralhos",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Inverter Baralhos A / B",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        // Selector Deck B
                        ExposedDropdownMenuBox(
                            expanded = expandedDropdownB,
                            onExpandedChange = { expandedDropdownB = !expandedDropdownB },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val iconKeyB = DisciplinePalette.getIconKeyForDiscipline(selectedPairB.second)
                            val iconB = DisciplinePalette.getIconVector(iconKeyB)
                            val colorHexB = DisciplinePalette.getColorForDiscipline(selectedPairB.second)
                            val parsedColorB = DisciplinePalette.parseColor(colorHexB)

                            OutlinedTextField(
                                value = "${selectedPairB.second} — (${selectedPairB.first})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Baralho B", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = iconB,
                                        contentDescription = null,
                                        tint = parsedColorB,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdownB) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedDropdownB,
                                onDismissRequest = { expandedDropdownB = false }
                            ) {
                                l1L2Pairs.forEach { pair ->
                                    DropdownMenuItem(
                                        text = { Text("${pair.second} — (${pair.first})", fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            selectedPairB = pair
                                            expandedDropdownB = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Visual Similarity Gauge Bar
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Duplicação / Taxa de Sobreposição",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "%.1f%%".format(overlapPercentage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (overlapPercentage >= 80f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { (overlapPercentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Summary Metric Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Deck A",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${cardsA.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedPairA.second,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Em Comum",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${commonCards.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Cards Idênticos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Deck B",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${cardsB.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedPairB.second,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filtrar texto das perguntas e respostas...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar busca")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Tabs
                PrimaryTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Em Comum (${commonCards.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Únicos A (${uniqueCardsA.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Únicos B (${uniqueCardsB.size})", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val rawDisplayCards = when (activeTab) {
                    0 -> commonCards
                    1 -> uniqueCardsA
                    else -> uniqueCardsB
                }

                val currentDisplayCards = remember(rawDisplayCards, searchQuery) {
                    if (searchQuery.isBlank()) {
                        rawDisplayCards
                    } else {
                        rawDisplayCards.filter {
                            it.frontHtml.contains(searchQuery, ignoreCase = true) ||
                            it.backHtml.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                // Cards List Container
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        if (currentDisplayCards.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (activeTab == 0) {
                                        "Nenhum card em comum encontrado entre os baralhos selecionados."
                                    } else {
                                        "Nenhum card exclusivo nesta categoria."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(currentDisplayCards, key = { it.id }) { card ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.primaryContainer
                                                    ) {
                                                        Text(
                                                            text = "L3: ${card.l3.ifEmpty { "Geral" }}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }

                                                    if (activeTab == 0) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.CheckCircle,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                                Text(
                                                                    text = "Duplicado",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = "ID: #${card.id}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = card.frontHtml.ifEmpty { "(Sem pergunta)" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 3
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "R: ${card.backHtml.ifEmpty { "(Sem resposta)" }}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 3,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDeleteL2Discipline != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showDeleteADialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Excluir Baralho A", style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick = { showDeleteBDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Excluir Baralho B", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_close_comparison")
                    ) {
                        Text("Fechar Comparação")
                    }
                }
            }
        }
    }

    if (showDeleteADialog && onDeleteL2Discipline != null) {
        AlertDialog(
            onDismissRequest = { showDeleteADialog = false },
            title = { Text("Excluir Baralho A (${selectedPairA.second})?") },
            text = { Text("Deseja realmente excluir todos os cards de '${selectedPairA.second}' no baralho '${selectedPairA.first}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteL2Discipline(selectedPairA.first, selectedPairA.second)
                        showDeleteADialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir Baralho A")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteADialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteBDialog && onDeleteL2Discipline != null) {
        AlertDialog(
            onDismissRequest = { showDeleteBDialog = false },
            title = { Text("Excluir Baralho B (${selectedPairB.second})?") },
            text = { Text("Deseja realmente excluir todos os cards de '${selectedPairB.second}' no baralho '${selectedPairB.first}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteL2Discipline(selectedPairB.first, selectedPairB.second)
                        showDeleteBDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir Baralho B")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteBDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
