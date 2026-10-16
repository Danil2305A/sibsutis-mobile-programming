package com.example.bugs.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bugs.data.model.Bug
import com.example.bugs.data.model.BugType
import com.example.bugs.data.model.GameSettings
import kotlin.random.Random

class GameViewModel : ViewModel() {
    private var config: GameSettings = GameSettings()

    val bugs = mutableStateListOf<Bug>()

    var fieldWidth by mutableFloatStateOf(Bug.FIELD_HEIGHT)
        private set
    var fieldHeight by mutableFloatStateOf(Bug.FIELD_HEIGHT)
        private set

    var score by mutableIntStateOf(0)
        private set
    var timeLeft by mutableIntStateOf(config.roundDuration)
        private set
    var isGameOver by mutableStateOf(false)
        private set

    private var nextId = 0L

    var hits by mutableIntStateOf(0)
        private set
    var misses by mutableIntStateOf(0)
        private set

    var scoreSaved by mutableStateOf(false)
        private set

    private var lastPlayerId: Long? = null

    val accuracy: Float
        get() = if (hits + misses == 0) 0f else hits * 100f / (hits + misses)

    var bonusVisible by mutableStateOf(false)
        private set

    var bonusX by mutableFloatStateOf(0f)
        private set

    var bonusY by mutableFloatStateOf(0f)
        private set

    val bonusSize = 120f

    var tiltModeActive by mutableStateOf(false)
        private set

    private var bonusTimer = BONUS_INTERVAL_SECONDS
    private var bonusLifeLeft = 0
    private var tiltModeLeft = 0

    private var tiltX = 0f
    private var tiltY = 0f

    fun applySettings(newConfig: GameSettings) {
        val durationChanged = newConfig.roundDuration != config.roundDuration
        config = newConfig
        if (durationChanged) {
            resetGame()
        }
    }

    fun markScoreSaved() {
        scoreSaved = true
    }

    fun setFieldSize(width: Float, height: Float) {
        fieldWidth = width
        fieldHeight = height

        bugs.forEach { bug ->
            bug.x = bug.x.coerceIn(0f, (fieldWidth - bug.size).coerceAtLeast(0f))
            bug.y = bug.y.coerceIn(0f, (fieldHeight - bug.size).coerceAtLeast(0f))
        }
    }

    private fun getRandomBugType(): BugType {
        val r = Random.nextFloat()
        var acc = 0f

        BugType.entries.forEach { type ->
            acc += type.spawnWeight
            if (r <= acc) return type
        }

        return BugType.NORMAL
    }

    fun onTilt(x: Float, y: Float) {
        tiltX = x
        tiltY = y
    }

    private fun spawnBonus() {
        bonusX = Random.nextFloat() * (fieldWidth - bonusSize)
        bonusY = Random.nextFloat() * (fieldHeight - bonusSize)
        bonusVisible = true
        bonusLifeLeft = BONUS_LIFETIME_SECONDS
    }

    fun spawnBug() {
        if (isGameOver) return
        if (bugs.size >= config.maxBugAmount) return

        val type = getRandomBugType()

        val bug = Bug(
            id = nextId++,
            x = Random.nextFloat() * (fieldWidth - type.size),
            y = Random.nextFloat() * (fieldHeight - type.size),
            velocityX = (Random.nextFloat() - 0.5f) * 2f *
                    Bug.BASE_SPEED * type.baseSpeed * config.speed,
            velocityY = (Random.nextFloat() - 0.5f) * 2f *
                    Bug.BASE_SPEED * type.baseSpeed * config.speed,
            type = type
        )

        bugs.add(bug)
    }

    fun updatePositions(dt: Float) {
        if (isGameOver) return;
        bugs.forEach { bug ->
            if (tiltModeActive) {
                bug.x += tiltX * TILT_FORCE * dt
                bug.y += tiltY * TILT_FORCE * dt
            }

            bug.move(dt)
            bug.handleBoundsCollision(fieldWidth, fieldHeight)
        }
    }

    fun onTap(tapX: Float, tapY: Float) {
        if (isGameOver) return

//        логика бонуса
        if (bonusVisible
            && tapX in bonusX..(bonusX + bonusSize)
            && tapY in bonusY..(bonusY + bonusSize)) {
            bonusVisible = false
            tiltModeActive = true
            tiltModeLeft = TILT_MODE_DURATION_SECONDS
            return
        }

//        логика жука
        val hit = bugs.find { bug ->
            tapX in bug.x..(bug.x + bug.size) &&
                    tapY in bug.y..(bug.y + bug.size)
        }

        if (hit != null) {
            bugs.remove(hit)
            score += hit.scoreValue
            hits++
        } else {
            score = (score - PENALTY_DEDUCTION).coerceAtLeast(0);
            misses++
        }
    }

    fun tick() {
        if (isGameOver) return;

        timeLeft -= 1
        if (timeLeft <= 0) {
            isGameOver = true;
            return
        }

        if (bonusVisible) {
            bonusLifeLeft--
            if (bonusLifeLeft <= 0) bonusVisible = false
        }

        if (tiltModeActive) {
            tiltModeLeft--
            if (tiltModeLeft <= 0) tiltModeActive = false
        }

        bonusTimer--
        if (bonusTimer <= 0) {
            if (!bonusVisible && !tiltModeActive) {
                spawnBonus()
            }
            bonusTimer = BONUS_INTERVAL_SECONDS
        }
    }

    fun onPlayerChanged(playerId: Long?) {
        if (lastPlayerId != playerId) {
            lastPlayerId = playerId
            resetGame()
        }
    }

    fun resetGame() {
        bugs.clear()
        score = 0
        hits = 0
        misses = 0
        timeLeft = config.roundDuration
        isGameOver = false
        nextId = 0L
        scoreSaved = false

        bonusVisible = false
        tiltModeActive = false
        bonusTimer = BONUS_INTERVAL_SECONDS
        bonusLifeLeft = 0
        tiltModeLeft = 0
        tiltX = 0f
        tiltY = 0f
    }

    companion object {
        const val PENALTY_DEDUCTION = 5

        const val BONUS_INTERVAL_SECONDS = 15
        const val BONUS_LIFETIME_SECONDS = 6
        const val TILT_MODE_DURATION_SECONDS = 10
        const val TILT_FORCE = 3f
    }
}