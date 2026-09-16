package com.example.creatorlog

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AccountEntity::class,
        ClientEntity::class,
        ProjectEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CreatorLogDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun clientDao(): ClientDao
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: CreatorLogDatabase? = null

        fun getDatabase(context: Context): CreatorLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CreatorLogDatabase::class.java,
                    "creatorlog_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}