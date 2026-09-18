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
    // Linha 1: Vermelhos, Carmesins, Corais, Laranjas e Amarelos (10)
    RED("Vermelho Rubim", Color(0xFFDC2626), Color(0xFFF87171), Color(0xFFDC2626)),
    CRIMSON("Carmesim", Color(0xFFB91C1C), Color(0xFFFCA5A5), Color(0xFFB91C1C)),
    ROSE("Rosa Carmesim", Color(0xFFE11D48), Color(0xFFFB7185), Color(0xFFE11D48)),
    RUBY("Rubi Profundo", Color(0xFF9F1239), Color(0xFFFECDD3), Color(0xFF9F1239)),
    CORAL("Coral Vívido", Color(0xFFF97316), Color(0xFFFB923C), Color(0xFFF97316)),
    FLAME("Chama Solar", Color(0xFFF43F5E), Color(0xFFFDA4AF), Color(0xFFF43F5E)),
    ORANGE("Laranja Âmbar", Color(0xFFEA580C), Color(0xFFFDBA74), Color(0xFFEA580C)),
    TANGERINE("Tangerina", Color(0xFFC2410C), Color(0xFFFED7AA), Color(0xFFC2410C)),
    AMBER("Dourado Âmbar", Color(0xFFD97706), Color(0xFFFBBF24), Color(0xFFD97706)),
    YELLOW("Amarelo Solar", Color(0xFFCA8A04), Color(0xFFFACC15), Color(0xFFCA8A04)),

    // Linha 2: Limas, Verdes, Esmeraldas, Menta e Turquesas (10)
    LIME("Verde Lima", Color(0xFF65A30D), Color(0xFFA3E635), Color(0xFF65A30D)),
    CHARTREUSE("Verde Citrino", Color(0xFF84CC16), Color(0xFFBEF264), Color(0xFF84CC16)),
    GREEN("Verde Esmeralda", Color(0xFF059669), Color(0xFF34D399), Color(0xFF059669)),
    FOREST("Verde Floresta", Color(0xFF15803D), Color(0xFF86EFAC), Color(0xFF15803D)),
    JADE("Verde Jade", Color(0xFF166534), Color(0xFFBBF7D0), Color(0xFF166534)),
    EMERALD("Verde Menta", Color(0xFF10B981), Color(0xFF6EE7B7), Color(0xFF10B981)),
    MINT("Menta Suave", Color(0xFF047857), Color(0xFFA7F3D0), Color(0xFF047857)),
    TEAL("Ciano Turquesa", Color(0xFF0D9488), Color(0xFF2DD4BF), Color(0xFF0D9488)),
    PINE("Verde Petróleo", Color(0xFF0F766E), Color(0xFF5EEAD4), Color(0xFF0F766E)),
    AQUA("Água Marinha", Color(0xFF14B8A6), Color(0xFF99F6E4), Color(0xFF14B8A6)),

    // Linha 3: Cianos, Céu, Azuis, Safiras e Marinhos (10)
    CYAN("Ciano Oceano", Color(0xFF0891B2), Color(0xFF38BDF8), Color(0xFF0891B2)),
    LAGOON("Azul Lagoa", Color(0xFF0E7490), Color(0xFF67E8F9), Color(0xFF0E7490)),
    SKY("Azul Céu", Color(0xFF0284C7), Color(0xFF7DD3FC), Color(0xFF0284C7)),
    CERULEAN("Azul Cerúleo", Color(0xFF0369A1), Color(0xFFBAE6FD), Color(0xFF0369A1)),
    BLUE("Azul Real", Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFF2563EB)),
    SAPPHIRE("Azul Safira", Color(0xFF1D4ED8), Color(0xFF93C5FD), Color(0xFF1D4ED8)),
    ULTRAMARINE("Azul Cobalto", Color(0xFF1E40AF), Color(0xFF93C5FD), Color(0xFF1E40AF)),
    PACIFIC("Azul Pacífico", Color(0xFF1D40AF), Color(0xFFBFDBFE), Color(0xFF1D40AF)),
    NAVY("Azul Marinho", Color(0xFF1E293B), Color(0xFF64748B), Color(0xFF1E293B)),
    MIDNIGHT("Azul Meia-Noite", Color(0xFF172554), Color(0xFF93C5FD), Color(0xFF172554)),

    // Linha 4: Índigos, Violetas, Roxos, Fúcsias, Magentas e Rosas (10)
    INDIGO("Índigo Profundo", Color(0xFF4F46E5), Color(0xFF818CF8), Color(0xFF4F46E5)),
    INDIGO_DARK("Índigo Noite", Color(0xFF3730A3), Color(0xFFA5B4FC), Color(0xFF3730A3)),
    VIOLET("Violeta Elétrico", Color(0xFF8B5CF6), Color(0xFFC4B5FD), Color(0xFF8B5CF6)),
    PURPLE("Roxo Anki", Color(0xFF7C3AED), Color(0xFFA78BFA), Color(0xFF7C3AED)),
    AMETHYST("Ametista", Color(0xFF6D28D9), Color(0xFFDDD6FE), Color(0xFF6D28D9)),
    PLUM("Ameixa Profunda", Color(0xFF5B21B6), Color(0xFFEDE9FE), Color(0xFF5B21B6)),
    FUCHSIA("Fúcsia Vibrante", Color(0xFFC026D3), Color(0xFFE879F9), Color(0xFFC026D3)),
    MAGENTA("Rosa Magenta", Color(0xFFD946EF), Color(0xFFF0ABFC), Color(0xFFD946EF)),
    PINK("Rosa Choque", Color(0xFFEC4899), Color(0xFFF472B6), Color(0xFFEC4899)),
    BERRY("Framboesa", Color(0xFFBE185D), Color(0xFFFBCFE8), Color(0xFFBE185D)),

    // Linha 5: Terracotas, Bronzes, Cafés, Cinzas, Grafites e Ônix (10)
    TERRACOTTA("Terracota", Color(0xFF9A3412), Color(0xFFFDBA74), Color(0xFF9A3412)),
    RUST("Ferrugem Nobre", Color(0xFF7C2D12), Color(0xFFFFEDD5), Color(0xFF7C2D12)),
    COPPER("Cobre Escuro", Color(0xFFB45309), Color(0xFFFDE68A), Color(0xFFB45309)),
    BRONZE("Bronze Antigo", Color(0xFF92400E), Color(0xFFFEF3C7), Color(0xFF92400E)),
    BROWN("Café Especial", Color(0xFF78350F), Color(0xFFD97706), Color(0xFF78350F)),
    COFFEE("Café Torrado", Color(0xFF451A03), Color(0xFFD6D3D1), Color(0xFF451A03)),
    STONE("Cinza Quartzo", Color(0xFF57534E), Color(0xFFA8A29E), Color(0xFF57534E)),
    SLATE("Cinza Grafite", Color(0xFF475569), Color(0xFF94A3B8), Color(0xFF475569)),
    STEEL("Aço Escuro", Color(0xFF334155), Color(0xFFCBD5E1), Color(0xFF334155)),
    GRAPHITE("Preto Ônix", Color(0xFF0F172A), Color(0xFF94A3B8), Color(0xFF0F172A))
}
