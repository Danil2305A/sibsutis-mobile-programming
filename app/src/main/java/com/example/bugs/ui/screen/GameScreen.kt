package com.example.bugs.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugs.R
import com.example.bugs.data.model.Bug
import com.example.bugs.data.model.BugType
import com.example.bugs.data.model.GameSettings
import com.example.bugs.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun GameScreen(settings: MutableState<GameSettings>) {
    val viewModel: GameViewModel = viewModel()

    LaunchedEffect(settings.value) {
        viewModel.applySettings(settings.value)
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

    GameField(viewModel)
}

@Composable
fun GameField(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier
        .fillMaxSize()
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
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Очки: ${viewModel.score}")
            Text("Время: ${viewModel.timeLeft} сек")
        }

        if (viewModel.isGameOver) {
            GameOverDialog(viewModel)
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