package com.example.bugs.data.repository

import android.content.Context
import com.example.bugs.data.db.AppDatabase

object RepositoryProvider {
    @Volatile
    private var repository: PlayerRepository? = null

    fun get(context: Context): PlayerRepository =
        repository ?: synchronized(this) {
            repository ?: PlayerRepository(
                AppDatabase.get(context).playerDao(),
                AppDatabase.get(context).scoreDao()
            ).also { repository = it }
        }
}