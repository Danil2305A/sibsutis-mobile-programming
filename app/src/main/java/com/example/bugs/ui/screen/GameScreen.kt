package com.example.bugs.ui.screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugs.R
import com.example.bugs.data.db.entity.PlayerEntity
import com.example.bugs.data.db.entity.ScoreEntity
import com.example.bugs.data.model.Bug
import com.example.bugs.data.model.BugType
import com.example.bugs.data.model.DifficultyPreset
import com.example.bugs.data.model.GameSettings
import com.example.bugs.data.repository.RepositoryProvider
import com.example.bugs.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun GameScreen(
    settings: MutableState<GameSettings>,
    currentPlayer: MutableState<PlayerEntity?>
) {
    val viewModel: GameViewModel = viewModel()

    val context = LocalContext.current
    val repository = remember { RepositoryProvider.get(context) }

    LaunchedEffect(settings.value) {
        viewModel.applySettings(settings.value)
    }

    LaunchedEffect(currentPlayer.value?.id) {
        viewModel.onPlayerChanged(currentPlayer.value?.id)
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dtSeconds = (now - lastNanos) / 1_000_000_000f
                    val dt = dtSeconds * 60f
                    viewModel.updatePositions(dt)
                }
                lastNanos = now
            }
        }
    }

    val spawnInterval = 0.5.seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(spawnInterval)
            viewModel.spawnBug()
        }
    }

    val delayDuration = 1.seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(delayDuration)
            viewModel.tick()
        }
    }

    LaunchedEffect(viewModel.isGameOver, currentPlayer.value?.id) {
        if (!viewModel.isGameOver) return@LaunchedEffect
        if (viewModel.scoreSaved) return@LaunchedEffect

        val player = currentPlayer.value ?: return@LaunchedEffect
        val diffPreset = DifficultyPreset.from(settings.value)

        repository.saveScore(
            ScoreEntity(
                playerId = player.id,
                score = viewModel.score,
                hits = viewModel.hits,
                misses = viewModel.misses,
                difficulty = diffPreset,
                settingsSpeed = settings.value.speed,
            )
        )
        viewModel.markScoreSaved()
    }

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    val tiltSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    DisposableEffect(tiltSensor) {
        if (tiltSensor == null) return@DisposableEffect onDispose { }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val g = SensorManager.GRAVITY_EARTH
                viewModel.onTilt(
                    x = -event.values[0] / g,
                    y = event.values[1] / g
                )
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            tiltSensor,
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose { sensorManager.unregisterListener(listener) }
    }

    val screamPlayer = remember {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            try {
                context.resources.openRawResourceFd(R.raw.bug_scream).use { afd ->
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }
                prepare()
            } catch (e: Exception) {
                android.util.Log.e("BugScream", "prepare failed", e)
            }
            isLooping = true
            setVolume(0.1f ,0.1f)
        }
    }

    DisposableEffect(Unit) {
        onDispose { screamPlayer?.release() }
    }

    LaunchedEffect(viewModel.tiltModeActive) {
        val player = screamPlayer ?: return@LaunchedEffect

        if (viewModel.tiltModeActive) {
            player.seekTo(0)
            player.start()
        } else {
            if (player.isPlaying) player.pause()
        }
    }

    GameField(viewModel)
}

@Composable
fun GameField(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (viewModel.tiltModeActive) {
                Text(
                    text = "НАКЛОН!",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }

            Text("Очки: ${viewModel.score}")
            Text("Время: ${viewModel.timeLeft} сек")
        }

        BoxWithConstraints(modifier = modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            val fieldHeight = Bug.FIELD_HEIGHT
            val fieldWidth = fieldHeight * (widthPx / heightPx)


            val scale = heightPx / fieldHeight

            fun toScreenX(x: Float) = x * scale
            fun toScreenY(y: Float) = y * scale
            fun toLogicalX(px: Float) = px / scale
            fun toLogicalY(py: Float) = py / scale

            LaunchedEffect(fieldWidth, fieldHeight) {
                viewModel.setFieldSize(fieldWidth, fieldHeight)
            }

            val bugImages = BugType.entries.associateWith {
                ImageBitmap.imageResource(it.imageRes)
            }

            val bonusImage = ImageBitmap.imageResource(R.drawable.bonus_tilt)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val logicalX = toLogicalX(offset.x)
                            val logicalY = toLogicalY(offset.y)

                            if (logicalX in 0f..fieldWidth &&
                                logicalY in 0f..fieldHeight
                            ) {
                                viewModel.onTap(logicalX, logicalY)
                            }
                        }
                    }
            ) {
                viewModel.bugs.forEach { bug ->
                    val half = bug.size / 2f
                    val centerX = toScreenX(bug.x + half)
                    val centerY = toScreenY(bug.y + half)
                    val bugSizeInPixels = bug.size * scale
                    val halfBugSizeInPixels = bugSizeInPixels / 2f

                    withTransform({
                        translate(left = centerX, top = centerY)
                        rotate(degrees = bug.angle, pivot = Offset.Zero)
                    }) {
                        drawImage(
                            image = bugImages.getValue(bug.type),
                            dstOffset = IntOffset(
                                (-halfBugSizeInPixels).toInt(),
                                (-halfBugSizeInPixels).toInt()
                            ),
                            dstSize = IntSize(
                                bugSizeInPixels.toInt(),
                                bugSizeInPixels.toInt()
                            )
                        )
                    }
                }

                if (viewModel.bonusVisible) {
                    val centerX = toScreenX(viewModel.bonusX + viewModel.bonusSize / 2f)
                    val centerY = toScreenY(viewModel.bonusY + viewModel.bonusSize / 2f)
                    val bonusSizePx = viewModel.bonusSize * scale
                    val half = bonusSizePx / 2f

                    withTransform({
                        translate(left = centerX, top = centerY)
                        rotate(degrees = -25f, pivot = Offset.Zero)
                    }) {
                        drawImage(
                            image = bonusImage,
                            dstOffset = IntOffset((-half).toInt(), (-half).toInt()),
                            dstSize = IntSize(bonusSizePx.toInt(), bonusSizePx.toInt())
                        )
                    }
                }
            }

            if (viewModel.isGameOver) {
                GameOverDialog(viewModel)
            }
        }
    }
}

@Composable
fun GameOverDialog(viewModel: GameViewModel) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Игра окончена") },
        text = {
            Column {
                Text("Очки: ${viewModel.score}")
                Text("Попадания: ${viewModel.hits}")
                Text("Промахи: ${viewModel.misses}")
                Text("Точность: ${"%.1f".format(viewModel.accuracy)}%")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.resetGame() }
            ) {
                Text("Играть снова")
            }
        }
    )
}