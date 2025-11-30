package com.pianokids.game.view.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class FallingNote(
    val id: Int,
    val note: String,
    val offsetY: Float, // 0.0 (top) to 1.0 (bottom/hit zone)
    val lane: Int, // 0-11 for 12 piano keys
    val lengthFactor: Float = 1f,
    val isHit: Boolean = false,
    val isMissed: Boolean = false
)

@Composable
fun FallingNotesView(
    expectedNotes: List<String>,
    currentNoteIndex: Int,
    noteDurations: List<Float>,
    onNoteHit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fallingNotes by remember { mutableStateOf<List<FallingNote>>(emptyList()) }
    var nextNoteToSpawn by remember { mutableStateOf(0) }
    val currentIndexState by rememberUpdatedState(currentNoteIndex)
    val noteDurationsState by rememberUpdatedState(noteDurations)
    
    // Map notes to piano lanes (0-11)
    val noteLaneMap = mapOf(
        "do" to 0, "do#" to 1,
        "re" to 2, "ré" to 2, "reb" to 1, "ré♭" to 1,
        "re#" to 3, "ré#" to 3, "mib" to 3, "mi♭" to 3,
        "mi" to 4,
        "fa" to 5, "fa#" to 6,
        "sol" to 7, "sol#" to 8, "solb" to 6, "sol♭" to 6,
        "la" to 9, "la#" to 10, "lab" to 8, "la♭" to 8,
        "si" to 11, "sib" to 10, "si♭" to 10
    )

    LaunchedEffect(expectedNotes) {
        fallingNotes = emptyList()
        nextNoteToSpawn = currentNoteIndex
    }

    LaunchedEffect(currentNoteIndex) {
        fallingNotes = fallingNotes.filter { it.id >= currentNoteIndex }
    }

    // Animation for falling notes
    LaunchedEffect(expectedNotes, noteDurations) {
        val baseSpeed = 0.0048f
        val freezeThreshold = 0.9f
        val previewWindow = 5
        val spawnSpacingBase = 0.12f
        while (true) {
            delay(16)

            val currentIndex = currentIndexState

            // Update existing notes positions and freeze current note at hit zone
            fallingNotes = fallingNotes
                .filter { it.id >= currentIndex }
                .map { note ->
                    val isCurrent = note.id == currentIndex
                    val speed = baseSpeed / note.lengthFactor.coerceAtLeast(0.5f)
                    val nextOffset = note.offsetY + speed
                    val targetOffset = if (isCurrent && nextOffset >= freezeThreshold) freezeThreshold else nextOffset
                    note.copy(offsetY = targetOffset)
                }

            // Spawn limited future notes
            val previousIndex = nextNoteToSpawn - 1
                val previousCompleted = previousIndex < currentIndex
                val previousReady = previousIndex < 0 || previousCompleted

            val canSpawn = nextNoteToSpawn < expectedNotes.size &&
                    nextNoteToSpawn < currentIndex + previewWindow &&
                    fallingNotes.none { it.id == nextNoteToSpawn } &&
                    previousReady

            if (canSpawn) {
                val noteText = expectedNotes[nextNoteToSpawn]
                val lane = noteLaneMap[noteText.lowercase()] ?: 0
                val lengthFactor = noteDurationsState.getOrNull(nextNoteToSpawn) ?: 1f
                val prevDuration = noteDurationsState.getOrNull(nextNoteToSpawn - 1) ?: 1f
                val pauseBonus = if (prevDuration >= 1.2f && lengthFactor <= 0.35f) 0.08f else 0f
                val spacing = spawnSpacingBase * lengthFactor.coerceIn(0.6f, 2.2f) + pauseBonus
                val highestOffset = fallingNotes.minOfOrNull { it.offsetY } ?: -0.3f
                val spawnOffset = minOf(highestOffset - spacing, -0.35f)
                fallingNotes = fallingNotes + FallingNote(
                    id = nextNoteToSpawn,
                    note = noteText,
                    offsetY = spawnOffset,
                    lane = lane,
                    lengthFactor = lengthFactor
                )
                nextNoteToSpawn++
            }
        }
    }

    // Check if note is in hit zone
    LaunchedEffect(currentNoteIndex) {
        fallingNotes.forEach { note ->
            if (note.id == currentNoteIndex && note.offsetY in 0.85f..0.95f && !note.isHit) {
                // Note is in perfect hit zone
                onNoteHit()
            }
        }
    }

    Box(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val containerHeight = maxHeight
            val containerWidth = maxWidth
            val laneWidth = containerWidth / 12
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val laneWidthPx = size.width / 12f
                
                // Draw lane dividers
                for (i in 0..12) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(i * laneWidthPx, 0f),
                        end = Offset(i * laneWidthPx, size.height),
                        strokeWidth = 1.5f
                    )
                }
                
                // Highlight active lanes (where notes are falling)
                fallingNotes.forEach { note ->
                    if (note.offsetY > -0.1f && note.offsetY < 1.0f) {
                        val laneX = note.lane * laneWidthPx
                        drawRect(
                            color = Color(0xFF00D9FF).copy(alpha = 0.05f),
                            topLeft = Offset(laneX, 0f),
                            size = Size(laneWidthPx, size.height)
                        )
                        
                        // Draw stronger highlight for current note
                        if (note.id == currentNoteIndex) {
                            drawRect(
                                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                topLeft = Offset(laneX, 0f),
                                size = Size(laneWidthPx, size.height)
                            )
                        }
                    }
                }
                
                // Draw hit zone line (where notes should be played)
                val hitZoneY = size.height * 0.92f
                drawLine(
                    color = Color(0xFF00D9FF),
                    start = Offset(0f, hitZoneY),
                    end = Offset(size.width, hitZoneY),
                    strokeWidth = 5f
                )
                
                // Draw hit zone glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00D9FF).copy(alpha = 0.4f),
                            Color(0xFF00D9FF).copy(alpha = 0.0f)
                        ),
                        startY = hitZoneY - 50f,
                        endY = hitZoneY
                    ),
                    topLeft = Offset(0f, hitZoneY - 50f),
                    size = Size(size.width, 50f)
                )
            }
            
            // Draw falling notes
            fallingNotes.forEach { note ->
                val xPos = laneWidth * note.lane
                val yPos = containerHeight * note.offsetY
                val noteHeight = (70.dp * note.lengthFactor.coerceIn(0.5f, 2.5f)) + 40.dp
                
                Box(
                    modifier = Modifier
                        .offset(x = xPos + (laneWidth * 0.075f), y = yPos)
                        .width(laneWidth * 0.85f)
                        .height(noteHeight)
                        .shadow(16.dp, RoundedCornerShape(12.dp))
                        .background(
                            brush = when {
                                note.isHit -> Brush.verticalGradient(
                                    listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
                                )
                                note.isMissed -> Brush.verticalGradient(
                                    listOf(Color(0xFFFF5E62), Color(0xFFE53935))
                                )
                                note.id == currentNoteIndex -> Brush.verticalGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFFFA726))
                                )
                                else -> Brush.verticalGradient(
                                    listOf(Color(0xFF00D9FF), Color(0xFF667EEA))
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 3.dp,
                            color = when {
                                note.id == currentNoteIndex -> Color(0xFFFFD700)
                                else -> Color.White.copy(alpha = 0.4f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = note.note.uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
