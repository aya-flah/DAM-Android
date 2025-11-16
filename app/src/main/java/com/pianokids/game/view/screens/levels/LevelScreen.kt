package com.pianokids.game.view.screens.levels

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pianokids.game.utils.components.LevelCompletedDialog

@Composable
fun LevelScreen(
    userId: String,
    levelId: String,
    viewModel: LevelViewModel = viewModel(),
    onExit: () -> Unit
) {
    val state = viewModel.uiState.collectAsState().value
    var showIntro by remember { mutableStateOf(true) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(levelId) {
        viewModel.loadLevel(levelId)
    }

    // LOADING
    if (state.isLoading || state.currentLevel == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF00D9FF))
        }
        return
    }

    val level = state.currentLevel

    // -------------------------------------------------------------
    // INTRO DIALOG
    // -------------------------------------------------------------
    if (showIntro) {
        LevelIntroDialog(
            heroImageRes = if (level.theme == "Batman") {
                R.drawable.hero_batman
            } else {
                R.drawable.hero_spiderman
            },
            storyText = level.story,
            onFinished = {
                showIntro = false
                SoundManager.stopBackgroundMusic()
            }
        )
    }

    // ANIMATIONS
    val shakeOffset by animateFloatAsState(
        targetValue = if (state.showWrongAnimation) 15f else 0f,
        animationSpec = tween(300, easing = LinearEasing),
        finishedListener = { viewModel.clearWrongAnimation() }
    )

    val avatarOffsetX by animateDpAsState(
        targetValue = if (!showIntro) 0.dp else (-300).dp,
        animationSpec = tween(
            durationMillis = 700,
            easing = {
                OvershootInterpolator(1.2f).getInterpolation(it)
            }
        )
    )

    val bossOffsetX by animateDpAsState(
        targetValue = if (!showIntro) 0.dp else (300).dp,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis = 150,
            easing = {
                OvershootInterpolator(1.2f).getInterpolation(it)
            }
        )
    )

    // Pulsing animation for next note
    val pulseScale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // -------------------------------------------------------------
    // MAIN UI
    // -------------------------------------------------------------
    if (!showIntro) {
        Box(modifier = Modifier.fillMaxSize()) {

            // BACKGROUND with dark overlay for better contrast
            AsyncImage(
                model = level.backgroundUrl,
                contentDescription = "bg",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark gradient overlay for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // --------------------------
                // SCORE & HEARTS (TOP BAR)
                // --------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Score display
                    Box(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Score: ${state.score}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Lives/Hearts
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .background(
                                Color.Black.copy(alpha = 0.4f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        repeat(3) { index ->
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = if (index < (3 - state.wrongNoteCount)) {
                                    Color(0xFFFF6B9D)
                                } else {
                                    Color.Gray.copy(alpha = 0.3f)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --------------------------
                // CHARACTER CARDS
                // --------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // AVATAR (Hero)
                    Box(
                        modifier = Modifier
                            .offset(x = avatarOffsetX)
                            .size(width = 170.dp, height = 240.dp)
                            .shadow(12.dp, RoundedCornerShape(24.dp))
                            .border(
                                width = 3.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF00D9FF),
                                        Color(0xFF667EEA)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1A1A2E).copy(alpha = 0.9f),
                                        Color(0xFF16213E).copy(alpha = 0.9f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "HERO",
                                color = Color(0xFF00D9FF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Add hero image here when available
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color.White.copy(0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🦸", fontSize = 48.sp)
                            }
                        }
                    }

                    // BOSS (Villain)
                    Box(
                        modifier = Modifier
                            .offset(x = bossOffsetX)
                            .size(width = 170.dp, height = 240.dp)
                            .shadow(12.dp, RoundedCornerShape(24.dp))
                            .border(
                                width = 3.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B9D),
                                        Color(0xFFFF5E62)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        AsyncImage(
                            model = level.bossUrl,
                            contentDescription = "Boss",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Boss label overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        ) {
                            Text(
                                "BOSS",
                                color = Color(0xFFFF6B9D),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --------------------------
                // PROGRESS BAR (Enhanced)
                // --------------------------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = shakeOffset.dp)
                ) {
                    Text(
                        text = "Progress: ${(state.progressPercentage * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .shadow(6.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
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
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(state.progressPercentage)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF00D9FF),
                                            Color(0xFF667EEA),
                                            Color(0xFF764BA2)
                                        )
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                        )
                    }
                }

                // WRONG NOTE MESSAGE
                AnimatedVisibility(
                    visible = state.wrongMessage != null,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .background(
                                    Color(0xFFFF5E62).copy(alpha = 0.9f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = state.wrongMessage ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --------------------------
                // NEXT NOTE (Enhanced)
                // --------------------------
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val nextNoteText = if (state.currentNoteIndex < level.expectedNotes.size) {
                        level.expectedNotes[state.currentNoteIndex]
                    } else {
                        "🎉"
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Next Note",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Box(
                            modifier = Modifier
                                .scale(pulseScale)
                                .shadow(16.dp, CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF667EEA),
                                            Color(0xFF764BA2)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(24.dp)
                        ) {
                            Text(
                                text = nextNoteText,
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // --------------------------
                // PIANO PLACEHOLDER (Enhanced)
                // --------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .shadow(12.dp, RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A1A2E).copy(alpha = 0.95f),
                                    Color(0xFF16213E).copy(alpha = 0.95f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0xFF00D9FF).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🎹 PIANO PLACEHOLDER",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // TEST BUTTONS (Enhanced)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (state.currentNoteIndex < level.expectedNotes.size) {
                                val next = level.expectedNotes[state.currentNoteIndex]
                                viewModel.onNotePlayed(next)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00D9FF)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                    ) {
                        Text("Correct Note", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.onNotePlayed("WRONG_NOTE")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5E62)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                    ) {
                        Text("Wrong Note", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // -------------------------------------------------------------
    // DIALOGS
    // -------------------------------------------------------------
    fun calculateStars(score: Int): Int = when {
        score >= 85 -> 3
        score >= 60 -> 2
        score >= 30 -> 1
        else -> 0
    }

    LaunchedEffect(state.isLevelCompleted) {
        if (state.isLevelCompleted) {
            viewModel.saveProgress(userId)
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        LevelCompletedDialog(
            stars = calculateStars(state.score),
            onDismiss = {
                showSuccessDialog = false
                onExit()
            }
        )
    }

    if (state.isFailed) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Level Failed!") },
            text = { Text("You've run out of lives. Try again!") },
            confirmButton = {
                Button(onClick = { viewModel.loadLevel(levelId) }) {
                    Text("🔄 Retry")
                }
            },
            dismissButton = {
                Button(onClick = onExit) {
                    Text("Exit")
                }
            }
        )
    }
}