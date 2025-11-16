package com.pianokids.game.view.screens.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.components.LevelIntroDialog
import com.pianokids.game.viewmodel.LevelViewModel
import com.pianokids.game.R
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign

@Composable
fun LevelScreen(
    userId: String,
    levelId: String,
    viewModel: LevelViewModel = viewModel(),
    onExit: () -> Unit
) {
    val state = viewModel.uiState.collectAsState().value
    var showIntro by remember { mutableStateOf(true) }

    LaunchedEffect(levelId) {
        viewModel.loadLevel(levelId)
    }

    if (state.isLoading || state.currentLevel == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    val level = state.currentLevel

    if (showIntro && level != null) {
        LevelIntroDialog(
            heroImageRes = R.drawable.hero_batman,   // later dynamic
            storyText = level.story,
            onFinished = {
                showIntro = false
                SoundManager.stopBackgroundMusic()
                // later: start level music
            }
        )
    }

    val shakeOffset by animateFloatAsState(
        targetValue = if (state.showWrongAnimation) 15f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        finishedListener = {
            // Reset animation flag
            viewModel.clearWrongAnimation()
        }
    )

    if (!showIntro) {
    Box(modifier = Modifier.fillMaxSize()) {

        // BACKGROUND
        AsyncImage(
            model = level.backgroundUrl,
            contentDescription = "Level Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // ---------------------------------------------------------
            // CARDS ABOVE PROGRESS BAR
            // ---------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // LEFT: Avatar Card
                Box(
                    modifier = Modifier
                        .size(width = 180.dp, height = 250.dp)
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Avatar", color = Color.Black)
                }

                // RIGHT: Boss Card
                Box(
                    modifier = Modifier
                        .size(width = 180.dp, height = 250.dp)
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = level.bossUrl,
                        contentDescription = "Boss",
                        modifier = Modifier.fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }

            // ---------------------------------------------------------
            // PROGRESS BAR (center)
            // ---------------------------------------------------------
            Box(
                modifier = Modifier
                    .offset(x = shakeOffset.dp)   // 🔥 shake left/right
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .drawBehind {
                        if (state.showWrongAnimation) {
                            drawRoundRect(
                                color = Color.Red,
                                size = size,
                                cornerRadius = CornerRadius(10f, 10f),
                                style = Stroke(width = 4f)
                            )
                        }
                    }
            ) {
                LinearProgressIndicator(
                    progress = state.progressPercentage,
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF00D9FF),
                    trackColor = Color.White.copy(alpha = 0.4f)
                )
            }

            if (state.wrongMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = shakeOffset.dp),   // keeps the shake effect
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.wrongMessage!!,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // NEXT NOTE
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Next: ${level.expectedNotes[state.currentNoteIndex]}",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------------------------------------------------
            // CONTENT BELOW — FILL EMPTY SPACE
            // ---------------------------------------------------------
            Spacer(modifier = Modifier.weight(1f))

            // ---------------------------------------------------------
            // PIANO PLACEHOLDER AT BOTTOM
            // ---------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎹 PIANO PLACEHOLDER", color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TEST BUTTONS (only for now)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    val nextNote = level.expectedNotes[state.currentNoteIndex]
                    viewModel.onNotePlayed(nextNote)
                }) { Text("Correct Note") }

                Button(onClick = {
                    viewModel.onNotePlayed("WRONG_NOTE")
                }) { Text("Wrong Note") }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

        // SUCCESS / FAILURE DIALOGS (unchanged)
        if (state.isLevelCompleted) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Level Complete!") },
                text = { Text("Great job! Your score has been saved.") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.saveProgress(userId)
                        onExit()
                    }) { Text("Continue") }
                }
            )
        }

        if (state.isFailed) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Level Failed!") },
                text = { Text("Try again!") },
                confirmButton = {
                    Button(onClick = { viewModel.loadLevel(levelId) }) { Text("Retry") }
                },
                dismissButton = {
                    Button(onClick = onExit) { Text("Exit") }
                }
            )
        }
    }
}


