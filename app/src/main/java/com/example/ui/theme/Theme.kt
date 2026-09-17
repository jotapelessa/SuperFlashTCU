package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun buildLightColorScheme(accent: AccentColorOption): ColorScheme {
    return lightColorScheme(
        primary = accent.primaryLight,
        onPrimary = Color.White,
        primaryContainer = accent.primaryLight.copy(alpha = 0.15f),
        onPrimaryContainer = accent.primaryLight,
        secondary = accent.primaryLight.copy(alpha = 0.8f),
        onSecondary = Color.White,
        secondaryContainer = accent.primaryLight.copy(alpha = 0.10f),
        onSecondaryContainer = accent.primaryLight,
        tertiary = Color(0xFF10B981),
        onTertiary = Color.White,
        background = Color(0xFFF8FAFC),
        onBackground = Color(0xFF0F172A),
        surface = Color.White,
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFFCBD5E1),
        outlineVariant = Color(0xFFE2E8F0)
    )
}

fun buildDarkColorScheme(accent: AccentColorOption): ColorScheme {
    return darkColorScheme(
        primary = accent.primaryDark,
        onPrimary = Color(0xFF0F172A),
        primaryContainer = accent.primaryDark.copy(alpha = 0.25f),
        onPrimaryContainer = accent.primaryDark,
        secondary = accent.primaryDark.copy(alpha = 0.85f),
        onSecondary = Color(0xFF0F172A),
        secondaryContainer = accent.primaryDark.copy(alpha = 0.15f),
        onSecondaryContainer = accent.primaryDark,
        tertiary = Color(0xFF34D399),
        onTertiary = Color(0xFF0F172A),
        background = Color(0xFF090D16),
        onBackground = Color(0xFFF1F5F9),
        surface = Color(0xFF111827),
        onSurface = Color(0xFFF1F5F9),
        surfaceVariant = Color(0xFF1F2937),
        onSurfaceVariant = Color(0xFF9CA3AF),
        outline = Color(0xFF374151),
        outlineVariant = Color(0xFF1F2937)
    )
}

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    accentColor: AccentColorOption = AccentColorOption.BLUE,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) {
        buildDarkColorScheme(accentColor)
    } else {
        buildLightColorScheme(accentColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
