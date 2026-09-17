package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.absoluteValue

data class DeckIconItem(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object DisciplinePalette {
    // Curated high-contrast, elegant palette for disciplines
    val selectableColors = listOf(
        "#2563EB", // Royal Blue
        "#D97706", // Amber
        "#7C3AED", // Violet
        "#059669", // Emerald
        "#0D9488", // Teal
        "#DC2626", // Crimson
        "#4F46E5", // Indigo
        "#EA580C", // Orange
        "#0284C7", // Sky
        "#9333EA", // Purple
        "#16A34A", // Green
        "#CA8A04", // Gold
        "#E11D48", // Rose
        "#0891B2", // Cyan
        "#6366F1", // Lavender
        "#EC4899"  // Pink
    )

    val selectableIcons = listOf(
        DeckIconItem("School", "Escola / Geral", Icons.Default.School),
        DeckIconItem("Folder", "Pasta / Matéria", Icons.Default.Folder),
        DeckIconItem("AutoStories", "Livro / Estudo", Icons.Default.AutoStories),
        DeckIconItem("Psychology", "Cérebro / Raciocínio", Icons.Default.Psychology),
        DeckIconItem("Star", "Destaque", Icons.Default.Star),
        DeckIconItem("Book", "Manual / Leitura", Icons.Default.Book),
        DeckIconItem("Category", "Categoria / Tópicos", Icons.Default.Category),
        DeckIconItem("Class", "Aula / Conteúdo", Icons.Default.Class),
        DeckIconItem("Bookmark", "Marcador / Revisão", Icons.Default.Bookmark),
        DeckIconItem("CheckCircle", "Aprovado / Objetivos", Icons.Default.CheckCircle),
        DeckIconItem("Build", "Ferramentas / Prática", Icons.Default.Build),
        DeckIconItem("Grade", "Excelente / Nível", Icons.Default.Grade)
    )

    private val customColors = mutableMapOf<String, String>()
    private val customIcons = mutableMapOf<String, String>()

    fun setCustomColor(disciplineName: String, hex: String) {
        if (disciplineName.isNotBlank()) {
            customColors[disciplineName.trim().lowercase()] = hex
        }
    }

    fun setCustomIcon(disciplineName: String, iconKey: String) {
        if (disciplineName.isNotBlank()) {
            customIcons[disciplineName.trim().lowercase()] = iconKey
        }
    }

    fun getIconKeyForDiscipline(disciplineName: String): String {
        val key = disciplineName.trim().lowercase()
        return customIcons[key] ?: "School"
    }

    fun getIconVector(iconKey: String): ImageVector {
        return selectableIcons.find { it.key == iconKey }?.icon ?: Icons.Default.School
    }

    fun getColorForDiscipline(disciplineName: String): String {
        if (disciplineName.isBlank() || disciplineName.equals("Geral", ignoreCase = true)) {
            return "#4B5563"
        }
        val lower = disciplineName.lowercase().trim()
        if (customColors.containsKey(lower)) {
            return customColors[lower]!!
        }

        // Keyword-based matches for natural discipline colors
        return when {
            lower.contains("constitucional") -> "#2563EB"
            lower.contains("administrativo") -> "#D97706"
            lower.contains("controle") || lower.contains("tcu") || lower.contains("tce") -> "#7C3AED"
            lower.contains("tribut") || lower.contains("fiscal") -> "#059669"
            lower.contains("processo civil") || lower.contains("processual civil") -> "#0D9488"
            lower.contains("processo penal") || lower.contains("processual penal") -> "#BE123C"
            lower.contains("penal") -> "#DC2626"
            lower.contains("civil") -> "#4F46E5"
            lower.contains("trabalho") -> "#EA580C"
            lower.contains("previdenci") -> "#0284C7"
            lower.contains("financeiro") || lower.contains("orçament") -> "#9333EA"
            lower.contains("auditoria") -> "#16A34A"
            lower.contains("contabil") -> "#CA8A04"
            lower.contains("português") || lower.contains("lingua") -> "#3B82F6"
            lower.contains("raciocínio") || lower.contains("lógica") -> "#6366F1"
            lower.contains("informática") || lower.contains("ti") -> "#0891B2"
            else -> {
                val index = lower.hashCode().absoluteValue % selectableColors.size
                selectableColors[index]
            }
        }
    }

    fun parseColor(hex: String, fallback: Color = Color(0xFF2563EB)): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorLong = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(colorLong or 0x00000000FF000000)
            } else {
                Color(colorLong)
            }
        } catch (_: Exception) {
            fallback
        }
    }
}
