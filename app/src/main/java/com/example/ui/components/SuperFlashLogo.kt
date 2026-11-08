package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SuperFlash Official Brand Symbol — Concept: The Gestalt Shutter
 * 
 * Geometria vetorial exata em Compose (baseada no grid 512x512):
 * Quatro formas poligonais calibradas que esculpem o monograma 'S'
 * e a centelha de ativação da memória em espaço negativo puro.
 */
@Composable
fun SuperFlashSymbol(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 32.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension / 512f

        // Top-Right Anchor Block
        val pathTopAnchor = Path().apply {
            moveTo(184f * s, 80f * s)
            lineTo(384f * s, 80f * s)
            cubicTo(406f * s, 80f * s, 424f * s, 98f * s, 424f * s, 120f * s)
            lineTo(424f * s, 240f * s)
            lineTo(344f * s, 240f * s)
            lineTo(344f * s, 160f * s)
            lineTo(264f * s, 160f * s)
            close()
        }

        // Central Kinetic Fin (Upper)
        val pathUpperFin = Path().apply {
            moveTo(120f * s, 152f * s)
            lineTo(232f * s, 264f * s)
            lineTo(160f * s, 264f * s)
            cubicTo(138f * s, 264f * s, 120f * s, 246f * s, 120f * s, 224f * s)
            close()
        }

        // Central Kinetic Fin (Lower)
        val pathLowerFin = Path().apply {
            moveTo(392f * s, 360f * s)
            lineTo(280f * s, 248f * s)
            lineTo(352f * s, 248f * s)
            cubicTo(374f * s, 248f * s, 392f * s, 266f * s, 392f * s, 288f * s)
            close()
        }

        // Bottom-Left Anchor Block
        val pathBottomAnchor = Path().apply {
            moveTo(328f * s, 432f * s)
            lineTo(128f * s, 432f * s)
            cubicTo(106f * s, 432f * s, 88f * s, 414f * s, 88f * s, 392f * s)
            lineTo(88f * s, 272f * s)
            lineTo(168f * s, 272f * s)
            lineTo(168f * s, 352f * s)
            lineTo(248f * s, 352f * s)
            close()
        }

        drawPath(pathTopAnchor, color = tint)
        drawPath(pathUpperFin, color = tint)
        drawPath(pathLowerFin, color = tint)
        drawPath(pathBottomAnchor, color = tint)
    }
}

/**
 * SuperFlash Horizontal Lockup (Symbol + Wordmark)
 */
@Composable
fun SuperFlashLockup(
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SuperFlashSymbol(
            tint = accentColor,
            size = iconSize
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Super",
                fontSize = (iconSize.value * 0.75f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Text(
                text = "Flash",
                fontSize = (iconSize.value * 0.75f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }
    }
}
