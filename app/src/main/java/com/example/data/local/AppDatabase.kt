package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FlashcardEntity

@Database(entities = [FlashcardEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao

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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anki_flashcards.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
