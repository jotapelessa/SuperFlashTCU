package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["cardId"]),
        Index(value = ["l1", "timestamp"]),
        Index(value = ["l1", "l2", "timestamp"])
    ]
)
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val l1: String,
    val l2: String,
    val l3: String,
    val rating: Int, // 1 = Again, 2 = Hard, 3 = Good, 4 = Easy
    val reviewDurationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
