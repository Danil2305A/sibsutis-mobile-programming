package com.example.bugs.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bugs.data.db.dao.PlayerDao
import com.example.bugs.data.db.dao.ScoreDao
import com.example.bugs.data.db.entity.PlayerEntity
import com.example.bugs.data.db.entity.ScoreEntity

@Database(
    entities = [PlayerEntity::class, ScoreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun scoreDao(): ScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bugs.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
        }
    }
}