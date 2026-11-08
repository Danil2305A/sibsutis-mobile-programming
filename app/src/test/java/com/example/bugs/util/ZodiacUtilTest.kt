package com.example.bugs.util

import com.example.bugs.R
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ZodiacUtilTest {

    private fun z(month: Int, day: Int) = ZodiacUtil.getZodiac(
        Calendar.getInstance().apply {
            set(Calendar.YEAR, 2000)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
        }
    )

    @Test fun capricornAquarius() {
        assertEquals("Козерог", z(1, 19).name); assertEquals("Водолей", z(1, 20).name)
    }
    @Test fun aquariusPisces() {
        assertEquals("Водолей", z(2, 18).name); assertEquals("Рыбы", z(2, 19).name)
    }
    @Test fun piscesAries() {
        assertEquals("Рыбы", z(3, 20).name); assertEquals("Овен", z(3, 21).name)
    }
    @Test fun ariesTaurus() {
        assertEquals("Овен", z(4, 19).name); assertEquals("Телец", z(4, 20).name)
    }
    @Test fun taurusGemini() {
        assertEquals("Телец", z(5, 20).name); assertEquals("Близнецы", z(5, 21).name)
    }
    @Test fun geminiCancer() {
        assertEquals("Близнецы", z(6, 20).name); assertEquals("Рак", z(6, 21).name)
    }
    @Test fun cancerLeo() {
        assertEquals("Рак", z(7, 22).name); assertEquals("Лев", z(7, 23).name)
    }
    @Test fun leoVirgo() {
        assertEquals("Лев", z(8, 22).name); assertEquals("Дева", z(8, 23).name)
    }
    @Test fun virgoLibra() {
        assertEquals("Дева", z(9, 22).name); assertEquals("Весы", z(9, 23).name)
    }
    @Test fun libraScorpio() {
        assertEquals("Весы", z(10, 22).name); assertEquals("Скорпион", z(10, 23).name)
    }
    @Test fun scorpioSagittarius() {
        assertEquals("Скорпион", z(11, 21).name); assertEquals("Стрелец", z(11, 22).name)
    }
    @Test fun sagittariusCapricorn() {
        assertEquals("Стрелец", z(12, 21).name); assertEquals("Козерог", z(12, 22).name)
    }

    @Test
    fun resIdComesFromMatchingDrawable() {
        assertEquals(R.drawable.aries, z(3, 25).resId)
        assertEquals(R.drawable.pisces, z(2, 25).resId)
    }
}