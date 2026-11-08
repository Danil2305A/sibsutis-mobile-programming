package com.example.bugs.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BugTypeTest {
    @Test
    fun spawnWeightsOfNonGoldSumToOne() {
        val sum = BugType.entries.filter { it != BugType.GOLD }
            .sumOf { it.spawnWeight.toDouble() }
        assertEquals(1.0, sum, 1e-4)
    }

    @Test
    fun goldIsNotSpawnable() {
        assertEquals(0f, BugType.GOLD.spawnWeight, 1e-6f)
        assertEquals(0, BugType.GOLD.score)
    }

    @Test
    fun fixedScoreValues() {
        assertEquals(10, BugType.NORMAL.score)
        assertEquals(20, BugType.FAST.score)
        assertEquals(50, BugType.RARE.score)
    }
}