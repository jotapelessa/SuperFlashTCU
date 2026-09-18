package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.L1DeckSummary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeckCard(
    deck: L1DeckSummary,
    onClick: () -> Unit,
    onQuickStudy: () -> Unit,
    onEditDeck: ((L1DeckSummary) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val primaryColor = when {
        !deck.cardColorHex.isNullOrBlank() -> DisciplinePalette.parseColor(deck.cardColorHex)
        deck.disciplineColors.isNotEmpty() -> DisciplinePalette.parseColor(deck.disciplineColors.first())
        else -> MaterialTheme.colorScheme.primary
    }

    val isCoverActive = !deck.coverUrl.isNullOrBlank()
    val textShadow = if (isCoverActive) Shadow(Color.Black, Offset(2f, 2f), 8f) else null
    val textColor = if (isCoverActive) Color.White else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (isCoverActive) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("deck_card_${deck.l1}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Cover Image if active
            if (isCoverActive) {
                AsyncImage(
                    model = deck.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                // Dark Gradient Scrim Overlay for high-contrast legibility
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row: L1 Badge + Course/Cursinho Badge + Discipline Color Dots + Edit + Due Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCoverActive) Color.Black.copy(alpha = 0.45f) else primaryColor.copy(alpha = 0.15f),
                            border = if (isCoverActive) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) else null
                        ) {
                            Text(
                                text = "L1 BARALHO",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(shadow = textShadow),
                                fontWeight = FontWeight.Bold,
                                color = if (isCoverActive) Color.White else primaryColor
                            )
                        }

                        // Cursinho / Provider Badge
                        if (!deck.courseName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = primaryColor,
                                shadowElevation = 1.dp
                            ) {
                                Text(
                                    text = deck.courseName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Badge de IA Pausada
                        if (!deck.isAiAnalysisEnabled) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCoverActive) Color.Black.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isCoverActive) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)) else null
                            ) {
                                Text(
                                    text = "IA Pausada",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(shadow = textShadow),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCoverActive) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Discipline color indicators
                        if (deck.disciplineColors.isNotEmpty() && !isCoverActive) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                deck.disciplineColors.take(4).forEach { hex ->
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(DisciplinePalette.parseColor(hex))
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Due status badge
                        if (deck.dueCards > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "${deck.dueCards} a revisar",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF10B981).copy(alpha = if (isCoverActive) 0.85f else 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isCoverActive) Color.White else Color(0xFF059669),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Em dia",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCoverActive) Color.White else Color(0xFF059669)
                                    )
                                }
                            }
                        }

                        // Edit L1 Deck Action Button
                        if (onEditDeck != null) {
                            IconButton(
                                onClick = { onEditDeck(deck) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_edit_deck_${deck.l1}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Baralho L1",
                                    tint = if (isCoverActive) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // L1 Title
                Text(
                    text = deck.l1,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        shadow = textShadow
                    ),
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Compact chips: L2 count, L3 count, Total cards
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetaChip(
                        icon = Icons.Default.Folder,
                        label = "${deck.l2Count} matérias (L2)",
                        tint = subTextColor,
                        isCoverActive = isCoverActive
                    )
                    MetaChip(
                        icon = Icons.Default.AutoStories,
                        label = "${deck.l3Count} tópicos (L3)",
                        tint = subTextColor,
                        isCoverActive = isCoverActive
                    )
                    MetaChip(
                        icon = Icons.Default.School,
                        label = "${deck.totalCards} cards",
                        tint = subTextColor,
                        isCoverActive = isCoverActive
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar & Mastery Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Domínio do Baralho",
                        style = MaterialTheme.typography.labelSmall.copy(shadow = textShadow),
                        color = subTextColor
                    )
                    Text(
                        text = "${deck.masteryPercentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(shadow = textShadow),
                        fontWeight = FontWeight.Bold,
                        color = if (deck.masteryPercentage >= 70f) (if (isCoverActive) Color(0xFF34D399) else Color(0xFF059669)) else (if (isCoverActive) Color.White else primaryColor)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { (deck.masteryPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (deck.masteryPercentage >= 70f) Color(0xFF10B981) else primaryColor,
                    trackColor = if (isCoverActive) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerHighest
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Ver Sub-decks (L2/L3)",
                            style = MaterialTheme.typography.labelMedium.copy(shadow = textShadow),
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCoverActive) Color.White else MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Abrir sub-decks",
                            tint = if (isCoverActive) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    FilledTonalButton(
                        onClick = onQuickStudy,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("quick_study_${deck.l1}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (deck.dueCards > 0) "Revisar (${deck.dueCards})" else "Estudar",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    isCoverActive: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isCoverActive) Color.Black.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
        border = if (isCoverActive) androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    shadow = if (isCoverActive) Shadow(Color.Black, Offset(1f, 1f), 4f) else null
                ),
                color = tint
            )
        }
    }
}
