package com.example.bugs.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bugs.data.db.entity.PlayerEntity
import com.example.bugs.data.model.Gender
import com.example.bugs.data.model.Player
import com.example.bugs.data.model.RegistrationFormDefaultData
import com.example.bugs.data.repository.RepositoryProvider
import com.example.bugs.util.ZodiacUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import java.util.Date
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationFormScreen(
    currentPlayer: MutableState<PlayerEntity?>
) {
    val context = LocalContext.current
    val repository = remember { RepositoryProvider.get(context) }
    val scope = rememberCoroutineScope()

    val players by repository.observePlayers()
        .collectAsState(initial = emptyList())

    var selectedPlayerId by rememberSaveable { mutableStateOf<Long?>(null) }
    var playerExpanded by rememberSaveable { mutableStateOf(false) }

    val registrationFormDefaultData = RegistrationFormDefaultData()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        var fullName by rememberSaveable() { mutableStateOf(registrationFormDefaultData.fullName) }
        var gender by rememberSaveable() { mutableStateOf(registrationFormDefaultData.gender) }
        var course by rememberSaveable() { mutableStateOf(registrationFormDefaultData.course) }
        var difficulty by rememberSaveable() { mutableStateOf(registrationFormDefaultData.difficulty) }

        var birthDate by rememberSaveable() { mutableStateOf(registrationFormDefaultData.birthDate) }
        var showDatePicker by rememberSaveable() { mutableStateOf(false) }

        val courses = listOf("1 курс", "2 курс", "3 курс", "4 курс")
        var courseExpanded by rememberSaveable() { mutableStateOf(false) }

        var player by remember { mutableStateOf<Player?>(null) }

        var playerToDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
        var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

        Text("Выберите игрока:")
        ExposedDropdownMenuBox(
            expanded = playerExpanded,
            onExpandedChange = { playerExpanded = !playerExpanded }
        ) {
            OutlinedTextField(
                value = selectedPlayerId?.let { id ->
                    players.find { it.id == id }?.fullName
                } ?: "Новый игрок",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = playerExpanded)
                },
                modifier = Modifier.menuAnchor(
                    ExposedDropdownMenuAnchorType.PrimaryNotEditable
                )
            )

            ExposedDropdownMenu(
                expanded = playerExpanded,
                onDismissRequest = { playerExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Новый игрок") },
                    onClick = {
                        selectedPlayerId = null
                        fullName = RegistrationFormDefaultData().fullName
                        gender = RegistrationFormDefaultData().gender
                        course = RegistrationFormDefaultData().course
                        difficulty = RegistrationFormDefaultData().difficulty
                        birthDate = RegistrationFormDefaultData().birthDate
                        playerExpanded = false
                    }
                )

                players.forEach { p ->
                    DropdownMenuItem(
                        text = { Text("${p.fullName} (#${p.id})") },
                        onClick = {
                            selectedPlayerId = p.id
                            fullName = p.fullName
                            gender = p.gender
                            course = p.course
                            difficulty = p.difficulty.toFloat()
                            birthDate = Calendar.getInstance().apply {
                                timeInMillis = p.birthDateMillis
                            }
                            playerExpanded = false
                        },
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .clickable {
                                        playerToDeleteId = p.id
                                        playerExpanded = false
                                        showDeleteDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить игрока",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    )
                }
            }
        }

        Text("Введите ФИО:")
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
        )

        Spacer(Modifier.height(12.dp))

        Text("Выберите пол:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = gender == Gender.MALE.value,
                onClick = { gender = Gender.MALE.value }
            )
            Text(Gender.MALE.value, modifier = Modifier.padding(end = 16.dp))
            RadioButton(
                selected = gender == Gender.FEMALE.value,
                onClick = { gender = Gender.FEMALE.value }
            )
            Text(Gender.FEMALE.value, modifier = Modifier.padding(end = 16.dp))
        }

        Spacer(Modifier.height(12.dp))

        Text("Выберите курс:")
        ExposedDropdownMenuBox(
            expanded = courseExpanded,
            onExpandedChange = { courseExpanded = !courseExpanded }
        ) {
            OutlinedTextField(
                value = courses[course - 1],
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded)
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = courseExpanded,
                onDismissRequest = { courseExpanded = false }
            ) {
                courses.forEachIndexed { index, name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            course = index + 1
                            courseExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Выберите уровень сложности:")
        Slider(
            value = difficulty,
            onValueChange = { difficulty = it },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        )
        Text("Значение: ${difficulty.toInt()}")

        Spacer(Modifier.height(12.dp))

        Text("Выберите дату рождения:")
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", LocalLocale.current.platformLocale)
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        ) {
            Text(dateFormat.format(birthDate.time))
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = birthDate.timeInMillis
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = it
                                }
                                birthDate = cal
                            }
                            showDatePicker = false
                        }
                    ) { Text("ОК") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
                    ) { Text("Отмена") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(Modifier.height(12.dp))

        FilledTonalButton(onClick = {
            if (fullName.isBlank()) {
                return@FilledTonalButton
            }
            val zodiac = ZodiacUtil.getZodiac(birthDate)

            scope.launch {
                val entity = PlayerEntity(
                    id = selectedPlayerId ?: 0L,
                    fullName = fullName.trim(),
                    gender = gender,
                    course = course,
                    difficulty = difficulty.toInt(),
                    birthDateMillis = birthDate.timeInMillis,
                    zodiac = zodiac.name,
                    zodiacResId = zodiac.resId
                )

                val savedId = repository.upsertPlayer(entity)
                currentPlayer.value = repository.getById(savedId)
            }
        }) {
            Text("Зарегистрировать игрока")
        }

//        вывод зарегистрированного игрока
        currentPlayer.value?.let { p ->
            Text("Зарегистрирован игрок:")
            Spacer(Modifier.height(4.dp))

            Text("ФИО: ${p.fullName}")
            Text("Пол: ${p.gender}")
            Text("Курс: ${p.course}")
            Text("Уровень сложности: ${p.difficulty}")
            Text("Дата рождения: ${dateFormat.format(Date(p.birthDateMillis))}")
            Text("Знак зодиака:")

            Spacer(Modifier.height(12.dp))

            Image(
                painter = painterResource(id = p.zodiacResId),
                contentDescription = "Знак зодиака: ${p.zodiac}",
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        if (showDeleteDialog) {
            val idToDelete = playerToDeleteId
            val playerToDelete = players.find { it.id == idToDelete }

            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    playerToDeleteId = null
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Удалить игрока?")
                        IconButton(
                            onClick = {
                                showDeleteDialog = false
                                playerToDeleteId = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть"
                            )
                        }
                    }
                },
                text = {
                    Text(
                        "Игрок ${playerToDelete?.fullName ?: ""} и все его рекорды " +
                                "будут удалены безвозвратно."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (idToDelete == null) {
                                showDeleteDialog = false
                                return@TextButton
                            }
                            scope.launch {
                                repository.deletePlayer(idToDelete)

                                if (currentPlayer.value?.id == idToDelete) {
                                    currentPlayer.value = null
                                }
                                if (selectedPlayerId == idToDelete) {
                                    selectedPlayerId = null
                                    fullName = RegistrationFormDefaultData().fullName
                                    gender = RegistrationFormDefaultData().gender
                                    course = RegistrationFormDefaultData().course
                                    difficulty = RegistrationFormDefaultData().difficulty
                                    birthDate = RegistrationFormDefaultData().birthDate
                                }

                                showDeleteDialog = false
                                playerToDeleteId = null
                            }
                        }
                    ) {
                        Text("Да", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        playerToDeleteId = null
                    }) {
                        Text("Нет")
                    }
                }
            )
        }
    }
}

// нужен data class
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun SelectPlayerDropdown() {
//}

//@Preview(showBackground = true)
//@Composable
//fun RegistrationFormScreenPreview() {
//    MaterialTheme {
//        RegistrationFormScreen()
//    }
//}