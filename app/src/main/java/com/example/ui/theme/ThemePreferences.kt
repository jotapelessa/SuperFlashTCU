package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val label: String, val icon: String) {
    SYSTEM("Sistema", "📱"),
    LIGHT("Modo Claro", "☀️"),
    DARK("Modo Escuro", "🌙")
}

enum class AccentColorOption(
    val title: String,
    val primaryLight: Color,
    val primaryDark: Color,
    val previewColor: Color
) {
    BLUE("Azul Real", Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFF2563EB)),
    INDIGO("Índigo Profundo", Color(0xFF4F46E5), Color(0xFF818CF8), Color(0xFF4F46E5)),
    PURPLE("Roxo Anki", Color(0xFF7C3AED), Color(0xFFA78BFA), Color(0xFF7C3AED)),
    VIOLET("Violeta Elétrico", Color(0xFF8B5CF6), Color(0xFFC4B5FD), Color(0xFF8B5CF6)),
    MAGENTA("Rosa Magenta", Color(0xFFD946EF), Color(0xFFE879F9), Color(0xFFD946EF)),
    PINK("Rosa Choque", Color(0xFFEC4899), Color(0xFFF472B6), Color(0xFFEC4899)),
    ROSE("Rosa Carmesim", Color(0xFFE11D48), Color(0xFFFB7185), Color(0xFFE11D48)),
    RED("Vermelho Rubim", Color(0xFFDC2626), Color(0xFFF87171), Color(0xFFDC2626)),
    CORAL("Coral Vívido", Color(0xFFF97316), Color(0xFFFB923C), Color(0xFFF97316)),
    ORANGE("Laranja Âmbar", Color(0xFFEA580C), Color(0xFFFDBA74), Color(0xFFEA580C)),
    AMBER("Dourado Âmbar", Color(0xFFD97706), Color(0xFFFBBF24), Color(0xFFD97706)),
    YELLOW("Amarelo Solar", Color(0xFFCA8A04), Color(0xFFFACC15), Color(0xFFCA8A04)),
    LIME("Verde Lima", Color(0xFF65A30D), Color(0xFFA3E635), Color(0xFF65A30D)),
    GREEN("Verde Esmeralda", Color(0xFF059669), Color(0xFF34D399), Color(0xFF059669)),
    EMERALD("Verde Menta", Color(0xFF10B981), Color(0xFF6EE7B7), Color(0xFF10B981)),
    TEAL("Ciano Turquesa", Color(0xFF0D9488), Color(0xFF2DD4BF), Color(0xFF0D9488)),
    CYAN("Ciano Oceano", Color(0xFF0891B2), Color(0xFF38BDF8), Color(0xFF0891B2)),
    SKY("Azul Céu", Color(0xFF0284C7), Color(0xFF7DD3FC), Color(0xFF0284C7)),
    ULTRAMARINE("Azul Cobalto", Color(0xFF1E40AF), Color(0xFF93C5FD), Color(0xFF1E40AF)),
    NAVY("Azul Marinho", Color(0xFF1E293B), Color(0xFF64748B), Color(0xFF1E293B)),
    SLATE("Cinza Grafite", Color(0xFF475569), Color(0xFF94A3B8), Color(0xFF475569)),
    BROWN("Café Especial", Color(0xFF78350F), Color(0xFFD97706), Color(0xFF78350F))
}
