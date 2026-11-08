package com.example.bugs.viewmodel

import com.example.bugs.data.model.BugType
import com.example.bugs.data.model.GameSettings
import com.example.bugs.data.repository.GoldRateProvider
import com.example.bugs.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GameViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private lateinit var goldRate: GoldRateProvider
    private lateinit var vm: GameViewModel

    @Before
    fun setUp() {
        goldRate = mockk(relaxed = true)
        coEvery { goldRate.getTodayGoldRate(any()) } returns null
        every { goldRate.getLastKnownRate() } returns null
        vm = GameViewModel(goldRate)
    }

    @Test
    fun initialStateIsFresh() {
        assertEquals(0, vm.score)
        assertEquals(0, vm.hits)
        assertEquals(0, vm.misses)
        assertTrue(vm.bugs.isEmpty())
        assertFalse(vm.isGameOver)
        assertFalse(vm.scoreSaved)
        assertEquals(0f, vm.accuracy)
    }

    @Test
    fun applySettingsWithNewDurationResets() {
        vm.applySettings(GameSettings(roundDuration = 30))
        assertEquals(30, vm.timeLeft)

        vm.applySettings(GameSettings(roundDuration = 45))
        assertEquals(45, vm.timeLeft)
        assertEquals(0, vm.score)
    }

    @Test
    fun applySettingsWithSameDurationKeepsScore() {
        vm.applySettings(GameSettings(roundDuration = 60))
        vm.spawnBug()
        vm.onTap(vm.bugs.first().x + 1f, vm.bugs.first().y + 1f)
        assertTrue(vm.score > 0)

        vm.applySettings(GameSettings(roundDuration = 60))
        assertTrue(vm.score > 0)
    }

    @Test
    fun spawnRespectsMaxBugAmount() {
        vm.applySettings(GameSettings(maxBugAmount = 3))
        repeat(10) { vm.spawnBug() }
        assertEquals(3, vm.bugs.size)
    }

    @Test
    fun spawnIsNoOpWhenGameOver() {
        vm.applySettings(GameSettings(roundDuration = 1))
        vm.tick()
        assertTrue(vm.isGameOver)
        vm.spawnBug()
        assertTrue(vm.bugs.isEmpty())
    }

    @Test
    fun spawnGoldBugGuardsAgainstDuplicate() {
        vm.spawnGoldBug()
        vm.spawnGoldBug()
        assertEquals(1, vm.bugs.count { it.type == BugType.GOLD })
    }

    @Test
    fun tapOnNormalBugAddsScoreAndHit() {
        vm.spawnBug()
        val b = vm.bugs.first()
        val expected = b.scoreValue

        vm.onTap(b.x + 1f, b.y + 1f)

        assertEquals(expected, vm.score)
        assertEquals(1, vm.hits)
        assertTrue(vm.bugs.isEmpty())
    }

    @Test
    fun tapOnEmptySpaceCountsMissAndClampsAtZero() {
        vm.spawnBug()
        val b = vm.bugs.first()
        vm.onTap(b.x - 500f, b.y - 500f)

        assertEquals(1, vm.misses)
        assertEquals(0, vm.score)
    }

    @Test
    fun tapsDuringGameOverAreIgnored() {
        vm.applySettings(GameSettings(roundDuration = 1))
        vm.tick()
        vm.onTap(0f, 0f)
        assertEquals(0, vm.hits)
        assertEquals(0, vm.misses)
    }

    @Test
    fun tickSpawnsBonusAfterInterval() {
        vm.applySettings(GameSettings(bonusInterval = 1))
        vm.tick()
        assertTrue(vm.bonusVisible)
    }

    @Test
    fun tappingBonusActivatesTiltWithoutHit() {
        vm.applySettings(GameSettings(bonusInterval = 1))
        vm.tick()
        vm.onTap(vm.bonusX + 10f, vm.bonusY + 10f)

        assertTrue(vm.tiltModeActive)
        assertFalse(vm.bonusVisible)
        assertEquals(0, vm.hits)
        assertEquals(0, vm.misses)
    }

    @Test
    fun bonusDisappearsAfterLifetime() {
        val interval = GameViewModel.BONUS_LIFETIME_SECONDS + 1

        vm.applySettings(GameSettings(bonusInterval = interval))
        repeat(interval) { vm.tick() }
        assertTrue(vm.bonusVisible)

        repeat(GameViewModel.BONUS_LIFETIME_SECONDS) { vm.tick() }
        assertFalse(vm.bonusVisible)
    }

    @Test
    fun tiltModeEndsAfterDuration() {
        vm.applySettings(GameSettings(bonusInterval = 1))
        vm.tick()
        vm.onTap(vm.bonusX + 10f, vm.bonusY + 10f)
        repeat(GameViewModel.TILT_MODE_DURATION_SECONDS) { vm.tick() }
        assertFalse(vm.tiltModeActive)
    }

    @Test
    fun goldBugUsesLastKnownRate() {
        every { goldRate.getLastKnownRate() } returns 12_345.0
        vm.spawnGoldBug()
        val g = vm.bugs.first { it.type == BugType.GOLD }
        vm.onTap(g.x + 1f, g.y + 1f)
        assertEquals(123, vm.score)
    }

    @Test
    fun goldBugFallsBackToDefaultWhenRateTiny() {
        every { goldRate.getLastKnownRate() } returns 50.0
        vm.spawnGoldBug()
        val g = vm.bugs.first()
        vm.onTap(g.x + 1f, g.y + 1f)
        assertEquals(10, vm.score)
    }

    @Test
    fun goldBugFallsBackToDefaultWhenRateUnknown() {
        every { goldRate.getLastKnownRate() } returns null
        vm.spawnGoldBug()
        val g = vm.bugs.first()
        vm.onTap(g.x + 1f, g.y + 1f)
        assertEquals(10, vm.score)
    }

    @Test
    fun tickEndsGame() {
        vm.applySettings(GameSettings(roundDuration = 3))
        vm.tick(); assertEquals(2, vm.timeLeft)
        vm.tick(); assertEquals(1, vm.timeLeft)
        vm.tick(); assertEquals(0, vm.timeLeft)
        assertTrue(vm.isGameOver)
        assertFalse(vm.tiltModeActive)
    }

    @Test
    fun ticksAfterGameOverAreNoOp() {
        vm.applySettings(GameSettings(roundDuration = 1))
        vm.tick()
        val score = vm.score
        val t = vm.timeLeft
        vm.tick()
        assertEquals(score, vm.score)
        assertEquals(t, vm.timeLeft)
    }

    @Test
    fun resetClearsAllTransientState() {
        vm.applySettings(GameSettings(roundDuration = 1))
        vm.spawnBug()
        vm.onTap(vm.bugs.first().x + 1f, vm.bugs.first().y + 1f)

        vm.resetGame()

        assertEquals(0, vm.score)
        assertEquals(0, vm.hits)
        assertEquals(0, vm.misses)
        assertEquals(1, vm.timeLeft)
        assertFalse(vm.isGameOver)
        assertFalse(vm.scoreSaved)
        assertTrue(vm.bugs.isEmpty())
        assertFalse(vm.bonusVisible)
        assertFalse(vm.tiltModeActive)
    }

    @Test
    fun onPlayerChangedResets() {
        vm.applySettings(GameSettings(roundDuration = 1))
        vm.spawnBug()
        vm.onTap(vm.bugs.first().x + 1f, vm.bugs.first().y + 1f)
        assertTrue(vm.score > 0)

        vm.onPlayerChanged(42L)
        assertEquals(0, vm.score)
    }

    @Test
    fun onPlayerChangedSameIdDoesNotReset() {
        vm.onPlayerChanged(1L)
        vm.spawnBug()
        vm.onTap(vm.bugs.first().x + 1f, vm.bugs.first().y + 1f)
        val saved = vm.score

        vm.onPlayerChanged(1L)
        assertEquals(saved, vm.score)
    }

    @Test
    fun markScoreSavedTogglesFlag() {
        assertFalse(vm.scoreSaved)
        vm.markScoreSaved()
        assertTrue(vm.scoreSaved)
        vm.resetGame()
        assertFalse(vm.scoreSaved)
    }

    @Test
    fun setFieldSizeClampsExistingBugs() {
        vm.spawnGoldBug()
        val b = vm.bugs.first()
        b.x = 500f; b.y = 500f

        vm.setFieldSize(width = 100f, height = 100f)

        assertEquals(0f, b.x, 1e-4f)
        assertEquals(0f, b.y, 1e-4f)
        assertEquals(100f, vm.fieldWidth, 1e-4f)
    }
}