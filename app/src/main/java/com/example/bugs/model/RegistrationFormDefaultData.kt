package com.example.bugs.model

import java.util.Calendar

data class RegistrationFormDefaultData (
    val fullName: String = "Иванов Иван Иванович",
    val gender: Gender = Gender.MALE,
    val course: Int = 1,
    val difficulty: Float = 1f,
    val birthDate: Calendar = Calendar.getInstance(),
)