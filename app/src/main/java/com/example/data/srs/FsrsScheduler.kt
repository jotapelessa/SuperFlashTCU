package com.example.data.srs

import com.example.data.model.FlashcardEntity
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Native implementation of the Free Spaced Repetition Scheduler (FSRS-5)
 * based on the open-spaced-repetition specification.
 *
 * Models human memory through the DSR (Difficulty, Stability, Retrievability) architecture.
 */
object FsrsScheduler {

    /**
     * FSRS-5 default global pre-trained weights (19 parameters).
     * Validated across millions of review logs by the open-spaced-repetition community.
     */
    val DEFAULT_WEIGHTS = doubleArrayOf(
        0.40255, 1.18385, 3.173, 15.69105, 7.1949, 0.5345, 1.4604, 0.0046, 1.54575, 0.1192,
        1.01925, 1.9395, 0.11, 0.29605, 2.2698, 0.2315, 2.9898, 0.51655, 0.6621
    )

    private const val FACTOR = 19.0 / 81.0 // 0.2345679...
    private const val DECAY = -0.5

    data class FsrsResult(
        val stability: Double,
        val difficulty: Double,
        val intervalDays: Int
    )

    data class IntervalPreview(
        val againDays: Int,
        val hardDays: Int,
        val goodDays: Int,
        val easyDays: Int
    )

    /**
     * Calculates the probability of recall (Retrievability, R) after t elapsed days since last review.
     */
    fun retrievability(elapsedDays: Double, stability: Double): Double {
        if (stability <= 0.0) return 0.0
        return (1.0 + FACTOR * (elapsedDays / stability)).pow(DECAY).coerceIn(0.0, 1.0)
    }

    /**
     * Solves the forgetting curve to find the optimal interval (in days) to achieve the target retention.
     * With DECAY = -0.5, 1/DECAY = -2.
     * I(r, S) = (S / FACTOR) * (r^-2 - 1)
     */
    fun nextInterval(stability: Double, targetRetention: Double): Int {
        if (stability <= 0.0) return 1
        val safeRetention = targetRetention.coerceIn(0.70, 0.98)
        val interval = (stability / FACTOR) * (safeRetention.pow(-2.0) - 1.0)
        return interval.roundToInt().coerceAtLeast(1)
    }

    fun initialStability(rating: Int, w: DoubleArray = DEFAULT_WEIGHTS): Double {
        val idx = (rating - 1).coerceIn(0, 3)
        return w[idx].coerceAtLeast(0.1)
    }

    fun initialDifficulty(rating: Int, w: DoubleArray = DEFAULT_WEIGHTS): Double {
        val d0 = w[4] - exp(w[5] * (rating - 1)) + 1.0
        return d0.coerceIn(1.0, 10.0)
    }

    fun nextDifficulty(d: Double, rating: Int, w: DoubleArray = DEFAULT_WEIGHTS): Double {
        val deltaD = -w[6] * (rating - 3)
        val dNew = d + deltaD * ((10.0 - d) / 9.0)
        val meanReversionTarget = w[4] - exp(w[5] * (4 - 1)) + 1.0 // D0(4)
        val dDoublePrime = w[7] * meanReversionTarget + (1.0 - w[7]) * dNew
        return dDoublePrime.coerceIn(1.0, 10.0)
    }

    fun nextRecallStability(d: Double, s: Double, r: Double, rating: Int, w: DoubleArray = DEFAULT_WEIGHTS): Double {
        val hardPenalty = if (rating == 2) w[15] else 1.0
        val easyBonus = if (rating == 4) w[16] else 1.0
        val sInc = 1.0 + exp(w[8]) * (11.0 - d) * s.pow(-w[9]) * (exp(w[10] * (1.0 - r)) - 1.0) * hardPenalty * easyBonus
        return (s * sInc).coerceAtLeast(0.1)
    }

    fun nextForgetStability(d: Double, s: Double, r: Double, w: DoubleArray = DEFAULT_WEIGHTS): Double {
        val sForget = w[11] * d.pow(-w[12]) * ((s + 1.0).pow(w[13]) - 1.0) * exp(w[14] * (1.0 - r))
        return sForget.coerceIn(0.1, max(0.1, s))
    }

    /**
     * Computes the new memory state and scheduled interval for a card given a rating and target retention.
     */
    fun schedule(
        card: FlashcardEntity,
        rating: Int,
        now: Long = System.currentTimeMillis(),
        targetRetention: Double = 0.90,
        w: DoubleArray = DEFAULT_WEIGHTS
    ): FsrsResult {
        if (card.reps == 0 || (card.stability == 0f && card.intervalDays == 0)) {
            // First time review (New Card)
            val newS = initialStability(rating, w)
            val newD = initialDifficulty(rating, w)
            val interval = nextInterval(newS, targetRetention)
            return FsrsResult(stability = newS, difficulty = newD, intervalDays = interval)
        }

        // Existing card (convert legacy SM-2 values if stability not yet recorded)
        val currentS = if (card.stability > 0f) {
            card.stability.toDouble()
        } else {
            card.intervalDays.toDouble().coerceAtLeast(1.0)
        }

        val currentD = if (card.difficulty > 0f) {
            card.difficulty.toDouble()
        } else {
            (11.0 - (card.easeFactor * 3.0)).coerceIn(1.0, 10.0)
        }

        val lastReviewed = if (card.lastReviewedTimestamp > 0L) card.lastReviewedTimestamp else (now - 24L * 3600L * 1000L)
        val elapsedDays = max(0.0, (now - lastReviewed).toDouble() / (24.0 * 3600.0 * 1000.0))
        val r = retrievability(elapsedDays, currentS)

        val newD = nextDifficulty(currentD, rating, w)
        val newS = if (rating == 1) {
            nextForgetStability(newD, currentS, r, w)
        } else {
            nextRecallStability(newD, currentS, r, rating, w)
        }

        val interval = nextInterval(newS, targetRetention)
        return FsrsResult(stability = newS, difficulty = newD, intervalDays = interval)
    }

    /**
     * Previews the next intervals in days for all 4 ratings (Again, Hard, Good, Easy)
     * to display on the study screen review buttons.
     */
    fun previewIntervals(
        card: FlashcardEntity,
        targetRetention: Double = 0.90,
        now: Long = System.currentTimeMillis()
    ): IntervalPreview {
        val againRes = schedule(card, rating = 1, now = now, targetRetention = targetRetention)
        val hardRes = schedule(card, rating = 2, now = now, targetRetention = targetRetention)
        val goodRes = schedule(card, rating = 3, now = now, targetRetention = targetRetention)
        val easyRes = schedule(card, rating = 4, now = now, targetRetention = targetRetention)

        return IntervalPreview(
            againDays = againRes.intervalDays,
            hardDays = hardRes.intervalDays,
            goodDays = goodRes.intervalDays,
            easyDays = easyRes.intervalDays
        )
    }
}
