package com.example

import com.example.data.model.L2DisciplineSummary
import com.example.data.model.L3TopicSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyStudyGoalTest {

    @Test
    fun testL3DailyTargetCalculation_percentageCeilingAndMinimum() {
        // Subtópico com 30 cards a 5%: 30 * 0.05 = 1.5 -> teto = 2
        val topic30 = L3TopicSummary(
            l1 = "Direito",
            l2 = "Constitucional",
            l3 = "Direitos Fundamentais",
            totalCards = 30,
            dueCards = 0,
            masteredCards = 0,
            learningCards = 0,
            newCards = 30,
            colorHex = "#2196F3",
            dailyGoalPercentage = 5f
        )
        assertEquals(2, topic30.dailyTargetCards)

        // Subtópico com 100 cards a 5%: 100 * 0.05 = 5
        val topic100 = L3TopicSummary(
            l1 = "Direito",
            l2 = "Constitucional",
            l3 = "Organização do Estado",
            totalCards = 100,
            dueCards = 0,
            masteredCards = 0,
            learningCards = 0,
            newCards = 100,
            colorHex = "#2196F3",
            dailyGoalPercentage = 5f
        )
        assertEquals(5, topic100.dailyTargetCards)

        // Subtópico com 124 cards a 5%: 124 * 0.05 = 6.2 -> teto = 7
        val topic124 = L3TopicSummary(
            l1 = "Direito",
            l2 = "Constitucional",
            l3 = "Poder Judiciário",
            totalCards = 124,
            dueCards = 0,
            masteredCards = 0,
            learningCards = 0,
            newCards = 124,
            colorHex = "#2196F3",
            dailyGoalPercentage = 5f
        )
        assertEquals(7, topic124.dailyTargetCards)

        // Subtópico minúsculo (3 cards a 5%): 3 * 0.05 = 0.15 -> mínimo garantido = 1
        val topic3 = L3TopicSummary(
            l1 = "Direito",
            l2 = "Constitucional",
            l3 = "Preâmbulo",
            totalCards = 3,
            dueCards = 0,
            masteredCards = 0,
            learningCards = 0,
            newCards = 3,
            colorHex = "#2196F3",
            dailyGoalPercentage = 5f
        )
        assertEquals(1, topic3.dailyTargetCards)
    }

    @Test
    fun testL3GoalProgressAndStatus() {
        val topic = L3TopicSummary(
            l1 = "Direito",
            l2 = "Constitucional",
            l3 = "Controle de Constitucionalidade",
            totalCards = 100,
            dueCards = 10,
            masteredCards = 20,
            learningCards = 10,
            newCards = 70,
            colorHex = "#2196F3",
            reviewedToday = 3,
            dailyGoalPercentage = 5f
        )

        // Meta é 5 cards, estudou 3 -> 60%, não concluída
        assertEquals(5, topic.dailyTargetCards)
        assertEquals(60f, topic.dailyProgressPercentage, 0.01f)
        assertFalse(topic.isDailyGoalMet)

        val topicCompleted = topic.copy(reviewedToday = 5)
        assertEquals(100f, topicCompleted.dailyProgressPercentage, 0.01f)
        assertTrue(topicCompleted.isDailyGoalMet)

        // Mais que a meta (overachievement) -> limita a barra em 100%, mas mantém goalMet = true
        val topicOver = topic.copy(reviewedToday = 8)
        assertEquals(100f, topicOver.dailyProgressPercentage, 0.01f)
        assertTrue(topicOver.isDailyGoalMet)
    }

    @Test
    fun testL2ConsolidatedGoalsSumFromTopics() {
        // Matéria com 3 tópicos de tamanhos variados
        val t1 = L3TopicSummary("D", "Const", "T1", totalCards = 30, dueCards = 0, masteredCards = 0, learningCards = 0, newCards = 30, colorHex = "#000", dailyGoalPercentage = 5f, reviewedToday = 2) // target 2
        val t2 = L3TopicSummary("D", "Const", "T2", totalCards = 100, dueCards = 0, masteredCards = 0, learningCards = 0, newCards = 100, colorHex = "#000", dailyGoalPercentage = 5f, reviewedToday = 3) // target 5
        val t3 = L3TopicSummary("D", "Const", "T3", totalCards = 124, dueCards = 0, masteredCards = 0, learningCards = 0, newCards = 124, colorHex = "#000", dailyGoalPercentage = 5f, reviewedToday = 7) // target 7

        val l2 = L2DisciplineSummary(
            l1 = "D",
            l2 = "Const",
            totalCards = 254,
            dueCards = 0,
            masteredCards = 0,
            learningCards = 0,
            newCards = 254,
            l3Count = 3,
            colorHex = "#000",
            topics = listOf(t1, t2, t3),
            reviewedToday = 12, // 2 + 3 + 7 = 12
            dailyGoalPercentage = 5f
        )

        // Soma das metas dos tópicos: 2 + 5 + 7 = 14
        assertEquals(14, l2.dailyTargetCards)
        assertEquals(12, l2.reviewedToday)
        assertFalse(l2.isDailyGoalMet)
        assertEquals((12f / 14f) * 100f, l2.dailyProgressPercentage, 0.01f)

        // Se bater os 14 cards
        val l2Met = l2.copy(reviewedToday = 14)
        assertTrue(l2Met.isDailyGoalMet)
        assertEquals(100f, l2Met.dailyProgressPercentage, 0.01f)
    }

    @Test
    fun testPercentageAdjustment10Percent() {
        // Subtópico com 124 cards a 10%: 124 * 0.10 = 12.4 -> teto = 13 cards
        val topic10Pct = L3TopicSummary(
            l1 = "D",
            l2 = "Const",
            l3 = "Tópico",
            totalCards = 124,
            dueCards = 0,
            masteredCards = 0,
            learningCards = 0,
            newCards = 124,
            colorHex = "#000",
            dailyGoalPercentage = 10f
        )
        assertEquals(13, topic10Pct.dailyTargetCards)
    }

    @Test
    fun testL1DeckSummaryDailyTarget_percentage5To15() {
        // Concurso L1 com 100 cards a 10%: 100 * 0.10 = 10 cards
        val l1_100 = com.example.data.model.L1DeckSummary(
            l1 = "TCU",
            totalCards = 100,
            dueCards = 10,
            masteredCards = 20,
            learningCards = 30,
            newCards = 50,
            l2Count = 5,
            l3Count = 15,
            dailyGoalPercentage = 10f
        )
        assertEquals(10, l1_100.dailyTargetCards)

        // Concurso L1 com 350 cards a 10%: 350 * 0.10 = 35 cards
        val l1_350 = l1_100.copy(totalCards = 350)
        assertEquals(35, l1_350.dailyTargetCards)

        // Concurso L1 com 150 cards a 15%: 150 * 0.15 = 22.5 -> teto = 23 cards
        val l1_150_pct15 = l1_100.copy(totalCards = 150, dailyGoalPercentage = 15f)
        assertEquals(23, l1_150_pct15.dailyTargetCards)

        // Concurso L1 pequeno (17 cards a 5%): 17 * 0.05 = 0.85 -> teto = 1 card mínimo
        val l1_17_pct5 = l1_100.copy(totalCards = 17, dailyGoalPercentage = 5f)
        assertEquals(1, l1_17_pct5.dailyTargetCards)

        // Concurso L1 sem cards = 0 cards de meta
        val l1_empty = l1_100.copy(totalCards = 0)
        assertEquals(0, l1_empty.dailyTargetCards)
    }

    @Test
    fun testL1DeckSummaryDailyGoalProgressAndStatus() {
        val l1 = com.example.data.model.L1DeckSummary(
            l1 = "TCU",
            totalCards = 200,
            dueCards = 20,
            masteredCards = 50,
            learningCards = 50,
            newCards = 100,
            l2Count = 4,
            l3Count = 12,
            reviewedToday = 15,
            dailyGoalPercentage = 10f // meta: 200 * 0.10 = 20 cards
        )

        assertEquals(20, l1.dailyTargetCards)
        assertEquals(15, l1.reviewedToday)
        assertFalse(l1.isDailyGoalMet)
        assertEquals(75f, l1.dailyProgressPercentage, 0.01f) // 15 / 20 = 75%

        val l1Completed = l1.copy(reviewedToday = 20)
        assertTrue(l1Completed.isDailyGoalMet)
        assertEquals(100f, l1Completed.dailyProgressPercentage, 0.01f)

        // Mais que a meta (overachievement) -> limita a 100% de progresso
        val l1Over = l1.copy(reviewedToday = 25)
        assertTrue(l1Over.isDailyGoalMet)
        assertEquals(100f, l1Over.dailyProgressPercentage, 0.01f)
    }

    @Test
    fun testMultipleL1DecksConsolidatedGoalSum() {
        val tcu = com.example.data.model.L1DeckSummary(
            l1 = "TCU",
            totalCards = 500,
            dueCards = 0,
            masteredCards = 0,
            learningCards = 0,
            newCards = 500,
            l2Count = 10,
            l3Count = 30,
            dailyGoalPercentage = 10f
        ) // 500 * 0.10 = 50 cards
        val cgu = com.example.data.model.L1DeckSummary(
            l1 = "CGU",
            totalCards = 350,
            dueCards = 0,
            masteredCards = 0,
            learningCards = 0,
            newCards = 350,
            l2Count = 8,
            l3Count = 20,
            dailyGoalPercentage = 10f
        ) // 350 * 0.10 = 35 cards

        val l1List = listOf(tcu, cgu)
        val consolidatedDailyTarget = l1List.sumOf { it.dailyTargetCards }
        assertEquals(85, consolidatedDailyTarget)
    }
}
