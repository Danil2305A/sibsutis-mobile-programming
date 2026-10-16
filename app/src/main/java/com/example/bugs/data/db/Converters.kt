package com.example.bugs.data.db;

import androidx.room.TypeConverter;

import com.example.bugs.data.model.DifficultyPreset;

class Converters {
    @TypeConverter
    fun fromPreset(p: DifficultyPreset): String = p.name

    @TypeConverter
    fun toPreset(s: String): DifficultyPreset =
        runCatching { DifficultyPreset.valueOf(s) }
            .getOrDefault(DifficultyPreset.MEDIUM)
}