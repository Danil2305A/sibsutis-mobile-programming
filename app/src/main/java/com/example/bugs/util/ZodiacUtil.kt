package com.example.bugs.util

import java.util.Calendar

object ZodiacUtil {
    data class Zodiac(val name: String, val resId: Int)

    fun getZodiac(date: Calendar): Zodiac {
        return Zodiac("", -1)
    }
}