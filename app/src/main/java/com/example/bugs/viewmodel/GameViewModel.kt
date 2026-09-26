package com.example.bugs.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bugs.data.model.Bug
import com.example.bugs.data.model.GameSettings
import kotlin.random.Random

class GameViewModel : ViewModel() {
    private var config: GameSettings = GameSettings()

    val bugs = mutableStateListOf<Bug>()
    var score by mutableStateOf(0)
        private set
    var timeLeft by mutableStateOf(config.roundDuration)
        private set
    var isGameOver by mutableStateOf(false)
        private set

    private var nextId = 0L
    private var screenWidth = 0f
    private var screenHeight = 0f

    companion object {
        const val SCORE_AMNT_FOR_BUG = 10
        const val PENALTY_DEDUCTION = 5
    }

    fun applySettings(newConfig: GameSettings) {
        config = newConfig
    }

    fun setScreenSize(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
    }

    fun spawnBug() {
        if (isGameOver) return;
        if (bugs.size >= config.maxBugAmount) return
        val bug = Bug(
            id = nextId++,
            x = Random.nextFloat() * (screenWidth - Bug.DEFAULT_SIZE),
            y = Random.nextFloat() * (screenHeight - Bug.DEFAULT_SIZE),
            velocityX = (Random.nextFloat() - 0.5f) * 2f * Bug.BASE_SPEED * config.speed,
            velocityY = (Random.nextFloat() - 0.5f) * 2f * Bug.BASE_SPEED * config.speed
        )
        bugs.add(bug)
    }

    fun updatePositions(dt: Float) {
        if (isGameOver) return;
        bugs.forEach { bug ->
            bug.move(dt)
            bug.handleBoundsCollision(screenWidth, screenHeight)
        }
    }

    fun onTap(tapX: Float, tapY: Float) {
        val hit = bugs.find{ bug ->
            tapX in bug.x..(bug.x + bug.size) && tapY in bug.y..(bug.y + bug.size)
        }
        if (hit != null) {
            bugs.remove(hit)
            score += SCORE_AMNT_FOR_BUG
        } else {
            score = (score - PENALTY_DEDUCTION).coerceAtLeast(0);
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
        timeLeft = config.roundDuration
        isGameOver = false
        nextId = 0L
    }
}