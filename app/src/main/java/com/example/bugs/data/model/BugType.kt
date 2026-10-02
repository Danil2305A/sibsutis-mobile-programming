package com.example.bugs.data.model

import androidx.annotation.DrawableRes
import com.example.bugs.R
enum class BugType(
    @DrawableRes val imageRes: Int,
    val size: Float,
    val baseSpeed: Float,
    val score: Int,
    val spawnWeight: Float
) {
    NORMAL(
        imageRes = R.drawable.bug_normal,
        size = 120f,
        baseSpeed = 1f,
        score = 10,
        spawnWeight = 0.6f
    ),
    FAST(
        imageRes = R.drawable.bug_fast,
        size = 100f,
        baseSpeed = 2.2f,
        score = 20,
        spawnWeight = 0.3f
    ),
    RARE(
        imageRes = R.drawable.bug_rare,
        size = 110f,
        baseSpeed = 0.7f,
        score = 50,
        spawnWeight = 0.1f
    )
}