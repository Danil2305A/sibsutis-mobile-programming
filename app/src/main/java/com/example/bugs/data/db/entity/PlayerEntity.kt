package com.example.bugs.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bugs.data.model.DifficultyPreset

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fullName: String,
    val gender: String,
    val course: Int,
    val difficulty: DifficultyPreset = DifficultyPreset.MEDIUM,
    val birthDateMillis: Long,
    val zodiac: String,
    val zodiacResId: Int,
    val createdAt: Long = System.currentTimeMillis()
)
