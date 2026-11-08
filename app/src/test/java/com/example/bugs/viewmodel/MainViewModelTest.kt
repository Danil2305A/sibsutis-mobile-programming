package com.example.bugs.viewmodel

import com.example.bugs.data.db.entity.PlayerEntity
import com.example.bugs.data.model.DifficultyPreset
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class MainViewModelTest {

    private val stub = PlayerEntity(
        fullName = "x", gender = "Мужской", course = 1,
        difficulty = DifficultyPreset.MEDIUM,
        birthDateMillis = 0L, zodiac = "Овен", zodiacResId = 0
    )

    @Test
    fun startsNull() = assertNull(MainViewModel().currentPlayer.value)

    @Test
    fun setStoresAndClears() {
        val vm = MainViewModel()
        vm.setCurrentPlayer(stub)
        assertSame(stub, vm.currentPlayer.value)
        vm.setCurrentPlayer(null)
        assertNull(vm.currentPlayer.value)
    }
}