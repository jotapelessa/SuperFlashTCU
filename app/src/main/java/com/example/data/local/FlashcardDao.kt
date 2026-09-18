package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FlashcardEntity
import kotlinx.coroutines.flow.Flow

data class HierarchyFolderTuple(
    val l1: String,
    val l2: String,
    val l3: String
)

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY l1 ASC, l2 ASC, l3 ASC, id ASC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE l1 = :l1 ORDER BY l2 ASC, l3 ASC, id ASC")
    fun getFlashcardsByL1(l1: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE l1 = :l1 AND l2 = :l2 ORDER BY l3 ASC, id ASC")
    fun getFlashcardsByL1AndL2(l1: String, l2: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE l1 = :l1 AND l2 = :l2 AND l3 = :l3 ORDER BY id ASC")
    fun getFlashcardsByL1L2L3(l1: String, l2: String, l3: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE l1 = :l1 AND dueTimestamp <= :now ORDER BY dueTimestamp ASC")
    fun getDueFlashcardsByL1(l1: String, now: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT DISTINCT l1 FROM flashcards WHERE l1 != '' ORDER BY l1 ASC")
    fun getDistinctL1Decks(): Flow<List<String>>

    @Query("SELECT DISTINCT l2 FROM flashcards WHERE l1 = :l1 AND l2 != '' ORDER BY l2 ASC")
    fun getDistinctL2Disciplines(l1: String): Flow<List<String>>

    @Query("SELECT DISTINCT l3 FROM flashcards WHERE l1 = :l1 AND l2 = :l2 AND l3 != '' ORDER BY l3 ASC")
    fun getDistinctL3Topics(l1: String, l2: String): Flow<List<String>>

    @Query("SELECT * FROM flashcards WHERE id = :id LIMIT 1")
    suspend fun getFlashcardById(id: Long): FlashcardEntity?

    @Query("SELECT * FROM flashcards WHERE contentHash = :hash LIMIT 1")
    suspend fun getByContentHash(hash: String): FlashcardEntity?

    @Query("SELECT contentHash FROM flashcards")
    suspend fun getAllContentHashes(): List<String>

    @Query("SELECT DISTINCT l1, l2, l3 FROM flashcards WHERE l1 != ''")
    suspend fun getAllDistinctFolders(): List<HierarchyFolderTuple>

    @Query("SELECT DISTINCT l1 FROM flashcards WHERE l1 != ''")
    suspend fun getAllDistinctL1(): List<String>

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun countTotal(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFlashcard(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cards: List<FlashcardEntity>): List<Long>

    @Update
    suspend fun updateFlashcard(card: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(card: FlashcardEntity)

    @Query("UPDATE flashcards SET l2 = :newL2, deckRaw = l1 || ' :: ' || :newL2 || ' :: ' || l3 WHERE l1 = :l1 AND l2 = :oldL2 AND l3 = :l3")
    suspend fun moveL3TopicToNewL2(l1: String, oldL2: String, l3: String, newL2: String)

    @Query("UPDATE flashcards SET l1 = :newL1, deckRaw = :newL1 || ' :: ' || l2 || ' :: ' || l3 WHERE l1 = :oldL1 AND l2 = :l2")
    suspend fun moveL2ToL1(oldL1: String, l2: String, newL1: String)

    @Query("SELECT * FROM flashcards WHERE l1 = :l1 AND l2 = :l2 ORDER BY l3 ASC, id ASC")
    suspend fun getCardsByL1AndL2Sync(l1: String, l2: String): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE l1 = :l1 ORDER BY l2 ASC, l3 ASC, id ASC")
    suspend fun getCardsByL1Sync(l1: String): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards ORDER BY l1 ASC, l2 ASC, l3 ASC, id ASC")
    suspend fun getAllFlashcardsSync(): List<FlashcardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(cards: List<FlashcardEntity>): List<Long>

    @Query("UPDATE flashcards SET l1 = :newL1, deckRaw = :newL1 || ' :: ' || l2 || ' :: ' || l3 WHERE l1 = :oldL1")
    suspend fun renameL1Deck(oldL1: String, newL1: String)

    @Query("UPDATE flashcards SET l2 = :newL2, deckRaw = l1 || ' :: ' || :newL2 || ' :: ' || l3 WHERE l1 = :l1 AND l2 = :oldL2")
    suspend fun renameL2Discipline(l1: String, oldL2: String, newL2: String)

    @Query("DELETE FROM flashcards WHERE l1 = :l1 AND l2 = :l2")
    suspend fun deleteL2Discipline(l1: String, l2: String)

    @Query("DELETE FROM flashcards WHERE l1 = :l1")
    suspend fun deleteDeckL1(l1: String)

    @Query("UPDATE flashcards SET reps = 0, lapses = 0, masteryLevel = 0, intervalDays = 0, easeFactor = 2.5, dueTimestamp = 0, lastReviewedTimestamp = 0, stability = 0.0, difficulty = 0.0")
    suspend fun resetAllCardStats()

    @Query("DELETE FROM flashcards")
    suspend fun clearAll()
}
