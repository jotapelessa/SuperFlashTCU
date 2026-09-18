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
        Index(value = ["l1", "l2", "dueTimestamp"])
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
