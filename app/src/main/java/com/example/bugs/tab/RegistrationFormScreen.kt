package com.example.bugs.tab

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bugs.model.Gender
import com.example.bugs.model.Player
import com.example.bugs.util.ZodiacUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationFormScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Регистрация игрока",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(24.dp))

        var fullName by rememberSaveable() { mutableStateOf("Иванов Иван Иванович") }
        var gender by rememberSaveable() { mutableStateOf(Gender.MALE) }
        var course by rememberSaveable() { mutableStateOf(1) }
        var difficulty by rememberSaveable() { mutableStateOf(1f) }

        var birthDate by rememberSaveable() { mutableStateOf(Calendar.getInstance()) }
        var showDatePicker by rememberSaveable() { mutableStateOf(false) }

        val courses = listOf("1 курс", "2 курс", "3 курс", "4 курс")
        var courseExpanded by rememberSaveable() { mutableStateOf(false) }

        var player by rememberSaveable() { mutableStateOf<Player?>(null) }

        Text("Введите ФИО:")
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
        )

        Spacer(Modifier.height(12.dp))

        Text("Выберите пол:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = gender == Gender.MALE,
                onClick = { gender = Gender.MALE }
            )
            Text("Мужской", modifier = Modifier.padding(end = 16.dp))
            RadioButton(
                selected = gender == Gender.FEMALE,
                onClick = { gender = Gender.FEMALE }
            )
            Text("Женский", modifier = Modifier.padding(end = 16.dp))
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
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
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

        Button(onClick = {
            if (fullName.isBlank()) {
                player = null
                return@Button
            }
            val zodiac = ZodiacUtil.getZodiac(birthDate)
            player = Player(
                fullName = fullName.trim(),
                gender = gender,
                course = course,
                difficulty = difficulty.toInt(),
                birthDate = birthDate,
                zodiac = zodiac.name,
                zodiacResId = zodiac.resId
            )
        }) {
            Text("Зарегистрировать игрока")
        }

        player?.let { p ->
            Text("Зарегистрирован игрок:")
            Spacer(Modifier.height(4.dp))

            Text("ФИО: ${p.fullName}")
            Text("Пол: ${if (p.gender == Gender.MALE)  "Мужской" else "Женский"}")
            Text("Курс: ${p.course}")
            Text("Уровень сложности: ${p.difficulty}")
            Text("Дата рождения: ${dateFormat.format(p.birthDate.time)}")
            Text("Знак зодиака:")

            Spacer(Modifier.height(12.dp))

            if (p.zodiac != "") {
                Image(
                    painter = painterResource(id = p.zodiacResId),
                    contentDescription = "Знак зодиака: ${p.zodiac}",
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun RegistrationFormScreenPreview() {
//    MaterialTheme {
//        RegistrationFormScreen()
//    }
//}