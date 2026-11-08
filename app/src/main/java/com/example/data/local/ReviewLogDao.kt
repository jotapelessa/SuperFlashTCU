package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ReviewLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLog(log: ReviewLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ReviewLogEntity>): List<Long>

    @Query("SELECT * FROM review_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getReviewLogsSince(sinceTimestamp: Long): Flow<List<ReviewLogEntity>>

    @Query("SELECT * FROM review_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    suspend fun getReviewLogsSinceSync(sinceTimestamp: Long): List<ReviewLogEntity>

    @Query("SELECT * FROM review_logs WHERE l1 = :l1 AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getReviewLogsForL1Since(l1: String, sinceTimestamp: Long): Flow<List<ReviewLogEntity>>

    @Query("SELECT * FROM review_logs ORDER BY timestamp ASC")
    fun getAllReviewLogs(): Flow<List<ReviewLogEntity>>

    @Query("SELECT * FROM review_logs ORDER BY timestamp ASC")
    suspend fun getAllReviewLogsSync(): List<ReviewLogEntity>

    @Query("SELECT COUNT(*) FROM review_logs WHERE timestamp >= :dayStart")
    fun countReviewsToday(dayStart: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_logs")
    suspend fun countTotalReviews(): Int

    @Query("DELETE FROM review_logs")
    suspend fun clearAll()

    @Query("DELETE FROM review_logs WHERE l1 = :l1")
    suspend fun deleteLogsByL1(l1: String)

    @Query("DELETE FROM review_logs WHERE l1 = :l1 AND l2 = :l2")
    suspend fun deleteLogsByL1AndL2(l1: String, l2: String)

    @Query("UPDATE review_logs SET l1 = :newL1 WHERE l1 = :oldL1")
    suspend fun renameL1Logs(oldL1: String, newL1: String)

    @Query("UPDATE review_logs SET l2 = :newL2 WHERE l1 = :l1 AND l2 = :oldL2")
    suspend fun renameL2Logs(l1: String, oldL2: String, newL2: String)
}
