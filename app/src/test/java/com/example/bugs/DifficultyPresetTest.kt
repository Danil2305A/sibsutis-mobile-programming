package com.example.bugs.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyPresetTest {

    @Test
    fun toSettingsReflectsEnumFieldsForSelectablePresets() {
        DifficultyPreset.selectable.forEach { preset ->
            val settings = preset.toSettings()

            assertEquals(preset.speed, settings.speed)
            assertEquals(preset.maxBugAmount, settings.maxBugAmount)
            assertEquals(preset.bonusInterval, settings.bonusInterval)
            assertEquals(preset.roundDuration, settings.roundDuration)
        }
    }

    @Test
    fun fromIsInverseOfToSettingsForSelectablePresets() {
        DifficultyPreset.selectable.forEach { preset ->
            assertEquals(preset, DifficultyPreset.from(preset.toSettings()))
        }
    }

    @Test
    fun fromReturnsCustomForSettingsWithUnusedSpeed() {
        val unused = unusedAmong(DifficultyPreset.selectable.map { it.speed })

        assertEquals(
            DifficultyPreset.CUSTOM,
            DifficultyPreset.from(GameSettings(speed = unused))
        )
    }

    @Test
    fun fromReturnsCustomForSettingsWithUnusedMaxBugAmount() {
        val unused = unusedAmong(DifficultyPreset.selectable.map { it.maxBugAmount })

        assertEquals(
            DifficultyPreset.CUSTOM,
            DifficultyPreset.from(GameSettings(maxBugAmount = unused))
        )
    }

    @Test
    fun fromReturnsCustomForSettingsWithUnusedBonusInterval() {
        val unused = unusedAmong(DifficultyPreset.selectable.map { it.bonusInterval })

        assertEquals(
            DifficultyPreset.CUSTOM,
            DifficultyPreset.from(GameSettings(bonusInterval = unused))
        )
    }

    @Test
    fun fromReturnsCustomForSettingsWithUnusedRoundDuration() {
        val unused = unusedAmong(DifficultyPreset.selectable.map { it.roundDuration })

        assertEquals(
            DifficultyPreset.CUSTOM,
            DifficultyPreset.from(GameSettings(roundDuration = unused))
        )
    }

    @Test
    fun defaultGameSettingsMatchesMedium() {
        assertEquals(DifficultyPreset.MEDIUM, DifficultyPreset.from(GameSettings()))
    }

    @Test
    fun selectableContainsOnlyEasyMediumHard() {
        assertEquals(
            listOf(DifficultyPreset.EASY, DifficultyPreset.MEDIUM, DifficultyPreset.HARD),
            DifficultyPreset.selectable
        )
        assertFalse(DifficultyPreset.selectable.contains(DifficultyPreset.CUSTOM))
    }

    @Test
    fun allContainsEveryPresetInDeclarationOrder() {
        assertEquals(DifficultyPreset.entries.toList(), DifficultyPreset.all)
    }

    @Test
    fun allTitlesAreNotBlank() {
        DifficultyPreset.entries.forEach { preset ->
            assertTrue(preset.title.isNotBlank())
        }
    }

    private fun unusedAmong(used: Collection<Int>): Int {
        val taken = used.toSet()
        return generateSequence(1) { it + 1 }.first { it !in taken }
    }
}