package com.example.bugs.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.bugs.data.repository.RepositoryProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LeaderboardScreen() {
    val context = LocalContext.current
    val repository = remember { RepositoryProvider.get(context) }

    val records by repository.observeRecords()
        .collectAsState(initial = emptyList())


    val dateFormat = remember {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(records) { record ->
            Column(Modifier.padding(vertical = 8.dp)) {
                Text("Игрок: ${record.fullName} (#${record.playerId})")
                Text("Очки: ${record.score}")
                Text("Сложность: ${record.difficulty}")
                Text("Скорость: ${record.settingsSpeed}")
                Text("Попадания/промахи: ${record.hits}/${record.misses}")
                Text("Дата: ${dateFormat.format(Date(record.playedAt))}")

            }
            HorizontalDivider()
        }
    }
}