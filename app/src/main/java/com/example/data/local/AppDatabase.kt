package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FlashcardEntity
import com.example.data.model.ReviewLogEntity

@Database(entities = [FlashcardEntity::class, ReviewLogEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun reviewLogDao(): ReviewLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_flashcards_l1_dueTimestamp ON flashcards(l1, dueTimestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_flashcards_l1_masteryLevel ON flashcards(l1, masteryLevel)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_flashcards_l1_l2_dueTimestamp ON flashcards(l1, l2, dueTimestamp)")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE flashcards ADD COLUMN stability REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE flashcards ADD COLUMN difficulty REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_flashcards_l1_l2_l3_dueTimestamp ON flashcards(l1, l2, l3, dueTimestamp)")
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS review_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cardId INTEGER NOT NULL,
                        l1 TEXT NOT NULL,
                        l2 TEXT NOT NULL,
                        l3 TEXT NOT NULL,
                        rating INTEGER NOT NULL,
                        reviewDurationMs INTEGER NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_logs_timestamp ON review_logs(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_logs_cardId ON review_logs(cardId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_logs_l1_timestamp ON review_logs(l1, timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_review_logs_l1_l2_timestamp ON review_logs(l1, l2, timestamp)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anki_flashcards.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration(false)
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
