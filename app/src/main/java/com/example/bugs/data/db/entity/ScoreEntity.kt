package com.example.bugs.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.bugs.data.model.DifficultyPreset

@Entity(
    tableName = "scores",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playerId"), Index("difficulty")]
)
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val playerId: Long,
    val score: Int,
    val hits: Int,
    val misses: Int,
    val difficulty: DifficultyPreset,
    val settingsSpeed: Int,
    val playedAt: Long = System.currentTimeMillis()
)
