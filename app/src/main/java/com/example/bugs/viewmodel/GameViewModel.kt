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

    val accuracy: Float
        get() = if (hits + misses == 0) 0f else hits * 100f / (hits + misses)

    fun applySettings(newConfig: GameSettings) {
        val durationChanged = newConfig.roundDuration != config.roundDuration
        config = newConfig
        if (durationChanged) {
            resetGame()
        }
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
            bug.move(dt)
            bug.handleBoundsCollision(fieldWidth, fieldHeight)
        }
    }

    fun onTap(tapX: Float, tapY: Float) {
        if (isGameOver) return

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
        if (timeLeft <= 0) isGameOver = true;
    }

    fun resetGame() {
        bugs.clear()
        score = 0
        hits = 0
        misses = 0
        timeLeft = config.roundDuration
        isGameOver = false
        nextId = 0L
    }

    companion object {
        const val PENALTY_DEDUCTION = 5
    }
}