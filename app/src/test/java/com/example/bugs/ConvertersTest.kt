package com.example.bugs

import com.example.bugs.data.db.Converters
import com.example.bugs.data.model.DifficultyPreset
import org.junit.Assert
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun fromPresetReturnsEnumNameForAllPresets() {
        DifficultyPreset.entries.forEach { preset ->
            Assert.assertEquals(preset.name, converters.fromPreset(preset))
        }
    }

    @Test
    fun fromPresetReturnsEasyName() {
        Assert.assertEquals("EASY", converters.fromPreset(DifficultyPreset.EASY))
    }

    @Test
    fun fromPresetReturnsCustomName() {
        Assert.assertEquals("CUSTOM", converters.fromPreset(DifficultyPreset.CUSTOM))
    }

    @Test
    fun toPresetParsesHardName() {
        Assert.assertEquals(DifficultyPreset.HARD, converters.toPreset("HARD"))
    }

    @Test
    fun toPresetFallsBackToMediumForUnknownName() {
        Assert.assertEquals(DifficultyPreset.MEDIUM, converters.toPreset("FOO"))
    }

    @Test
    fun toPresetFallsBackToMediumForEmptyString() {
        Assert.assertEquals(DifficultyPreset.MEDIUM, converters.toPreset(""))
    }

    @Test
    fun toPresetIsCaseSensitiveAndFallsBackToMedium() {
        Assert.assertEquals(DifficultyPreset.MEDIUM, converters.toPreset("easy"))
    }

    @Test
    fun roundTripPreservesAllPresets() {
        DifficultyPreset.entries.forEach { preset ->
            Assert.assertEquals(preset, converters.toPreset(converters.fromPreset(preset)))
        }
    }
}