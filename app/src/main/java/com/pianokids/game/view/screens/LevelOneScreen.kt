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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pianokids.game.R
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.viewmodel.LevelOneViewModel
import com.pianokids.game.viewmodel.PlayMode

@Composable
fun LevelOneScreen(
    onNavigateBack: () -> Unit,
    onStartAppPiano: () -> Unit = {},
    onStartRealPiano: () -> Unit = {},
    viewModel: LevelOneViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Floating animation for Batman
    val infiniteTransition = rememberInfiniteTransition(label = "batman_float")
    val batmanFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "batman_float"
    )
    
    // Scale animation for buttons
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "button_scale"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_level1),
            contentDescription = "Level 1 Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Batman Logo at top
        Image(
            painter = painterResource(id = R.drawable.level_1),
            contentDescription = "Batman Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
                .size(120.dp),
            contentScale = ContentScale.Fit
        )
        
        // Back Button
        IconButton(
            onClick = {
                SoundManager.playClick()
                onNavigateBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Level Title
            LevelTitle()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Batman Character with Speech Bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Speech Bubble
                    SpeechBubble(
                        text = "How will you unleash musical power today, hero?"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Batman Character
                    Image(
                        painter = painterResource(id = R.drawable.level_1),
                        contentDescription = "Batman",
                        modifier = Modifier
                            .size(150.dp)
                            .offset(y = batmanFloat.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Mode Selection Buttons
            if (uiState.showModeSelection) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Play on App Piano Button
                    CircularModeButton(
                        icon = "🎹",
                        title = "Play on App\nPiano",
                        backgroundColor = Color(0xFFFF9800), // Orange
                        scale = buttonScale,
                        onClick = {
                            SoundManager.playClick()
                            viewModel.selectPlayMode(PlayMode.APP_PIANO)
                            onStartAppPiano()
                        }
                    )
                    
                    // Play on My Real Piano Button
                    CircularModeButton(
                        icon = "🎹",
                        title = "Play on My Real\nPiano",
                        backgroundColor = Color(0xFF7986CB), // Blue
                        scale = buttonScale,
                        onClick = {
                            SoundManager.playClick()
                            viewModel.selectPlayMode(PlayMode.REAL_PIANO)
                            onStartRealPiano()
                        }
                    )
                }
            }
        }
        
        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RainbowYellow)
            }
        }
    }
}

@Composable
fun LevelTitle() {
    Card(
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level Number
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2196F3))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Level 1:",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Level Name
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF44336))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Basic Keys",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SpeechBubble(text: String) {
    Card(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = Color.Black
            ),
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
fun CircularModeButton(
    icon: String,
    title: String,
    backgroundColor: Color,
    scale: Float,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // Circular Button
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun GothamCityBackground() {
    // Simple city silhouette effect using boxes
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
        // Left Building
        Box(
            modifier = Modifier
                .width(120.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A237E).copy(alpha = 0.7f),
                            Color(0xFF283593).copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            // Windows
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color(0xFFFFEB3B).copy(alpha = 0.6f))
                            )
                        }
                    }
                }
            }
        }
        
        // Right Building
        Box(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight(0.8f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF311B92).copy(alpha = 0.7f),
                            Color(0xFF4A148C).copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            // Windows
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(Color(0xFFFFEB3B).copy(alpha = 0.6f))
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
