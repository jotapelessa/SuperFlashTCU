package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.DomainMasteryStat
import com.example.data.model.StudyProgressReport
import com.example.ui.components.DisciplinePalette
import java.util.Calendar
import kotlin.math.roundToInt

import androidx.compose.material.icons.filled.AutoAwesome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    report: StudyProgressReport,
    selectedL1Title: String?,
    onBack: () -> Unit,
    onOpenAiAnalysis: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Progresso & Estatísticas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedL1Title != null) "Escopo: $selectedL1Title" else "Visão Consolidada de Todos os Baralhos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_analytics")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    if (onOpenAiAnalysis != null) {
                        IconButton(
                            onClick = onOpenAiAnalysis,
                            modifier = Modifier.testTag("btn_top_ai_analysis")
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
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("analytics_scroll_view"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gemini AI Tutor Banner
            if (onOpenAiAnalysis != null) {
                item {
                    Card(
                        onClick = onOpenAiAnalysis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_banner_ai_analysis"),
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
                                    text = "Obtenha diagnósticos de retenção L1/L2/L3 e recomendações personalizadas para hoje.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Summary Highlight Metrics
            item {
                MetricHighlightsRow(report = report)
            }

            // Study Frequency Chart (Weekly reviews activity)
            item {
                StudyFrequencyChartCard(frequencyList = report.studyFrequency7Days)
            }

            // Status Distribution Overview
            item {
                CardStatusDistributionCard(report = report)
            }

            // Mastery Levels per Domain (Disciplines L2)
            item {
                Text(
                    text = "Nível de Domínio por Matéria (${report.domainMasteryList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            if (report.domainMasteryList.isEmpty()) {
                item {
                    EmptyDomainStateCard()
                }
            } else {
                items(report.domainMasteryList, key = { it.domainName }) { domainStat ->
                    DomainMasteryCard(stat = domainStat)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MetricHighlightsRow(report: StudyProgressReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryKpiBox(
            title = "Sequência",
            value = "${report.currentStreakDays}d",
            icon = Icons.Default.LocalFireDepartment,
            color = Color(0xFFF97316),
            modifier = Modifier.weight(1f)
        )
        SummaryKpiBox(
            title = "Domínio Geral",
            value = "${report.overallMasteryPercentage.roundToInt()}%",
            icon = Icons.Default.Psychology,
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        SummaryKpiBox(
            title = "Total Revisões",
            value = "${report.totalReviews}",
            icon = Icons.Default.Timeline,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryKpiBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StudyFrequencyChartCard(frequencyList: List<DayStudyStat>) {
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }

    val sortedFrequencyList = remember(frequencyList) {
        frequencyList.sortedBy { stat ->
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

    LaunchedEffect(sortedFrequencyList) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chart_study_frequency"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Frequência de Estudos (7 dias)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    val totalWeekReviews = sortedFrequencyList.sumOf { it.reviewsCount }
                    Text(
                        text = "$totalWeekReviews revisões",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Custom High-Performance Compose Canvas Bar Chart
            val maxCount = (sortedFrequencyList.maxOfOrNull { it.reviewsCount } ?: 5).coerceAtLeast(4)
            val primaryColor = MaterialTheme.colorScheme.primary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary
            val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            val labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

            val density = androidx.compose.ui.platform.LocalDensity.current
            val labelTextSizePx = remember(density) { with(density) { 11.sp.toPx() } }
            val countTextSizePx = remember(density) { with(density) { 10.sp.toPx() } }
            val cornerRadiusPx = remember(density) { with(density) { 6.dp.toPx() } }
            val minBarHeightPx = remember(density) { with(density) { 8.dp.toPx() } }
            val minEmptyBarHeightPx = remember(density) { with(density) { 2.dp.toPx() } }

            val labelPaintNormal = remember(labelTextColor, labelTextSizePx) {
                android.graphics.Paint().apply {
                    color = labelTextColor
                    textSize = labelTextSizePx
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            }
            val labelPaintBold = remember(labelTextColor, labelTextSizePx) {
                android.graphics.Paint().apply {
                    color = labelTextColor
                    textSize = labelTextSizePx
                    isAntiAlias = true
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            }
            val countPaintToday = remember(countTextSizePx) {
                android.graphics.Paint().apply {
                    color = Color(0xFF059669).toArgb()
                    textSize = countTextSizePx
                    isAntiAlias = true
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            }
            val countPaintPrimary = remember(primaryColor, countTextSizePx) {
                android.graphics.Paint().apply {
                    color = primaryColor.toArgb()
                    textSize = countTextSizePx
                    isAntiAlias = true
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            }

            val todayBrush = remember {
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                )
            }
            val activeBrush = remember(primaryColor) {
                Brush.verticalGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
                )
            }
            val emptyBrush = remember(tertiaryColor) {
                Brush.verticalGradient(
                    colors = listOf(tertiaryColor.copy(alpha = 0.2f), tertiaryColor.copy(alpha = 0.1f))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val chartWidth = size.width
                    val chartHeight = size.height - 30f // Leave space for bottom labels
                    val barCount = sortedFrequencyList.size
                    if (barCount == 0) return@Canvas

                    val availableWidthPerBar = chartWidth / barCount
                    val barWidth = availableWidthPerBar * 0.45f
                    val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)

                    // Draw reference gridlines
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val y = chartHeight - (chartHeight * (i.toFloat() / gridLines))
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(chartWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    // Draw bars sem alocações
                    sortedFrequencyList.forEachIndexed { index, dayStat ->
                        val count = dayStat.reviewsCount
                        val normalizedHeight = (count.toFloat() / maxCount) * chartHeight * animationProgress.value
                        val barHeight = normalizedHeight.coerceAtLeast(if (count > 0) minBarHeightPx else minEmptyBarHeightPx)

                        val x = (index * availableWidthPerBar) + (availableWidthPerBar - barWidth) / 2f
                        val y = chartHeight - barHeight

                        val barBrush = when {
                            dayStat.isToday -> todayBrush
                            count > 0 -> activeBrush
                            else -> emptyBrush
                        }

                        // Draw background track for bar
                        drawRoundRect(
                            color = gridColor.copy(alpha = 0.25f),
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, chartHeight),
                            cornerRadius = cornerRadius
                        )

                        // Draw active bar
                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = cornerRadius
                        )

                        // Draw bottom text label
                        val paint = if (dayStat.isToday) labelPaintBold else labelPaintNormal
                        drawContext.canvas.nativeCanvas.drawText(
                            dayStat.dayLabel,
                            x + (barWidth / 2f),
                            size.height - 6f,
                            paint
                        )

                        // Draw count label on top of bar if > 0
                        if (count > 0) {
                            val countPaint = if (dayStat.isToday) countPaintToday else countPaintPrimary
                            drawContext.canvas.nativeCanvas.drawText(
                                "$count",
                                x + (barWidth / 2f),
                                (y - 8f).coerceAtLeast(14f),
                                countPaint
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hoje",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dias anteriores",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Taxa Ativa",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CardStatusDistributionCard(report: StudyProgressReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Distribuição do Acervo de Cards",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment progress line
            val total = report.totalCards.coerceAtLeast(1)
            val masteredFrac = report.masteredCount.toFloat() / total
            val learningFrac = report.learningCount.toFloat() / total
            val newFrac = report.newCount.toFloat() / total

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                if (masteredFrac > 0) {
                    Box(
                        modifier = Modifier
                            .weight(masteredFrac)
                            .fillMaxSize()
                            .background(Color(0xFF10B981))
                    )
                }
                if (learningFrac > 0) {
                    Box(
                        modifier = Modifier
                            .weight(learningFrac)
                            .fillMaxSize()
                            .background(Color(0xFFF59E0B))
                    )
                }
                if (newFrac > 0) {
                    Box(
                        modifier = Modifier
                            .weight(newFrac)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusBadgeLegend(
                    label = "Dominado",
                    count = report.masteredCount,
                    color = Color(0xFF10B981)
                )
                StatusBadgeLegend(
                    label = "Aprendendo",
                    count = report.learningCount,
                    color = Color(0xFFF59E0B)
                )
                StatusBadgeLegend(
                    label = "Novo",
                    count = report.newCount,
                    color = MaterialTheme.colorScheme.outline
                )
                StatusBadgeLegend(
                    label = "Para Revisar",
                    count = report.dueNowCount,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StatusBadgeLegend(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DomainMasteryCard(stat: DomainMasteryStat) {
    val disciplineColor = DisciplinePalette.parseColor(stat.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("domain_mastery_card_${stat.domainName}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(disciplineColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stat.domainName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (stat.masteryPercentage >= 70f) {
                        Color(0xFF10B981).copy(alpha = 0.15f)
                    } else if (stat.masteryPercentage >= 40f) {
                        Color(0xFFF59E0B).copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    }
                ) {
                    Text(
                        text = "${stat.masteryPercentage.roundToInt()}% Domínio",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (stat.masteryPercentage >= 70f) {
                            Color(0xFF059669)
                        } else if (stat.masteryPercentage >= 40f) {
                            Color(0xFFD97706)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-segment mastery distribution bar for this domain
            val total = stat.totalCards.coerceAtLeast(1)
            val masteredFrac = stat.masteredCards.toFloat() / total
            val learningFrac = stat.learningCards.toFloat() / total
            val newFrac = stat.newCards.toFloat() / total

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                if (masteredFrac > 0) {
                    Box(
                        modifier = Modifier
                            .weight(masteredFrac)
                            .fillMaxSize()
                            .background(Color(0xFF10B981))
                    )
                }
                if (learningFrac > 0) {
                    Box(
                        modifier = Modifier
                            .weight(learningFrac)
                            .fillMaxSize()
                            .background(Color(0xFFF59E0B))
                    )
                }
                if (newFrac > 0) {
                    Box(
                        modifier = Modifier
                            .weight(newFrac)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stat.totalCards} cards totais",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• ${stat.masteredCards} Dominados  • ${stat.learningCards} Aprendendo  • ${stat.newCards} Novos",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyDomainStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nenhuma matéria cadastrada",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Importe ou crie flashcards com hierarquia L2 para visualizar o domínio por matéria.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
