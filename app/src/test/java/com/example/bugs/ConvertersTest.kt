package com.example.bugs

import com.example.bugs.data.db.Converters
import com.example.bugs.data.model.DifficultyPreset
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun fromPresetReturnsEnumNameForAllPresets() {
        DifficultyPreset.entries.forEach { preset ->
            assertEquals(preset.name, converters.fromPreset(preset))
        }
    }

    @Test
    fun fromPresetReturnsEasyName() {
        assertEquals("EASY", converters.fromPreset(DifficultyPreset.EASY))
    }

    @Test
    fun fromPresetReturnsCustomName() {
        assertEquals("CUSTOM", converters.fromPreset(DifficultyPreset.CUSTOM))
    }

    @Test
    fun toPresetParsesHardName() {
        assertEquals(DifficultyPreset.HARD, converters.toPreset("HARD"))
    }

    @Test
    fun toPresetFallsBackToMediumForUnknownName() {
        assertEquals(DifficultyPreset.MEDIUM, converters.toPreset("FOO"))
    }

    @Test
    fun toPresetFallsBackToMediumForEmptyString() {
        assertEquals(DifficultyPreset.MEDIUM, converters.toPreset(""))
    }

    @Test
    fun toPresetIsCaseSensitiveAndFallsBackToMedium() {
        assertEquals(DifficultyPreset.MEDIUM, converters.toPreset("easy"))
    }

    @Test
    fun roundTripPreservesAllPresets() {
        DifficultyPreset.entries.forEach { preset ->
            assertEquals(preset, converters.toPreset(converters.fromPreset(preset)))
        }
    }
}