package com.example.bugs.repository

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.bugs.model.GameSettings

@Composable
fun rememberGameSettings(): MutableState<GameSettings> {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("game", Context.MODE_PRIVATE) }
    val state = remember {
        mutableStateOf(
            GameSettings(
                speed = prefs.getInt("speed", 5),
                maxBugAmount = prefs.getInt("maxBugAmount", 10),
                bonusInterval = prefs.getInt("bonusInterval", 10),
                roundDuration = prefs.getInt("roundDuration", 60)
            )
        )
    }
    LaunchedEffect(state.value) {
        val s = state.value
        prefs.edit()
            .putInt("speed", s.speed)
            .putInt("maxBugAmount", s.maxBugAmount)
            .putInt("bonusInterval", s.bonusInterval)
            .putInt("roundDuration", s.roundDuration)
            .apply()
    }
    return state
}