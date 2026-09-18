package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import android.os.SystemClock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.FlashcardEntity
import com.example.data.model.PerCardTimeLimit
import com.example.data.model.SessionLiveStats
import com.example.data.model.StudyTimerConfig
import com.example.data.srs.FsrsScheduler
import com.example.ui.components.DisciplinePalette
import com.example.ui.components.HtmlText
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    cards: List<FlashcardEntity>,
    currentIndex: Int,
    isAnswerRevealed: Boolean,
    isCompleted: Boolean,
    sessionStats: SessionLiveStats,
    timerConfig: StudyTimerConfig = StudyTimerConfig(),
    srsAlgorithm: String = "FSRS",
    targetRetention: Float = 0.90f,
    onRevealAnswer: () -> Unit,
    onRateCard: (Int, Long) -> Unit,
    onEditCard: (FlashcardEntity) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessionStartTimestamp = remember { SystemClock.elapsedRealtime() }
    var cardStartTimestamp by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var sessionElapsedSeconds by remember { mutableLongStateOf(0L) }
    var cardElapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(currentIndex) {
        cardStartTimestamp = SystemClock.elapsedRealtime()
        cardElapsedSeconds = 0L
    }

    LaunchedEffect(isCompleted) {
        while (!isCompleted) {
            val now = SystemClock.elapsedRealtime()
            sessionElapsedSeconds = (now - sessionStartTimestamp) / 1000L
            cardElapsedSeconds = (now - cardStartTimestamp) / 1000L
            delay(500L)
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (!isCompleted && cards.isNotEmpty()) {
                            "Card ${currentIndex + 1} de ${cards.size}"
                        } else {
                            "Sessão de Estudos"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onFinish,
                        modifier = Modifier.testTag("btn_close_study")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Encerrar Sessão"
                        )
                    }
                },
                actions = {
                    if (!isCompleted && cards.isNotEmpty() && currentIndex in cards.indices) {
                        IconButton(
                            onClick = { onEditCard(cards[currentIndex]) },
                            modifier = Modifier.testTag("btn_edit_current_card")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar este Card"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (cards.isEmpty() && sessionStats.completedCards == 0) {
            EmptyStudySessionView(
                onFinish = onFinish,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
            )
        } else if (isCompleted || cards.isEmpty()) {
            SessionCompletedView(
                totalCards = if (sessionStats.completedCards > 0) sessionStats.completedCards else cards.size,
                stats = sessionStats,
                totalSessionElapsedSeconds = sessionElapsedSeconds,
                onFinish = onFinish,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
            )
        } else {
            val card = cards[currentIndex]
            val disciplineColor = DisciplinePalette.parseColor(
                DisciplinePalette.getColorForDiscipline(card.l2)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Live In-Session Review Status Tracker HUD with Configurable Timer
                SessionLiveHud(
                    stats = sessionStats,
                    remaining = (cards.size - currentIndex).coerceAtLeast(1),
                    sessionElapsedSeconds = sessionElapsedSeconds,
                    cardElapsedSeconds = cardElapsedSeconds,
                    perCardLimit = timerConfig.perCardLimit
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentIndex.toFloat() / cards.size).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hierarchy breadcrumb: L1 > L2 > L3
                HierarchyBreadcrumb(
                    l1 = card.l1,
                    l2 = card.l2,
                    l3 = card.l3,
                    disciplineColor = disciplineColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Main Flashcard View
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("study_flashcard"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp)
                    ) {
                        // Card Header with Repetition Stats and Mastery
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (masteryText, masteryColor) = when (card.masteryLevel) {
                                2 -> "Dominado" to Color(0xFF059669)
                                1 -> "Aprendendo" to Color(0xFFD97706)
                                else -> "Novo" to Color(0xFF6B7280)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = masteryColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = masteryText.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = masteryColor
                                )
                            }

                            Text(
                                text = "Rep: ${card.reps} • Int: ${card.intervalDays}d • Fator: ${String.format("%.1f", card.easeFactor)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Front HTML question
                        HtmlText(
                            html = card.frontHtml,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 26.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // If Answer Revealed -> Show Back HTML Answer & Justification
                        AnimatedVisibility(
                            visible = isAnswerRevealed,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "RESPOSTA & FUNDAMENTAÇÃO",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Back HTML answer with full preserved formatting!
                                HtmlText(
                                    html = card.backHtml,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (card.tags.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Tags: ${card.tags}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Controls
                if (!isAnswerRevealed) {
                    Button(
                        onClick = onRevealAnswer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_reveal_answer"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Flip, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mostrar Resposta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Rating Buttons (Again, Hard, Good, Easy) with Review Status & Time Tracking
                    val (againStr, hardStr, goodStr, easyStr) = remember(
                        card.id, card.reps, card.stability, card.difficulty, card.intervalDays, card.easeFactor, srsAlgorithm, targetRetention
                    ) {
                        if (srsAlgorithm.equals("FSRS", ignoreCase = true)) {
                            val preview = FsrsScheduler.previewIntervals(card, targetRetention.toDouble())
                            listOf("${preview.againDays}d", "${preview.hardDays}d", "${preview.goodDays}d", "${preview.easyDays}d")
                        } else {
                            val hardDays = (card.intervalDays * 1.2).toInt().coerceAtLeast(1)
                            val goodDays = if (card.intervalDays == 0) 1 else (card.intervalDays * card.easeFactor).roundToInt().coerceAtLeast(2)
                            val easyDays = if (card.intervalDays == 0) 4 else (card.intervalDays * card.easeFactor * 1.3).roundToInt().coerceAtLeast(4)
                            listOf("1d", "${hardDays}d", "${goodDays}d", "${easyDays}d")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RatingButton(
                            label = "Errei",
                            interval = againStr,
                            color = Color(0xFFEF4444),
                            onClick = {
                                val now = SystemClock.elapsedRealtime()
                                val cardDurationMillis = (now - cardStartTimestamp).coerceAtLeast(300L)
                                onRateCard(1, cardDurationMillis)
                            },
                            testTag = "btn_rate_again",
                            modifier = Modifier.weight(1f)
                        )
                        RatingButton(
                            label = "Difícil",
                            interval = hardStr,
                            color = Color(0xFFF59E0B),
                            onClick = {
                                val now = SystemClock.elapsedRealtime()
                                val cardDurationMillis = (now - cardStartTimestamp).coerceAtLeast(300L)
                                onRateCard(2, cardDurationMillis)
                            },
                            testTag = "btn_rate_hard",
                            modifier = Modifier.weight(1f)
                        )
                        RatingButton(
                            label = "Bom",
                            interval = goodStr,
                            color = Color(0xFF3B82F6),
                            onClick = {
                                val now = SystemClock.elapsedRealtime()
                                val cardDurationMillis = (now - cardStartTimestamp).coerceAtLeast(300L)
                                onRateCard(3, cardDurationMillis)
                            },
                            testTag = "btn_rate_good",
                            modifier = Modifier.weight(1f)
                        )
                        RatingButton(
                            label = "Fácil",
                            interval = easyStr,
                            color = Color(0xFF10B981),
                            onClick = {
                                val now = SystemClock.elapsedRealtime()
                                val cardDurationMillis = (now - cardStartTimestamp).coerceAtLeast(300L)
                                onRateCard(4, cardDurationMillis)
                            },
                            testTag = "btn_rate_easy",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SessionLiveHud(
    stats: SessionLiveStats,
    remaining: Int,
    sessionElapsedSeconds: Long,
    cardElapsedSeconds: Long,
    perCardLimit: PerCardTimeLimit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_timer_hud")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HudCounterPill(label = "Errei", count = stats.againCount, color = Color(0xFFEF4444))
                HudCounterPill(label = "Difícil", count = stats.hardCount, color = Color(0xFFF59E0B))
                HudCounterPill(label = "Bom", count = stats.goodCount, color = Color(0xFF3B82F6))
                HudCounterPill(label = "Fácil", count = stats.easyCount, color = Color(0xFF10B981))
            }

            // Live Timer & Pace indicators
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (perCardLimit != PerCardTimeLimit.UNLIMITED) {
                    val isExceeded = cardElapsedSeconds > perCardLimit.seconds
                    val isWarning = cardElapsedSeconds >= (perCardLimit.seconds * 0.75f)
                    val cardTimerColor = when {
                        isExceeded -> Color(0xFFEF4444)
                        isWarning -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = cardTimerColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${cardElapsedSeconds}s / ${perCardLimit.seconds}s",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = cardTimerColor
                        )
                    }
                }

                // Session elapsed stopwatch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Tempo de Estudo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    val mins = sessionElapsedSeconds / 60
                    val secs = sessionElapsedSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun HudCounterPill(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun HierarchyBreadcrumb(
    l1: String,
    l2: String,
    l3: String,
    disciplineColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(disciplineColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = l2,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = disciplineColor
            )
            Text(
                text = " › ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = l3,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RatingButton(
    label: String,
    interval: String,
    color: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = interval,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SessionCompletedView(
    totalCards: Int,
    stats: SessionLiveStats,
    totalSessionElapsedSeconds: Long = 0L,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF10B981).copy(alpha = 0.15f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sessão Concluída!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Você revisou $totalCards flashcards. Seus intervalos de repetição espaçada e índices de domínio foram atualizados.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Session Review Status Tracking Breakdown Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "STATUS DA REVISÃO NA SESSÃO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ResultStatColumn(label = "Errei", count = stats.againCount, color = Color(0xFFEF4444))
                    ResultStatColumn(label = "Difícil", count = stats.hardCount, color = Color(0xFFF59E0B))
                    ResultStatColumn(label = "Bom", count = stats.goodCount, color = Color(0xFF3B82F6))
                    ResultStatColumn(label = "Fácil", count = stats.easyCount, color = Color(0xFF10B981))
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Taxa de Acerto (Bom + Fácil):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.successRate.roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                if (stats.graduatedToMasteredCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Promovidos a Dominado:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+${stats.graduatedToMasteredCount} cards",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time and Per-Deck Performance Summary Report
        val effectiveTotalSeconds = totalSessionElapsedSeconds.coerceAtLeast(stats.timeReport.totalTimeMillis / 1000L)
        val formattedSessionTime = if (effectiveTotalSeconds > 0) {
            String.format(
                "%02dm %02ds",
                effectiveTotalSeconds / 60,
                effectiveTotalSeconds % 60
            )
        } else if (stats.timeReport.totalTimeMillis > 0) {
            "< 1s"
        } else {
            "00m 00s"
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("session_time_report")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RELATÓRIO DE TEMPO POR DECK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = stats.timeReport.paceLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary KPIs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedSessionTime,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tempo Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val avgSec = if (stats.timeReport.averageSecondsPerCard > 0) {
                            stats.timeReport.averageSecondsPerCard
                        } else if (totalCards > 0) {
                            effectiveTotalSeconds.toDouble() / totalCards
                        } else {
                            0.0
                        }
                        Text(
                            text = "${String.format("%.1f", avgSec)}s",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Média / Card",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Deck Breakdown List
                if (stats.timeReport.deckBreakdown.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Detalhamento por Baralho:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    stats.timeReport.deckBreakdown.forEach { deckItem ->
                        val deckColor = DisciplinePalette.parseColor(
                            DisciplinePalette.getColorForDiscipline(deckItem.deckName)
                        )
                        val deckPercent = if (stats.timeReport.totalTimeMillis > 0) {
                            (deckItem.timeMillis.toFloat() / stats.timeReport.totalTimeMillis).coerceIn(0f, 1f)
                        } else 1f

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("deck_time_item_${deckItem.deckName}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(deckColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = deckItem.deckName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${deckItem.cardsCount} cards",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = deckItem.formattedTime,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { deckPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = deckColor,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_finish_study"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Voltar ao Painel", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResultStatColumn(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyStudySessionView(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Nenhum Flashcard Pendente",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Não foram encontrados flashcards para o filtro selecionado ou todos já foram revisados e estão em dia com o ciclo de repetição espaçada.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_empty_study_back"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Voltar ao Painel", fontWeight = FontWeight.Bold)
        }
    }
}
