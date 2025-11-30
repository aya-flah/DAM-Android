package com.pianokids.game.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * Audio Preview Screen Component
 * Beautiful animated UI for song preview before gameplay
 * Theme-aware design (Batman, Frozen, etc.)
 */
@Composable
fun AudioPreviewScreen(
    levelTitle: String,
    theme: String,
    isPlaying: Boolean,
    isLoading: Boolean,
    progress: Float, // 0.0 to 1.0
    onPlayPause: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Theme colors
    val themeColors = getThemeColors(theme)
    
    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        themeColors.background1,
                        themeColors.background2
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Title
            Text(
                text = "🦇 $levelTitle",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.accent,
                textAlign = TextAlign.Center
            )
            
            // Animated Waveform/Music Visualizer
            if (isLoading) {
                CircularProgressIndicator(
                    color = themeColors.accent,
                    modifier = Modifier.size(80.dp)
                )
                Text(
                    text = "Loading preview...",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            } else {
                AnimatedWaveform(
                    isPlaying = isPlaying,
                    themeColor = themeColors.accent,
                    infiniteTransition = infiniteTransition
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Instructions
            Text(
                text = if (isPlaying) "🎵 Listen to the melody..." else "🎹 Press play to hear the song",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            // Progress Bar
            if (!isLoading) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(horizontal = 32.dp),
                    color = themeColors.accent,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause Button
                Button(
                    onClick = onPlayPause,
                    enabled = !isLoading,
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.accent,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        fontSize = 32.sp
                    )
                }
                
                // Skip Button
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = themeColors.accent
                    )
                ) {
                    Text(
                        text = "⏭ SKIP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ready message
            Text(
                text = "Get ready to play after the preview!",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Animated waveform bars
 */
@Composable
fun AnimatedWaveform(
    isPlaying: Boolean,
    themeColor: Color,
    infiniteTransition: InfiniteTransition
) {
    val barCount = 12
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 32.dp)
    ) {
        repeat(barCount) { index ->
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = 20f,
                targetValue = 100f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600 + index * 50,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (isPlaying) animatedHeight.dp else 20.dp)
                    .background(
                        themeColor.copy(alpha = if (isPlaying) 0.8f else 0.3f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

/**
 * Theme color configurations
 */
data class ThemeColors(
    val background1: Color,
    val background2: Color,
    val accent: Color
)

fun getThemeColors(theme: String): ThemeColors {
    return when (theme.lowercase()) {
        "batman" -> ThemeColors(
            background1 = Color(0xFF1A1A2E),
            background2 = Color(0xFF16213E),
            accent = Color(0xFFFFD700) // Gold
        )
        "frozen" -> ThemeColors(
            background1 = Color(0xFF1E3A5F),
            background2 = Color(0xFF2E5090),
            accent = Color(0xFF87CEEB) // Sky Blue
        )
        "jungle" -> ThemeColors(
            background1 = Color(0xFF1B4D3E),
            background2 = Color(0xFF2C6E49),
            accent = Color(0xFF76C893) // Green
        )
        else -> ThemeColors(
            background1 = Color(0xFF1A1A2E),
            background2 = Color(0xFF16213E),
            accent = Color(0xFF00D9FF) // Cyan
        )
    }
}
