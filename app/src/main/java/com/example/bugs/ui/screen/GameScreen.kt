package com.example.bugs.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugs.R
import com.example.bugs.data.model.GameSettings
import com.example.bugs.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
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
        .background(Color.White)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val bugImage = ImageBitmap.imageResource(R.drawable.bug)

        LaunchedEffect(widthPx, heightPx) {
            viewModel.setScreenSize(widthPx, heightPx)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        viewModel.onTap(offset.x, offset.y)
                    }
                }
        ) {
            viewModel.bugs.forEach { bug ->
                withTransform({
                    translate(left = bug.x, top = bug.y)
                }) {
                    drawImage(
                        image = bugImage,
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(bug.size.toInt(), bug.size.toInt())
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
            Text("Очки: ${viewModel.score}", color = Color.Black)
            Text("Время: ${viewModel.timeLeft} сек", color = Color.Black)
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
        text = { Text("Ваш счёт: ${viewModel.score}") },
        confirmButton = {
            TextButton(
                onClick = { viewModel.resetGame() }
            ) {
                Text("Играть снова")
            }
        }
    )
}