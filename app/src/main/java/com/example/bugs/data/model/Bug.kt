package com.example.bugs.data.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlin.math.atan2

class Bug(
    val id: Long,
    x: Float,
    y: Float,
    var velocityX: Float,
    var velocityY: Float,
    val type: BugType
) {
    var x by mutableFloatStateOf(x)
    var y by mutableFloatStateOf(y)

    val size: Float get() = type.size
    val scoreValue: Int get() = type.score

    @get:DrawableRes
    val imageRes: Int get() = type.imageRes

    val angle: Float
        get() = Math.toDegrees(
            atan2(velocityY.toDouble(), velocityX.toDouble())
        ).toFloat() + 90f

    fun move(dt: Float) {
        x += velocityX * dt
        y += velocityY * dt
    }

    fun handleBoundsCollision(
        fieldWidth: Float,
        fieldHeight: Float
    ) {
        if (x <= 0f || x >= fieldWidth - size) {
            velocityX = -velocityX
            x = x.coerceIn(0f, (fieldWidth - size).coerceAtLeast(0f))
        }
        if (y <= 0f || y >= fieldHeight - size) {
            velocityY = -velocityY
            y = y.coerceIn(0f, (fieldHeight - size).coerceAtLeast(0f))
        }
    }

    companion object {
        const val FIELD_HEIGHT = 1000f
        const val BASE_SPEED = 1f
    }
}