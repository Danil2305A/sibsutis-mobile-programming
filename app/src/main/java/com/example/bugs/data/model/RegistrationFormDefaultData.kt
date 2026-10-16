package com.example.bugs.data.model

import java.util.Calendar

data class RegistrationFormDefaultData (
    val fullName: String = "Иванов Иван Иванович",
    val gender: String = Gender.MALE.value,
    val course: Int = 1,
    val difficulty: DifficultyPreset = DifficultyPreset.MEDIUM,
    val birthDate: Calendar = Calendar.getInstance(),
)