package com.example.bugs.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bugs.repository.rememberGameSettings
import kotlin.math.roundToInt

@Composable
fun SettingsScreen() {
    val settings = rememberGameSettings()

    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SliderRow(
            label = "Скорость игры",
            value = settings.value.speed,
            range = 1f..10f,
            onChange = { settings.value = settings.value.copy(speed = it) }
        )
        SliderRow(
            label = "Макс. тараканов на экране",
            value = settings.value.maxBugAmount,
            range = 1f..20f,
            onChange = { settings.value = settings.value.copy(maxBugAmount = it) }
        )
        SliderRow(
            label = "Интервал бонусов, сек",
            value = settings.value.bonusInterval,
            range = 1f..30f,
            onChange = { settings.value = settings.value.copy(bonusInterval = it) }
        )
        SliderRow(
            label = "Длительность раунда, сек",
            value = settings.value.roundDuration,
            range = 30f..180f,
            onChange = { settings.value = settings.value.copy(roundDuration = it) }
        )
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Int) -> Unit
) {
    Column() {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text(value.toString(), fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1
        )
    }
}