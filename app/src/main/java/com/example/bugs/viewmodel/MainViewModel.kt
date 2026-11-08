package com.example.bugs.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bugs.data.db.entity.PlayerEntity

class MainViewModel: ViewModel() {
    val currentPlayer = mutableStateOf<PlayerEntity?>(null)

    fun setCurrentPlayer(player: PlayerEntity?) {
        currentPlayer.value = player
    }
}