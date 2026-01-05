package com.pianokids.game.view.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

data class PracticeFallingNote(
    val id: Long,
    val midi: Int,
    val lane: Int,
    val offsetY: Float,
    val noteName: String = ""
)

@Composable
fun PracticeFallingNotesView(
    laneCount: Int = 12,
    minMidi: Int = 60,
    maxMidi: Int = 71,
    // Pass events here by changing this value (ex: increment a counter, or push latest midi)
    lastPlayedMidi: Int?,
    modifier: Modifier = Modifier
) {
    var notes by remember { mutableStateOf<List<PracticeFallingNote>>(emptyList()) }
    
    // Map MIDI to note names and lanes (0-11 for 12 piano keys)
    val midiToNoteName = mapOf(
        60 to "DO", 61 to "DO#",
        62 to "RÉ", 63 to "RÉ#",
        64 to "MI",
        65 to "FA", 66 to "FA#",
        67 to "SOL", 68 to "SOL#",
        69 to "LA", 70 to "LA#",
        71 to "SI"
    )
    
    val midiToLane = mapOf(
        60 to 0, 61 to 1,
        62 to 2, 63 to 3,
        64 to 4,
        65 to 5, 66 to 6,
        67 to 7, 68 to 8,
        69 to 9, 70 to 10,
        71 to 11
    )

    // spawn on new input
    LaunchedEffect(lastPlayedMidi) {
        val midi = lastPlayedMidi ?: return@LaunchedEffect
        val lane = midiToLane[midi] ?: return@LaunchedEffect
        val noteName = midiToNoteName[midi] ?: ""

        notes = notes + PracticeFallingNote(
            id = System.nanoTime(),
            midi = midi,
            lane = lane,
            offsetY = -0.35f,
            noteName = noteName
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

            // Draw lane dividers for 12 piano keys
            for (i in 0..laneCount) {
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(i * laneWidthPx, 0f),
                    end = Offset(i * laneWidthPx, size.height),
                    strokeWidth = 1.5f
                )
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

        // draw notes as boxes with text labels (matching FallingNotesView style)
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val containerH = maxHeight
            val containerW = maxWidth
            val laneW = containerW / laneCount

            notes.forEach { n ->
                val x = laneW * n.lane
                val y = containerH * n.offsetY

                Box(
                    modifier = Modifier
                        .offset(x = x + (laneW * 0.075f), y = y)
                        .width(max(laneW * 0.85f, 6.dp))
                        .height(70.dp)
                        .shadow(16.dp, RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF00D9FF), Color(0xFF667EEA))
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 3.dp,
                            color = Color.White.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (n.noteName.isNotEmpty()) {
                        Text(
                            text = n.noteName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
