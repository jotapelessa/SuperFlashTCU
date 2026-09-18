package com.example.data.model

data class DayStudyStat(
    val dayLabel: String,
    val dateFormatted: String,
    val timestamp: Long,
    val reviewsCount: Int,
    val uniqueCardsCount: Int,
    val isToday: Boolean = false
)

data class DomainMasteryStat(
    val domainName: String,
    val colorHex: String,
    val totalCards: Int,
    val masteredCards: Int,
    val learningCards: Int,
    val newCards: Int,
    val masteryPercentage: Float
)

data class StudyProgressReport(
    val totalCards: Int = 0,
    val totalReviews: Int = 0,
    val currentStreakDays: Int = 0,
    val overallMasteryPercentage: Float = 0f,
    val studyFrequency7Days: List<DayStudyStat> = emptyList(),
    val domainMasteryList: List<DomainMasteryStat> = emptyList(),
    val dueNowCount: Int = 0,
    val learningCount: Int = 0,
    val masteredCount: Int = 0,
    val newCount: Int = 0
)

enum class StudyFilterMode(val label: String, val description: String) {
    ALL("Todos os Cards", "Estuda todos os cards do escopo selecionado"),
    DUE_ONLY("Apenas Vencidos", "Cards programados para revisão imediata"),
    NEW_AND_LEARNING("Novos & Aprendendo", "Cards nunca vistos ou em fase de fixação"),
    DIFFICULT_ONLY("Cards Difíceis", "Cards com lapsos ou baixo índice de retenção")
}

enum class PerCardTimeLimit(val seconds: Int, val label: String) {
    UNLIMITED(0, "Livre (Sem limite)"),
    FIFTEEN_SEC(15, "15 segundos"),
    THIRTY_SEC(30, "30 segundos"),
    SIXTY_SEC(60, "1 minuto")
}

data class StudyTimerConfig(
    val perCardLimit: PerCardTimeLimit = PerCardTimeLimit.UNLIMITED,
    val targetSessionMinutes: Int = 0 // 0 = sem meta fixa
)

data class DeckTimeSpent(
    val deckName: String,
    val l1: String,
    val l2: String,
    val timeMillis: Long,
    val cardsCount: Int,
    val colorHex: String = "#3B82F6"
) {
    val formattedTime: String
        get() {
            val totalSeconds = (timeMillis / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return when {
                minutes > 0 -> "${minutes}m ${seconds}s"
                seconds > 0 -> "${seconds}s"
                timeMillis > 0 -> "< 1s"
                else -> "0s"
            }
        }

    val avgSecondsPerCard: Float
        get() = if (cardsCount > 0) (timeMillis / 1000f) / cardsCount else 0f
}

data class SessionTimeReport(
    val totalTimeMillis: Long = 0L,
    val deckBreakdown: List<DeckTimeSpent> = emptyList(),
    val averageSecondsPerCard: Float = 0f
) {
    val formattedTotalTime: String
        get() {
            val totalSeconds = (totalTimeMillis / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return when {
                minutes > 0 -> "${minutes}m ${seconds}s"
                seconds > 0 -> "${seconds}s"
                totalTimeMillis > 0 -> "< 1s"
                else -> "0s"
            }
        }

    val paceLabel: String
        get() = when {
            averageSecondsPerCard <= 0f -> "Não cronometrado"
            averageSecondsPerCard < 15f -> "Ritmo Rápido ⚡"
            averageSecondsPerCard < 35f -> "Ritmo Moderado ⏱️"
            else -> "Ritmo Analítico 🔍"
        }
}

data class StudySessionConfig(
    val l1: String? = null,
    val l2: String? = null,
    val l3: String? = null,
    val tag: String? = null,
    val mode: StudyFilterMode = StudyFilterMode.DUE_ONLY,
    val cardLimit: Int = 20,
    val smartShuffle: Boolean = true,
    val timerConfig: StudyTimerConfig = StudyTimerConfig()
)

data class SessionLiveStats(
    val totalSessionCards: Int = 0,
    val completedCards: Int = 0,
    val againCount: Int = 0,
    val hardCount: Int = 0,
    val goodCount: Int = 0,
    val easyCount: Int = 0,
    val graduatedToMasteredCount: Int = 0,
    val timeReport: SessionTimeReport = SessionTimeReport()
) {
    val successRate: Float
        get() = if (completedCards > 0) ((goodCount + easyCount).toFloat() / completedCards) * 100f else 0f
}
