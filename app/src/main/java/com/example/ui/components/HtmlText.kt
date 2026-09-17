package com.example.ui.components

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import java.util.regex.Pattern

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Start,
    lineHeight: TextUnit = 24.sp
) {
    val annotatedString = remember(html, color) {
        if (!html.contains('<') && !html.contains('&')) {
            AnnotatedString(html)
        } else {
            val normalized = normalizeAnkiHtml(html)
            val spanned = HtmlCompat.fromHtml(normalized, HtmlCompat.FROM_HTML_MODE_COMPACT)
            spannedToAnnotatedString(spanned, color)
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style.copy(
            color = color,
            textAlign = textAlign,
            lineHeight = lineHeight
        )
    )
}

private val RGB_STYLE_PATTERN = Pattern.compile("style=\"[^\"]*color:\\s*rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)[^\"]*\"", Pattern.CASE_INSENSITIVE)
private val HEX_STYLE_PATTERN = Pattern.compile("style=\"[^\"]*color:\\s*(#[0-9a-fA-F]{6}|#[0-9a-fA-F]{3})[^\"]*\"", Pattern.CASE_INSENSITIVE)

private fun normalizeAnkiHtml(html: String): String {
    var result = html

    // Convert CSS rgb(r, g, b) styles to <font color="#RRGGBB"> for HtmlCompat compatibility
    val rgbMatcher = RGB_STYLE_PATTERN.matcher(result)
    val sbRgb = StringBuffer()
    while (rgbMatcher.find()) {
        val r = rgbMatcher.group(1)?.toIntOrNull() ?: 0
        val g = rgbMatcher.group(2)?.toIntOrNull() ?: 0
        val b = rgbMatcher.group(3)?.toIntOrNull() ?: 0
        val hex = String.format("#%02x%02x%02x", r, g, b)
        rgbMatcher.appendReplacement(sbRgb, "color=\"$hex\"")
    }
    rgbMatcher.appendTail(sbRgb)
    result = sbRgb.toString()

    val hexMatcher = HEX_STYLE_PATTERN.matcher(result)
    val sbHex = StringBuffer()
    while (hexMatcher.find()) {
        val hex = hexMatcher.group(1) ?: "#000000"
        hexMatcher.appendReplacement(sbHex, "color=\"$hex\"")
    }
    hexMatcher.appendTail(sbHex)
    result = sbHex.toString()

    return result
}

private fun spannedToAnnotatedString(spanned: Spanned, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        append(spanned.toString())
        val spans = spanned.getSpans(0, spanned.length, Any::class.java)

        spans.forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)

            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                        Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        Typeface.BOLD_ITALIC -> addStyle(
                            SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                            start,
                            end
                        )
                    }
                }
                is UnderlineSpan -> {
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                }
                is ForegroundColorSpan -> {
                    addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
                }
                is RelativeSizeSpan -> {
                    addStyle(SpanStyle(fontSize = span.sizeChange.em), start, end)
                }
            }
        }
    }
}
