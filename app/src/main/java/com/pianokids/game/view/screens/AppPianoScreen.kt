package com.pianokids.game.view.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pianokids.game.R
import com.pianokids.game.data.models.PianoLesson
import com.pianokids.game.data.models.LessonDifficulty
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.PianoSoundManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.view.components.PianoKeyboard
import com.pianokids.game.viewmodel.GameMode
import com.pianokids.game.viewmodel.PianoViewModel

/**
 * App Piano Screen - Main gameplay screen with virtual piano
 * This screen is reusable for all levels with different themes
 * 
 * @param levelNumber Which level is being played (1-6)
 * @param onNavigateBack Callback to return to level selection
 * @param onLessonComplete Callback when lesson is successfully completed
 */
@Composable
fun AppPianoScreen(
    levelNumber: Int = 1,
    onNavigateBack: () -> Unit,
    onLessonComplete: (stars: Int) -> Unit = {},
    viewModel: PianoViewModel = viewModel()
) {
    val context = LocalContext.current
    val pianoState by viewModel.pianoState.collectAsState()
    
    // Initialize piano sounds
    LaunchedEffect(Unit) {
        PianoSoundManager.init(context)
    }
    
    // Initialize level with default lesson for Level 1
    LaunchedEffect(levelNumber) {
        val lesson = PianoLesson(
            levelNumber = 1,
            lessonName = "Basic Keys",
            notesToLearn = listOf("Do", "Ré", "Mi"),
            sequence = listOf("Do", "Ré", "Mi", "Do", "Mi", "Do"),
            difficulty = LessonDifficulty.BEGINNER,
            description = "Learn to play Do, Ré, and Mi!"
        )
        viewModel.initializeLevel(levelNumber, lesson)
    }
    
    // Batman floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "batman_float")
    val batmanFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "batman_float"
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.bg_level1),
            contentDescription = "Level Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Semi-transparent overlay for better UI visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with back button, score, and stars
            AppPianoHeader(
                levelNumber = levelNumber,
                score = pianoState.score,
                stars = pianoState.stars,
                onBackClick = {
                    SoundManager.playClick()
                    onNavigateBack()
                }
            )
            
            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Batman character (floating)
                Image(
                    painter = painterResource(id = R.drawable.level_1),
                    contentDescription = "Batman",
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.Center)
                        .offset(y = batmanFloat.dp),
                    contentScale = ContentScale.Fit
                )
                
                // Lesson progress indicator
                if (pianoState.currentLesson != null) {
                    LessonProgressCard(
                        currentNote = pianoState.currentNoteIndex + 1,
                        totalNotes = pianoState.currentLesson!!.sequence.size,
                        nextNote = pianoState.currentLesson!!.sequence.getOrNull(pianoState.currentNoteIndex),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 24.dp)
                    )
                }
                
                // Completion dialog
                if (pianoState.isLessonComplete) {
                    LessonCompleteDialog(
                        stars = pianoState.stars,
                        score = pianoState.score,
                        onContinue = {
                            onLessonComplete(pianoState.stars)
                            onNavigateBack()
                        },
                        onRetry = {
                            viewModel.resetSession()
                        }
                    )
                }
            }
            
            // Piano keyboard at bottom
            PianoKeyboard(
                config = pianoState.config,
                onKeyPressed = { key ->
                    viewModel.onKeyPressed(key)
                    PianoSoundManager.playNote(key.note)
                },
                onKeyReleased = { key ->
                    viewModel.onKeyReleased(key)
                },
                pressedKeys = pianoState.pressedKeys,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Header component with back button, level info, and score
 */
@Composable
private fun AppPianoHeader(
    levelNumber: Int,
    score: Int,
    stars: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Level indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Level $levelNumber",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // Stars
            repeat(3) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < stars) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // Score
        Text(
            text = "$score",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.3f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Shows current lesson progress and next note to play
 */
@Composable
private fun LessonProgressCard(
    currentNote: Int,
    totalNotes: Int,
    nextNote: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Progress: $currentNote / $totalNotes",
                fontSize = 14.sp,
                color = Color.Black
            )
            
            if (nextNote != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Play: $nextNote",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = RainbowOrange
                )
            }
        }
    }
}

/**
 * Dialog shown when lesson is completed
 */
@Composable
private fun LessonCompleteDialog(
    stars: Int,
    score: Int,
    onContinue: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .width(300.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 Lesson Complete!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stars display
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < stars) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Score: $score",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RainbowOrange
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retry")
                    }
                    
                    Button(
                        onClick = onContinue,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RainbowGreen
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}
