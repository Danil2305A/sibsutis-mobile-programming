package com.example.bugs.data.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Bug(
    val id: Long,
    x: Float,
    y: Float,
    var velocityX: Float,
    var velocityY: Float,
    val size: Float = DEFAULT_SIZE
) {
    var x by mutableFloatStateOf(x)
    var y by mutableFloatStateOf(y)

    companion object {
        const val DEFAULT_SIZE = 230f
        const val BASE_SPEED = 1f
    }

    fun move(dt: Float) {
        x += velocityX * dt
        y += velocityY * dt
    }

    fun handleBoundsCollision(
        screenWidth: Float,
        screenHeight: Float
    ) {
        if (x <= 0f || x >= screenWidth - size) {
            velocityX = -velocityX
            x = x.coerceIn(0f, screenWidth - size)
        }
        if (y <= 0f || y >= screenHeight - size) {
            velocityY = -velocityY
            y = y.coerceIn(0f, screenHeight - size)
        }
    }
}
