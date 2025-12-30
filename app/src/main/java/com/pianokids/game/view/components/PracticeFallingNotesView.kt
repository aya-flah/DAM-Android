package com.pianokids.game.view.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

data class PracticeFallingNote(
    val id: Long,
    val midi: Int,
    val lane: Int,
    val offsetY: Float
)

@Composable
fun PracticeFallingNotesView(
    laneCount: Int = 88,
    minMidi: Int = 21,
    maxMidi: Int = 108,
    // Pass events here by changing this value (ex: increment a counter, or push latest midi)
    lastPlayedMidi: Int?,
    modifier: Modifier = Modifier
) {
    var notes by remember { mutableStateOf<List<PracticeFallingNote>>(emptyList()) }

    // spawn on new input
    LaunchedEffect(lastPlayedMidi) {
        val midi = lastPlayedMidi ?: return@LaunchedEffect
        val clamped = midi.coerceIn(minMidi, maxMidi)
        val lane = (clamped - minMidi).coerceIn(0, laneCount - 1)

        notes = notes + PracticeFallingNote(
            id = System.nanoTime(),
            midi = clamped,
            lane = lane,
            offsetY = -0.35f
        )
    }

    // animate falling
    LaunchedEffect(Unit) {
        val speed = 0.0075f
        while (true) {
            delay(16)
            notes = notes
                .map { it.copy(offsetY = it.offsetY + speed) }
                .filter { it.offsetY <= 1.15f }
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val laneWidthPx = size.width / laneCount.toFloat()

            // (optional) sparse dividers (88 dividers is too much noise)
            // Draw every octave boundary (12 keys)
            for (i in 0..laneCount) {
                if (i % 12 == 0) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.12f),
                        start = Offset(i * laneWidthPx, 0f),
                        end = Offset(i * laneWidthPx, size.height),
                        strokeWidth = 2f
                    )
                }
            }

            // hit zone
            val hitZoneY = size.height * 0.92f
            drawLine(
                color = Color(0xFF00D9FF),
                start = Offset(0f, hitZoneY),
                end = Offset(size.width, hitZoneY),
                strokeWidth = 5f
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00D9FF).copy(alpha = 0.35f),
                        Color(0xFF00D9FF).copy(alpha = 0.0f)
                    ),
                    startY = hitZoneY - 50f,
                    endY = hitZoneY
                ),
                topLeft = Offset(0f, hitZoneY - 50f),
                size = Size(size.width, 50f)
            )
        }

        // draw notes as boxes
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val containerH = maxHeight
            val containerW = maxWidth
            val laneW = containerW / laneCount

            notes.forEach { n ->
                val x = laneW * n.lane
                val y = containerH * n.offsetY

                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .width(max(laneW * 0.9f, 6.dp))
                        .height(70.dp)
                        .shadow(10.dp, RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF00D9FF), Color(0xFF667EEA))
                            ),
                            RoundedCornerShape(10.dp)
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                )
            }
        }
    }
}