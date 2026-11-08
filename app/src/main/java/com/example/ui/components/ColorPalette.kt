package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.absoluteValue

data class DeckIconItem(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object DisciplinePalette {
    // 50 cores cromáticas contínuas (5 linhas de 10 cores) alinhadas com o sistema de temas
    val selectableColors = listOf(
        // Linha 1: Vermelhos, Carmesins, Corais, Laranjas e Amarelos (10)
        "#DC2626", "#B91C1C", "#E11D48", "#9F1239", "#F97316",
        "#F43F5E", "#EA580C", "#C2410C", "#D97706", "#CA8A04",

        // Linha 2: Limas, Verdes, Esmeraldas, Menta e Turquesas (10)
        "#65A30D", "#84CC16", "#059669", "#15803D", "#166534",
        "#10B981", "#047857", "#0D9488", "#0F766E", "#14B8A6",

        // Linha 3: Cianos, Céu, Azuis, Safiras e Marinhos (10)
        "#0891B2", "#0E7490", "#0284C7", "#0369A1", "#2563EB",
        "#1D4ED8", "#1E40AF", "#1D40AF", "#1E293B", "#172554",

        // Linha 4: Índigos, Violetas, Roxos, Fúcsias, Magentas e Rosas (10)
        "#4F46E5", "#3730A3", "#8B5CF6", "#7C3AED", "#6D28D9",
        "#5B21B6", "#C026D3", "#D946EF", "#EC4899", "#BE185D",

        // Linha 5: Terracotas, Bronzes, Cafés, Cinzas, Grafites e Ônix (10)
        "#9A3412", "#7C2D12", "#B45309", "#92400E", "#78350F",
        "#451A03", "#57534E", "#475569", "#334155", "#0F172A"
    )

    // Exatamente 50 ícones selecionados para disciplinas e matérias de controle/TCU (5 linhas de 10)
    val selectableIcons = listOf(
        // Linha 1: Educação, Tribunais, Direito, Controle e Exatas (10)
        DeckIconItem("School", "Educação / Concurso", Icons.Default.School),
        DeckIconItem("AccountBalance", "Tribunal / TCU", Icons.Default.AccountBalance),
        DeckIconItem("Gavel", "Direito / Legislação", Icons.Default.Gavel),
        DeckIconItem("Balance", "Justiça / Equilíbrio", Icons.Default.Balance),
        DeckIconItem("Calculate", "Exatas / Matemática", Icons.Default.Calculate),
        DeckIconItem("Analytics", "Análise de Dados", Icons.Default.Analytics),
        DeckIconItem("Assessment", "Auditoria Governamental", Icons.Default.Assessment),
        DeckIconItem("FactCheck", "Controle Externo", Icons.Default.FactCheck),
        DeckIconItem("Policy", "Políticas Públicas", Icons.Default.Policy),
        DeckIconItem("Security", "Segurança / Defesa", Icons.Default.Security),

        // Linha 2: Raciocínio, Livros, Manuais e Organização (10)
        DeckIconItem("Psychology", "Cérebro / Raciocínio", Icons.Default.Psychology),
        DeckIconItem("AutoStories", "Doutrina / Leitura", Icons.Default.AutoStories),
        DeckIconItem("Book", "Manual / Código", Icons.Default.Book),
        DeckIconItem("MenuBook", "Vade Mecum", Icons.Default.MenuBook),
        DeckIconItem("Folder", "Pastas / Matérias", Icons.Default.Folder),
        DeckIconItem("FolderSpecial", "Prioridades", Icons.Default.FolderSpecial),
        DeckIconItem("Category", "Categorias", Icons.Default.Category),
        DeckIconItem("Class", "Videoaulas", Icons.Default.Class),
        DeckIconItem("Bookmark", "Marcadores", Icons.Default.Bookmark),
        DeckIconItem("Star", "Alta Incidência", Icons.Default.Star),

        // Linha 3: Desempenho, Metas, Questões e Tempo (10)
        DeckIconItem("Grade", "Nível Avançado", Icons.Default.Grade),
        DeckIconItem("CheckCircle", "Metas Concluídas", Icons.Default.CheckCircle),
        DeckIconItem("DoneAll", "Tópicos Dominados", Icons.Default.DoneAll),
        DeckIconItem("Build", "Resolução de Questões", Icons.Default.Build),
        DeckIconItem("Quiz", "Simulados", Icons.Default.Quiz),
        DeckIconItem("Timer", "Cronômetro", Icons.Default.Timer),
        DeckIconItem("Speed", "Velocidade de Resolução", Icons.Default.Speed),
        DeckIconItem("TrendingUp", "Evolução SRS", Icons.Default.TrendingUp),
        DeckIconItem("Timeline", "Cronograma", Icons.Default.Timeline),
        DeckIconItem("Lightbulb", "Mnemônicos", Icons.Default.Lightbulb),

        // Linha 4: Carreira Pública, Idiomas, Peças e Finanças (10)
        DeckIconItem("TipsAndUpdates", "Dicas de Banca", Icons.Default.TipsAndUpdates),
        DeckIconItem("Work", "Auditor Federal", Icons.Default.Work),
        DeckIconItem("BusinessCenter", "Administração Pública", Icons.Default.BusinessCenter),
        DeckIconItem("Public", "Direito Internacional", Icons.Default.Public),
        DeckIconItem("Language", "Língua Portuguesa", Icons.Default.Language),
        DeckIconItem("Edit", "Discursivas", Icons.Default.Edit),
        DeckIconItem("Description", "Pareceres Técnicos", Icons.Default.Description),
        DeckIconItem("Article", "Legislação Seca", Icons.Default.Article),
        DeckIconItem("ReceiptLong", "Orçamento / AFO", Icons.Default.ReceiptLong),
        DeckIconItem("Payments", "Contabilidade Pública", Icons.Default.Payments),

        // Linha 5: Finanças, TI, Dados, Metas e Conquistas (10)
        DeckIconItem("MonetizationOn", "Finanças / Tributos", Icons.Default.MonetizationOn),
        DeckIconItem("Code", "TI / Programação", Icons.Default.Code),
        DeckIconItem("Computer", "Informática Básica", Icons.Default.Computer),
        DeckIconItem("Storage", "Engenharia de Dados", Icons.Default.Storage),
        DeckIconItem("Cloud", "Governança de Nuvem", Icons.Default.Cloud),
        DeckIconItem("Memory", "Hardware / Redes", Icons.Default.Memory),
        DeckIconItem("Flag", "Marco Regulatório", Icons.Default.Flag),
        DeckIconItem("Explore", "Exploração de Tópicos", Icons.Default.Explore),
        DeckIconItem("WorkspacePremium", "Aprovação TCU", Icons.Default.WorkspacePremium),
        DeckIconItem("MilitaryTech", "Posse / Vaga", Icons.Default.MilitaryTech)
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

    fun removeCustom(disciplineName: String) {
        if (disciplineName.isNotBlank()) {
            val key = disciplineName.trim().lowercase()
            customColors.remove(key)
            customIcons.remove(key)
        }
    }

    fun getIconKeyForDiscipline(disciplineName: String): String {
        val key = disciplineName.trim().lowercase()
        if (customIcons.containsKey(key)) {
            return customIcons[key]!!
        }

        return when {
            // 17 Disciplinas Oficiais TCU por sigla ou nome
            key == "cex" || key.contains("controle externo") || key.contains("tcu") || key.contains("tce") -> "FactCheck"
            key == "afo" || key.contains("financeiro") || key.contains("orçament") || key.contains("afo") -> "ReceiptLong"
            key == "aud" || key.contains("auditoria") -> "Assessment"
            key == "con" || key.contains("constitucional") -> "Gavel"
            key == "adm" || key.contains("administrativo") -> "Balance"
            key == "cpb" || key.contains("contabilidade pública") || key.contains("mcasp") -> "Payments"
            key == "cge" || key == "ctg" || key.contains("contabilidade geral") || key.contains("contabil") -> "Calculate"
            key == "ti" || key.startsWith("ti ") || key.contains(" ti ") || key.endsWith(" ti") ||
                key.contains("informática") || key.contains("tecnologia") || key.contains("dados") -> "Analytics"
            key == "eco" || key.contains("economia") -> "MonetizationOn"
            key == "por" || key.contains("português") || key.contains("lingua portuguesa") -> "Language"
            key == "ing" || key.contains("inglês") || key.contains("ingles") || key.contains("english") -> "Public"
            key == "rlm" || key.contains("raciocínio") || key.contains("raciocinio") || key.contains("lógica") -> "Psychology"
            key == "est" || key.contains("estatística") || key.contains("estatistica") -> "Timeline"
            key == "civ" || key.contains("civil") && !key.contains("process") -> "Article"
            key == "cpc" || key == "prc" || key.contains("processo civil") || key.contains("processual civil") -> "Description"
            key == "pen" || key.contains("penal") -> "Security"
            key == "pub" || key.contains("administração pública") || key.contains("gestão pública") -> "BusinessCenter"
            else -> "School"
        }
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

        // Exact matches for 17 official TCU discipline acronyms
        when (lower) {
            "cex" -> return "#7C3AED"
            "afo" -> return "#9333EA"
            "aud" -> return "#16A34A"
            "con" -> return "#2563EB"
            "adm" -> return "#D97706"
            "cpb" -> return "#CA8A04"
            "cge", "ctg" -> return "#B45309"
            "ti" -> return "#0891B2"
            "eco" -> return "#059669"
            "por" -> return "#3B82F6"
            "ing" -> return "#0D9488"
            "rlm" -> return "#6366F1"
            "est" -> return "#4F46E5"
            "civ" -> return "#1D4ED8"
            "cpc", "prc" -> return "#0E7490"
            "pen" -> return "#DC2626"
            "pub" -> return "#EA580C"
        }

        // Keyword-based matches for natural discipline colors (ordered carefully to prevent collisions)
        return when {
            lower.contains("constitucional") -> "#2563EB"
            lower.contains("administrativo") -> "#D97706"
            lower.contains("controle") || lower.contains("tcu") || lower.contains("tce") -> "#7C3AED"
            lower.contains("tribut") || lower.contains("fiscal") -> "#059669"
            lower.contains("processo civil") || lower.contains("processual civil") -> "#0E7490"
            lower.contains("processo penal") || lower.contains("processual penal") -> "#BE123C"
            lower.contains("penal") -> "#DC2626"
            lower.contains("civil") -> "#1D4ED8"
            lower.contains("trabalho") -> "#EA580C"
            lower.contains("previdenci") -> "#0284C7"
            lower.contains("financeiro") || lower.contains("orçament") -> "#9333EA"
            lower.contains("auditoria") -> "#16A34A"
            lower.contains("contabilidade pública") || lower.contains("mcasp") -> "#CA8A04"
            lower.contains("contabil") -> "#B45309"
            lower.contains("português") || lower.contains("lingua portuguesa") -> "#3B82F6"
            lower.contains("inglês") || lower.contains("ingles") || lower.contains("english") -> "#0D9488"
            lower.contains("raciocínio") || lower.contains("lógica") -> "#6366F1"
            lower.contains("estatística") || lower.contains("estatistica") -> "#4F46E5"
            lower.contains("economia") -> "#059669"
            lower.contains("administração pública") || lower.contains("gestão pública") -> "#EA580C"
            // TI checks with bounded word checks to avoid false positives (e.g., "estatística")
            lower == "ti" || lower.startsWith("ti ") || lower.contains(" ti ") || lower.endsWith(" ti") ||
                lower.contains("informática") || lower.contains("tecnologia") || lower.contains("dados") -> "#0891B2"
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
