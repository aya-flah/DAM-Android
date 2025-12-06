package com.pianokids.game.view.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Audio Preview Screen Component
 * Beautiful animated UI for song preview before gameplay
 * Theme-aware design with light, playful aesthetic
 */
@Composable
fun AudioPreviewScreen(
    levelTitle: String,
    theme: String,
    isPlaying: Boolean,
    isLoading: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = getThemeColors(theme)
    var isVisible by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    val playButtonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "playButtonScale"
    )

    val outerShape = RoundedCornerShape(32.dp)
    val innerShape = RoundedCornerShape(26.dp)

    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(500))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            themeColors.background1,
                            themeColors.background2
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            // Glow background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                themeColors.accent.copy(alpha = glowAlpha * 0.2f),
                                Color.Transparent,
                                themeColors.accent.copy(alpha = glowAlpha * 0.15f)
                            )
                        )
                    )
            )

            // Card
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .wrapContentHeight()
                        .padding(vertical = 16.dp),
                    shape = outerShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    border = BorderStroke(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF667EEA),
                                Color(0xFF00D9FF),
                                Color(0xFFFFC857)
                            )
                        )
                    )
                ) {

                    Box(
                        modifier = Modifier.clip(innerShape)
                    ) {
                        // Frosted glass background
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.28f),
                                            Color.White.copy(alpha = 0.16f)
                                        )
                                    )
                                )
                                .blur(22.dp)
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.30f),
                                    innerShape
                                )
                        )

                        // CONTENT
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth()
                        ) {

                            // Header
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = themeColors.emoji, fontSize = 48.sp)
                                Text(
                                    text = levelTitle,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1A1A1A),
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 1.sp
                                )
                            }

                            Divider(
                                color = Color.Black.copy(alpha = 0.10f),
                                thickness = 1.dp
                            )

                            // WAVEFORM BOX — now opaque
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.60f))
                                    .border(
                                        1.dp,
                                        Color.Black.copy(alpha = 0.20f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = themeColors.accent,
                                            modifier = Modifier.size(60.dp),
                                            strokeWidth = 5.dp
                                        )
                                        Text(
                                            text = "Loading preview...",
                                            color = Color(0xFF1A1A1A),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else {
                                    AnimatedWaveform(
                                        isPlaying = isPlaying,
                                        themeColor = themeColors.accent,
                                        infiniteTransition = infiniteTransition
                                    )
                                }
                            }

                            // INSTRUCTIONS — now opaque & darker text
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.55f))
                                    .border(
                                        1.dp,
                                        Color.Black.copy(alpha = 0.18f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = if (isPlaying) "🎵" else "🎹", fontSize = 24.sp)
                                    Text(
                                        text = if (isPlaying) "Listen to the melody..." else "Press play to hear the song",
                                        fontSize = 16.sp,
                                        color = Color(0xFF1A1A1A),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Progress bar
                            if (!isLoading) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        color = themeColors.accent,
                                        trackColor = Color.Black.copy(alpha = 0.12f)
                                    )

                                    Text(
                                        text = "${(progress * 100).toInt()}% Complete",
                                        fontSize = 14.sp,
                                        color = Color(0xFF1A1A1A),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {

                                Button(
                                    onClick = onPlayPause,
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .scale(playButtonScale),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = themeColors.accent,
                                        contentColor = Color.White,
                                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Text(
                                        text = if (isPlaying) "⏸" else "▶",
                                        fontSize = 40.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = onSkip,
                                    modifier = Modifier.height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(
                                        width = 2.dp,
                                        color = themeColors.accent
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = themeColors.accent
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⏭", fontSize = 24.sp)
                                        Text("SKIP", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }

                            // Bottom hint — now opaque & dark text
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.55f))
                                    .border(
                                        1.dp,
                                        Color.Black.copy(alpha = 0.18f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("✨", fontSize = 18.sp)
                                    Text(
                                        "Get ready to play after the preview!",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
    val barCount = 15

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        repeat(barCount) { index ->
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = 15f,
                targetValue = 85f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 500 + index * 40,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (isPlaying) animatedHeight.dp else 15.dp)
                    .background(
                        brush = if (isPlaying) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    themeColor,
                                    themeColor.copy(alpha = 0.6f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    themeColor.copy(alpha = 0.3f),
                                    themeColor.copy(alpha = 0.2f)
                                )
                            )
                        },
                        shape = RoundedCornerShape(6.dp)
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
    val accent: Color,
    val accentSecondary: Color,
    val cardBackground1: Color,
    val cardBackground2: Color,
    val emoji: String
)

fun getThemeColors(theme: String): ThemeColors {
    return when (theme.lowercase()) {

        // -----------------------------------
        // 🦇 BATMAN
        // -----------------------------------
        "Batman" -> ThemeColors(
            background1 = Color(0xFF1A1A2E),
            background2 = Color(0xFF16213E),
            accent = Color(0xFFFFD700),       // yellow
            accentSecondary = Color(0xFFB8860B),
            cardBackground1 = Color(0xFF2D2D44),
            cardBackground2 = Color(0xFF1F1F38),
            emoji = "🦇"
        )

        // -----------------------------------
        // 🕷️ SPIDER-MAN
        // -----------------------------------
        "Spider-Man" -> ThemeColors(
            background1 = Color(0xFF8B0000),
            background2 = Color(0xFFB30000),
            accent = Color(0xFF1E90FF),       // spider blue
            accentSecondary = Color(0xFF4682B4),
            cardBackground1 = Color(0xFFFFE6E6),
            cardBackground2 = Color(0xFFFFF5F5),
            emoji = "🕷️"
        )

        // -----------------------------------
        // 🔍 DETECTIVE CONAN
        // -----------------------------------
        "Detective Conan" -> ThemeColors(
            background1 = Color(0xFFE3E9F7),        // soft investigation blue
            background2 = Color(0xFFD4DDF3),        // lighter atmospheric blue
            accent = Color(0xFF1E4BA3),             // Conan deep blue (jacket)
            accentSecondary = Color(0xFFC62828),    // Conan red bow tie
            cardBackground1 = Color(0xFFF5F7FF),
            cardBackground2 = Color(0xFFE6ECFA),
            emoji = "🔍"
        )

        // -----------------------------------
        // 🐾 BLACK PANTHER
        // -----------------------------------
        "Black Panther" -> ThemeColors(
            background1 = Color(0xFFEDE6F7),        // soft Wakandan purple haze
            background2 = Color(0xFFDCD0F0),        // lighter purple tone
            accent = Color(0xFF5528FF),             // royal Wakanda purple
            accentSecondary = Color(0xFF0A0A0A),    // Black Panther suit black
            cardBackground1 = Color(0xFFF8F3FF),
            cardBackground2 = Color(0xFFEDE3FF),
            emoji = "🐾"
        )

        // -----------------------------------
        // 🛡️ MARVEL TRIO (Iron Man + Cap + Flash)
        // -----------------------------------
        "marvel", "iron man", "captain america", "flash",
        "Avengers Mix",
        "mcu trio" -> ThemeColors(
            background1 = Color(0xFFB71C1C),  // Iron Man red
            background2 = Color(0xFF880E4F),  // Flash-magenta vibe
            accent = Color(0xFF0D47A1),       // Captain America blue
            accentSecondary = Color(0xFFFFD700), // Gold highlight
            cardBackground1 = Color(0xFFFFEBEE),
            cardBackground2 = Color(0xFFFCE4EC),
            emoji = "🛡️"
        )

        // -----------------------------------
        // 🟢 HUNTER X HUNTER
        // -----------------------------------
        "Hunter x Hunter" -> ThemeColors(
            background1 = Color(0xFFE0F8E0),
            background2 = Color(0xFFC8F4C8),
            accent = Color(0xFF2ECC71),       // Gon green
            accentSecondary = Color(0xFF27AE60),
            cardBackground1 = Color(0xFFF0FFF4),
            cardBackground2 = Color(0xFFE3FFE9),
            emoji = "🎯"
        )

        // -----------------------------------
        // DEFAULT
        // -----------------------------------
        else -> ThemeColors(
            background1 = Color(0xFFF0F0FA),
            background2 = Color(0xFFF8F9FA),
            accent = Color(0xFF667EEA),
            accentSecondary = Color(0xFF00D9FF),
            cardBackground1 = Color(0xFFF8F9FA),
            cardBackground2 = Color(0xFFE8EAF6),
            emoji = "🎵"
        )
    }
}