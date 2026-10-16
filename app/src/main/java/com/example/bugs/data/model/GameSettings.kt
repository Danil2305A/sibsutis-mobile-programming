package com.example.bugs.data.model

data class GameSettings(
    val speed: Int = 5,
    val maxBugAmount: Int = 10,
    val bonusInterval: Int = 10,
    val roundDuration: Int = 60,
)