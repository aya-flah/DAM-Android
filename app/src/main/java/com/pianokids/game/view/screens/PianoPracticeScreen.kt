package com.pianokids.game.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pianokids.game.R
import com.pianokids.game.data.models.NoteType
import com.pianokids.game.data.models.PianoConfig
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.PianoSoundManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.view.components.PianoKeyboard
import com.pianokids.game.viewmodel.PianoViewModel

/**
 * Piano Practice Screen - Batman Theme (Level 1)
 * This screen is reusable - just change the background and piano theme for other levels
 */
@Composable
fun PianoPracticeScreen(
    onNavigateBack: () -> Unit,
    levelNumber: Int = 1,
    levelTheme: String = "Batman",
    backgroundImage: Int = R.drawable.bg_level1,
    viewModel: PianoViewModel = viewModel()
) {
    val context = LocalContext.current
    val pianoState by viewModel.pianoState.collectAsState()
    
    // Initialize Piano Sound Manager
    LaunchedEffect(Unit) {
        PianoSoundManager.init(context)
        viewModel.initializeLevel(levelNumber)
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = backgroundImage),
            contentDescription = "Level Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Dark overlay for better contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Back Button and Score
            TopBar(
                levelNumber = levelNumber,
                levelTheme = levelTheme,
                score = pianoState.score,
                onBackClick = {
                    SoundManager.playClick()
                    onNavigateBack()
                }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Batman Character floating in center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.level_1),
                    contentDescription = "Batman",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Instructions
            Text(
                text = "Tap the keys to play notes!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Piano Keyboard
            PianoKeyboard(
                config = pianoState.config,
                onKeyPressed = { key ->
                    PianoSoundManager.playNote(key.solfege)
                    viewModel.onKeyPressed(key)
                },
                onKeyReleased = { key ->
                    viewModel.onKeyReleased(key)
                },
                pressedKeys = pianoState.pressedKeys,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun TopBar(
    levelNumber: Int,
    levelTheme: String,
    score: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Level Info
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Level $levelNumber: $levelTheme",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Piano Practice",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Score
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(RainbowYellow, RainbowOrange)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⭐",
                    fontSize = 20.sp
                )
                Text(
                    text = "$score",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
