package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.regex.Pattern

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class ListItem(val bullet: String, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    object Divider : MarkdownBlock()
}

@Composable
fun AiMarkdownContent(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdownText) {
        parseMarkdownToBlocks(markdownText)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    HeaderView(block = block)
                }
                is MarkdownBlock.ListItem -> {
                    ListItemView(block = block)
                }
                is MarkdownBlock.Paragraph -> {
                    ParagraphView(text = block.text)
                }
                is MarkdownBlock.Table -> {
                    TableView(block = block)
                }
                is MarkdownBlock.Divider -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderView(block: MarkdownBlock.Header) {
    val title = block.text
    val (icon, containerColor) = when {
        title.contains("Diagnóstico", ignoreCase = true) -> Icons.Default.Assessment to MaterialTheme.colorScheme.primaryContainer
        title.contains("Crítica", ignoreCase = true) || title.contains("Alerta", ignoreCase = true) || title.contains("Gargalo", ignoreCase = true) -> Icons.Default.Warning to MaterialTheme.colorScheme.errorContainer
        title.contains("Plano", ignoreCase = true) || title.contains("Ação", ignoreCase = true) || title.contains("Meta", ignoreCase = true) -> Icons.Default.Flag to MaterialTheme.colorScheme.secondaryContainer
        title.contains("Dica", ignoreCase = true) || title.contains("Espaçada", ignoreCase = true) -> Icons.Default.Lightbulb to MaterialTheme.colorScheme.tertiaryContainer
        else -> Icons.Default.Psychology to MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (containerColor) {
        MaterialTheme.colorScheme.primaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
        MaterialTheme.colorScheme.errorContainer -> MaterialTheme.colorScheme.onErrorContainer
        MaterialTheme.colorScheme.secondaryContainer -> MaterialTheme.colorScheme.onSecondaryContainer
        MaterialTheme.colorScheme.tertiaryContainer -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Spacer(modifier = Modifier.height(4.dp))
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = if (block.level <= 2) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ListItemView(block: MarkdownBlock.ListItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Bullet badge
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = parseMarkdownInline(block.text),
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ParagraphView(text: String) {
    Text(
        text = parseMarkdownInline(text),
        style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 22.sp,
            fontSize = 14.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    )
}

@Composable
private fun TableView(block: MarkdownBlock.Table) {
    if (block.headers.isEmpty()) return

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            // Linha de Cabeçalho da Tabela
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                block.headers.forEach { header ->
                    Box(
                        modifier = Modifier
                            .widthIn(min = 90.dp, max = 220.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = parseMarkdownInline(header),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Linhas de Dados da Tabela com alternância sutil de fundo
            block.rows.forEachIndexed { rowIndex, rowCells ->
                val rowBg = if (rowIndex % 2 == 0) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                }
                Row(
                    modifier = Modifier
                        .background(rowBg)
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    block.headers.indices.forEach { colIndex ->
                        val cellText = rowCells.getOrNull(colIndex) ?: ""
                        Box(
                            modifier = Modifier
                                .widthIn(min = 90.dp, max = 220.dp)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = parseMarkdownInline(cellText),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseMarkdownToBlocks(content: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = content.lines()

    var i = 0
    while (i < lines.size) {
        val rawLine = lines[i].trim()

        if (rawLine.isBlank()) {
            blocks.add(MarkdownBlock.Divider)
            i++
            continue
        }

        // Detecção de Tabela Markdown (| Col1 | Col2 | ...)
        if (rawLine.startsWith("|") && i + 1 < lines.size && isTableDelimiter(lines[i + 1])) {
            val headers = splitTableRow(rawLine)
            i += 2 // Pula linha de cabeçalho e divisor
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].trim().startsWith("|")) {
                val candidateLine = lines[i].trim()
                if (!isTableDelimiter(candidateLine)) {
                    val row = splitTableRow(candidateLine)
                    if (row.isNotEmpty()) {
                        rows.add(row)
                    }
                }
                i++
            }
            blocks.add(MarkdownBlock.Table(headers = headers, rows = rows))
            continue
        }

        when {
            rawLine.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Header(level = 3, text = cleanHeader(rawLine.removePrefix("### "))))
            }
            rawLine.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Header(level = 2, text = cleanHeader(rawLine.removePrefix("## "))))
            }
            rawLine.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Header(level = 1, text = cleanHeader(rawLine.removePrefix("# "))))
            }
            rawLine.startsWith("- ") || rawLine.startsWith("* ") || rawLine.startsWith("• ") -> {
                val bulletContent = rawLine.substring(2).trim()
                blocks.add(MarkdownBlock.ListItem(bullet = "•", text = bulletContent))
            }
            rawLine.matches(Regex("^\\d+\\.\\s.*")) -> {
                val match = Regex("^(\\d+\\.)\\s(.*)").find(rawLine)
                if (match != null) {
                    val number = match.groupValues[1]
                    val itemText = match.groupValues[2]
                    blocks.add(MarkdownBlock.ListItem(bullet = number, text = itemText))
                } else {
                    blocks.add(MarkdownBlock.Paragraph(rawLine))
                }
            }
            else -> {
                blocks.add(MarkdownBlock.Paragraph(rawLine))
            }
        }
        i++
    }

    return blocks
}

private fun isTableDelimiter(line: String): Boolean {
    val clean = line.trim()
    return clean.startsWith("|") && clean.contains("-") && clean.all { it == '|' || it == '-' || it == ':' || it.isWhitespace() }
}

private fun splitTableRow(line: String): List<String> {
    var text = line.trim()
    if (text.startsWith("|")) text = text.removePrefix("|")
    if (text.endsWith("|")) text = text.removeSuffix("|")
    return text.split("|").map { it.trim() }
}

private fun cleanHeader(text: String): String {
    return text.replace(Regex("^#+\\s*"), "").trim()
}

fun parseMarkdownInline(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val pattern = Pattern.compile("\\*\\*(.*?)\\*\\*")
    val matcher = pattern.matcher(text)
    var lastIndex = 0

    while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()

        if (start > lastIndex) {
            builder.append(text.substring(lastIndex, start))
        }

        val boldText = matcher.group(1) ?: ""
        val spanStart = builder.length
        builder.append(boldText)
        builder.addStyle(
            SpanStyle(fontWeight = FontWeight.Bold),
            spanStart,
            builder.length
        )

        lastIndex = end
    }

    if (lastIndex < text.length) {
        builder.append(text.substring(lastIndex))
    }

    return builder.toAnnotatedString()
}
