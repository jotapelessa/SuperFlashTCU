package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayStudyStat
import com.example.data.model.StudyProgressReport
import java.util.Calendar
import kotlin.math.roundToInt

import androidx.compose.material.icons.filled.AutoAwesome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    report: StudyProgressReport,
    todayReviewed: Int,
    dailyGoal: Int,
    onSetDailyGoal: (Int) -> Unit,
    onStartStudySession: () -> Unit,
    onOpenAiAnalysis: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showGoalDialog = remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Acompanhamento de Progresso",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Metas diárias, frequência e constância de estudos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (onOpenAiAnalysis != null) {
                        IconButton(
                            onClick = onOpenAiAnalysis,
                            modifier = Modifier.testTag("btn_top_ai_progress")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Análise IA Gemini",
                                tint = MaterialTheme.colorScheme.primary
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
                .testTag("progress_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Analysis Banner
            if (onOpenAiAnalysis != null) {
                item {
                    Card(
                        onClick = onOpenAiAnalysis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_banner_ai_progress"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Análise com IA Gemini",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Analise suas métricas de retenção e receba um plano de ação para os estudos L1/L2/L3.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // 1. Daily Goal Card
            item {
                ProgressGoalCard(
                    todayReviewed = todayReviewed,
                    dailyGoal = dailyGoal,
                    streak = report.currentStreakDays,
                    onEditGoal = { showGoalDialog.value = true },
                    onStartStudy = onStartStudySession
                )
            }

            // 2. 7-Day Frequency Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Frequência dos Últimos 7 Dias",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (report.studyFrequency7Days.isNotEmpty()) {
                            StudyFrequencyBarChart(dailyStats = report.studyFrequency7Days)
                        } else {
                            Text(
                                text = "Nenhum dado de estudo recente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Overall Mastery & Retention Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Nível de Retenção e Distribuição",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniBadge(
                                title = "Dominados",
                                count = "${report.masteredCount}",
                                color = Color(0xFF10B981)
                            )
                            StatMiniBadge(
                                title = "Aprendendo",
                                count = "${report.learningCount}",
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatMiniBadge(
                                title = "Novos",
                                count = "${report.newCount}",
                                color = MaterialTheme.colorScheme.secondary
                            )
                            StatMiniBadge(
                                title = "Para Revisar",
                                count = "${report.dueNowCount}",
                                color = if (report.dueNowCount > 0) MaterialTheme.colorScheme.error else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    if (showGoalDialog.value) {
        DailyGoalDialog(
            currentGoal = dailyGoal,
            onSave = { newGoal ->
                onSetDailyGoal(newGoal)
                showGoalDialog.value = false
            },
            onDismiss = { showGoalDialog.value = false }
        )
    }
}

@Composable
private fun ProgressGoalCard(
    todayReviewed: Int,
    dailyGoal: Int,
    streak: Int,
    onEditGoal: () -> Unit,
    onStartStudy: () -> Unit
) {
    val progressPercent = if (dailyGoal > 0) (todayReviewed.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f) else 0f
    val isGoalMet = todayReviewed >= dailyGoal

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Meta Diária",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isGoalMet) "Meta concluída hoje! 🎉" else "${dailyGoal - todayReviewed} cards restantes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onEditGoal,
                    modifier = Modifier.testTag("btn_edit_daily_goal_progress")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Meta",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(90.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        strokeWidth = 8.dp,
                    )
                    CircularProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isGoalMet) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(progressPercent * 100).roundToInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$todayReviewed/$dailyGoal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$streak Dias Consecutivos",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = onStartStudy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_start_progress_study"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Estudar Agora")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniBadge(
    title: String,
    count: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StudyFrequencyBarChart(dailyStats: List<DayStudyStat>) {
    val sortedDailyStats = remember(dailyStats) {
        dailyStats.sortedBy { stat ->
            val cal = Calendar.getInstance().apply { timeInMillis = stat.timestamp }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 7
            }
        }
    }

    val maxReviews = (sortedDailyStats.maxOfOrNull { it.reviewsCount } ?: 1).coerceAtLeast(1)
    val barAnimatables = remember(sortedDailyStats) { sortedDailyStats.map { Animatable(0f) } }

    LaunchedEffect(sortedDailyStats) {
        sortedDailyStats.forEachIndexed { index, stat ->
            val target = (stat.reviewsCount.toFloat() / maxReviews).coerceIn(0.05f, 1f)
            barAnimatables[index].animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 600, delayMillis = index * 50)
            )
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val todayColor = Color(0xFF10B981)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val width = size.width
            val height = size.height
            val barWidth = 24.dp.toPx()
            val itemCount = sortedDailyStats.size
            val availableSpace = width - (barWidth * itemCount)
            val spacing = availableSpace / (itemCount + 1)

            sortedDailyStats.forEachIndexed { index, stat ->
                val barX = spacing + index * (barWidth + spacing)
                val animatedFraction = barAnimatables[index].value
                val barHeight = (height - 35.dp.toPx()) * animatedFraction
                val barTop = (height - 25.dp.toPx()) - barHeight

                val color = if (stat.isToday) todayColor else primaryColor

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.6f))
                    ),
                    topLeft = Offset(barX, barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                if (stat.reviewsCount > 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        stat.reviewsCount.toString(),
                        barX + barWidth / 2,
                        barTop - 6.dp.toPx(),
                        android.graphics.Paint().apply {
                            this.color = labelColor
                            this.textSize = 10.sp.toPx()
                            this.textAlign = android.graphics.Paint.Align.CENTER
                            this.isAntiAlias = true
                        }
                    )
                }

                drawContext.canvas.nativeCanvas.drawText(
                    stat.dayLabel,
                    barX + barWidth / 2,
                    height - 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        this.color = labelColor
                        this.textSize = 11.sp.toPx()
                        this.textAlign = android.graphics.Paint.Align.CENTER
                        this.isAntiAlias = true
                    }
                )
            }
        }
    }
}
