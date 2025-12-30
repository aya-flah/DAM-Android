package com.pianokids.game.view.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.pianokids.game.R
import com.pianokids.game.utils.PianoSoundManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.view.components.PracticeFallingNotesView
import kotlin.random.Random

@Composable
fun PracticeScreen(
    onExit: () -> Unit
) {
    val context = LocalContext.current

    // ✅ init audio once
    LaunchedEffect(Unit) {
        PianoSoundManager.init(context)
    }

    // drives PracticeFallingNotesView
    var lastPlayedMidi by remember { mutableStateOf<Int?>(null) }

    fun playAndSpawn(solfege: String, midi: Int) {
        PianoSoundManager.playNote(solfege)

        // force update even if same midi tapped again
        lastPlayedMidi = null
        lastPlayedMidi = midi
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // --------------------------------------------------
        // ✅ BACKGROUND GIF (same technique as LevelScreen)
        // --------------------------------------------------
        // Replace this with your practice gif drawable
        val practiceGifRes = R.drawable.practice_bg

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(practiceGifRes)
                .decoderFactory(
                    if (Build.VERSION.SDK_INT >= 28) ImageDecoderDecoder.Factory()
                    else GifDecoder.Factory()
                )
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay (same vibe as LevelScreen)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.80f)
                        )
                    )
                )
        )

        // --------------------------------------------------
        // Falling notes
        // --------------------------------------------------
        PracticeFallingNotesView(
            lastPlayedMidi = lastPlayedMidi,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 92.dp,
                    bottom = 240.dp
                )
        )

        // --------------------------------------------------
        // Top bar
        // --------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp, start = 14.dp, end = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onExit() },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .border(2.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(50))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_exit),
                    contentDescription = "Exit Practice",
                    tint = Color(0xFFFF5E62)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Practice",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Play anything 🎹 (free mode)",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.size(48.dp))
        }

        // --------------------------------------------------
        // Bottom area
        // --------------------------------------------------
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Dev buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        SoundManager.playClick()
                        val (solfege, midi) = randomSolfegeAndMidi()
                        playAndSpawn(solfege, midi)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA))
                ) { Text("Spawn Note") }

                OutlinedButton(
                    onClick = {
                        SoundManager.playClick()
                        lastPlayedMidi = null
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Clear") }
            }

            Text(
                text = "Test Notes",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TestNoteButton("Do") { playAndSpawn("Do", 60) }
                    TestNoteButton("Ré") { playAndSpawn("Ré", 62) }
                    TestNoteButton("Mi") { playAndSpawn("Mi", 64) }
                    TestNoteButton("Fa") { playAndSpawn("Fa", 65) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TestNoteButton("Sol") { playAndSpawn("Sol", 67) }
                    TestNoteButton("La") { playAndSpawn("La", 69) }
                    TestNoteButton("Si") { playAndSpawn("Si", 71) }
                    TestNoteButton("Do#") { playAndSpawn("Do#", 61) }
                    TestNoteButton("Ré#") { playAndSpawn("Ré#", 63) }
                }
            }

            // Placeholder keyboard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎹 Full Keyboard Placeholder\n(teammate will plug full 88 keys here)",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RowScope.TestNoteButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

private fun randomSolfegeAndMidi(): Pair<String, Int> {
    val options = listOf(
        "Do" to 60, "Do#" to 61,
        "Ré" to 62, "Ré#" to 63,
        "Mi" to 64,
        "Fa" to 65, "Fa#" to 66,
        "Sol" to 67, "Sol#" to 68,
        "La" to 69, "La#" to 70,
        "Si" to 71
    )
    return options.random()
}
