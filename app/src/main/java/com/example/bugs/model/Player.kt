package com.example.bugs.model

import java.util.Calendar

data class Player(
    val fullName: String,
    val gender: String,
    val course: Int,
    val difficulty: Int,
    val birthDate: Calendar,
    val zodiac: String,
    val zodiacResId: Int
)
