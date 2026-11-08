package com.example.data.model

data class L1DeckSummary(
    val l1: String,
    val totalCards: Int,
    val dueCards: Int,
    val masteredCards: Int,
    val learningCards: Int,
    val newCards: Int,
    val l2Count: Int,
    val l3Count: Int,
    val disciplineColors: List<String> = emptyList(),
    val cardColorHex: String? = null,
    val courseName: String? = null,
    val coverUrl: String? = null,
    val isAiAnalysisEnabled: Boolean = true,
    val reviewedToday: Int = 0,
    val dailyGoalPercentage: Float = 10f
) {
    val masteryPercentage: Float
        get() = if (totalCards > 0) ((masteredCards * 1.0f + learningCards * 0.4f) / totalCards) * 100f else 0f

    val dailyTargetCards: Int
        get() = if (totalCards > 0) {
            kotlin.math.max(1, kotlin.math.ceil(totalCards * (dailyGoalPercentage / 100f)).toInt())
        } else 0

    val isDailyGoalMet: Boolean
        get() = dailyTargetCards > 0 && reviewedToday >= dailyTargetCards

    val dailyProgressPercentage: Float
        get() = if (dailyTargetCards > 0) {
            kotlin.math.min(100f, (reviewedToday.toFloat() / dailyTargetCards) * 100f)
        } else 0f
}

data class L2DisciplineSummary(
    val l1: String,
    val l2: String,
    val totalCards: Int,
    val dueCards: Int,
    val masteredCards: Int,
    val learningCards: Int,
    val newCards: Int,
    val l3Count: Int,
    val colorHex: String,
    val iconKey: String = "School",
    val topics: List<L3TopicSummary> = emptyList(),
    val reviewedToday: Int = 0,
    val dailyGoalPercentage: Float = 5f
) {
    val masteryPercentage: Float
        get() = if (totalCards > 0) ((masteredCards * 1.0f + learningCards * 0.4f) / totalCards) * 100f else 0f

    val dailyTargetCards: Int
        get() = if (topics.isNotEmpty()) {
            topics.sumOf { it.dailyTargetCards }
        } else if (totalCards > 0) {
            kotlin.math.max(1, kotlin.math.ceil(totalCards * (dailyGoalPercentage / 100f)).toInt())
        } else 0

    val isDailyGoalMet: Boolean
        get() = dailyTargetCards > 0 && reviewedToday >= dailyTargetCards

    val dailyProgressPercentage: Float
        get() = if (dailyTargetCards > 0) {
            kotlin.math.min(100f, (reviewedToday.toFloat() / dailyTargetCards) * 100f)
        } else 0f
}

data class L3TopicSummary(
    val l1: String,
    val l2: String,
    val l3: String,
    val totalCards: Int,
    val dueCards: Int,
    val masteredCards: Int,
    val learningCards: Int,
    val newCards: Int,
    val colorHex: String,
    val reviewedToday: Int = 0,
    val dailyGoalPercentage: Float = 5f
) {
    val masteryPercentage: Float
        get() = if (totalCards > 0) ((masteredCards * 1.0f + learningCards * 0.4f) / totalCards) * 100f else 0f

    val dailyTargetCards: Int
        get() = if (totalCards > 0) {
            kotlin.math.max(1, kotlin.math.ceil(totalCards * (dailyGoalPercentage / 100f)).toInt())
        } else 0

    val isDailyGoalMet: Boolean
        get() = dailyTargetCards > 0 && reviewedToday >= dailyTargetCards

    val dailyProgressPercentage: Float
        get() = if (dailyTargetCards > 0) {
            kotlin.math.min(100f, (reviewedToday.toFloat() / dailyTargetCards) * 100f)
        } else 0f
}

data class DomainStats(
    val total: Int = 0,
    val mastered: Int = 0,
    val learning: Int = 0,
    val newCards: Int = 0,
    val masteryPercentage: Float = 0f
)

data class ReviewStats(
    val dueNow: Int = 0,
    val dueToday: Int = 0,
    val totalReviewed: Int = 0,
    val totalReviewsCount: Int = 0,
    val retentionRate: Float = 0f,
    val averageIntervalDays: Float = 0f
)
