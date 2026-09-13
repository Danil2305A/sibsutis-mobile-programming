package com.example.bugs.util

import com.example.bugs.R
import java.util.Calendar

object ZodiacUtil {
    data class Zodiac(val name: String, val resId: Int)

    fun getZodiac(date: Calendar): Zodiac {
        val day = date.get(Calendar.DAY_OF_MONTH)
        val month = date.get(Calendar.MONTH) + 1

        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) ->
                Zodiac("Овен", R.drawable.aries)
            (month == 4 && day >= 20) || (month == 5 && day <= 20) ->
                Zodiac("Телец", R.drawable.taurus)
            (month == 5 && day >= 21) || (month == 6 && day <= 20) ->
                Zodiac("Близнецы", R.drawable.gemini)
            (month == 6 && day >= 21) || (month == 7 && day <= 22) ->
                Zodiac("Рак", R.drawable.cancer)
            (month == 7 && day >= 23) || (month == 8 && day <= 22) ->
                Zodiac("Лев", R.drawable.leo)
            (month == 8 && day >= 23) || (month == 9 && day <= 22) ->
                Zodiac("Дева", R.drawable.virgo)
            (month == 9 && day >= 23) || (month == 10 && day <= 22) ->
                Zodiac("Весы", R.drawable.libra)
            (month == 10 && day >= 23) || (month == 11 && day <= 21) ->
                Zodiac("Скорпион", R.drawable.scorpio)
            (month == 11 && day >= 22) || (month == 12 && day <= 21) ->
                Zodiac("Стрелец", R.drawable.sagittarius)
            (month == 12 && day >= 22) || (month == 1 && day <= 19) ->
                Zodiac("Козерог", R.drawable.capricorn)
            (month == 1 && day >= 20) || (month == 2 && day <= 18) ->
                Zodiac("Водолей", R.drawable.aquarius)
            else ->
                Zodiac("Рыбы", R.drawable.pisces)
        }
    }
}