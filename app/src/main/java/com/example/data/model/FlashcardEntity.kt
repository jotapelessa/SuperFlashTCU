package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    indices = [
        Index(value = ["contentHash"], unique = true),
        Index(value = ["l1"]),
        Index(value = ["l1", "l2"]),
        Index(value = ["l1", "l2", "l3"]),
        Index(value = ["dueTimestamp"]),
        Index(value = ["l1", "dueTimestamp"]),
        Index(value = ["l1", "masteryLevel"]),
        Index(value = ["l1", "l2", "dueTimestamp"]),
        Index(value = ["l1", "l2", "l3", "dueTimestamp"])
    ]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckRaw: String,
    val l1: String,
    val l2: String,
    val l3: String,
    val noteType: String = "Básico+",
    val frontHtml: String,
    val backHtml: String,
    val tags: String = "",
    val contentHash: String,
    val intervalDays: Int = 0,
    val easeFactor: Float = 2.5f,
    val reps: Int = 0,
    val lapses: Int = 0,
    val masteryLevel: Int = 0, // 0 = Novo, 1 = Em Aprendizado, 2 = Dominado
    val dueTimestamp: Long = 0,
    val lastReviewedTimestamp: Long = 0,
    val stability: Float = 0f,
    val difficulty: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Retorna true estritamente se o cartão for uma revisão pendente (já estudado e com vencimento atingido).
 * Cartões novos (reps == 0) não são considerados vencidos.
 */
fun FlashcardEntity.isDueForReview(now: Long = System.currentTimeMillis()): Boolean {
    return reps > 0 && dueTimestamp <= now
}

/**
 * Retorna true se o cartão for novo (nunca revisado pelo usuário).
 */
fun FlashcardEntity.isNew(): Boolean = reps == 0

