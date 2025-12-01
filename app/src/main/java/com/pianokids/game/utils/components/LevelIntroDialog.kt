package com.pianokids.game.utils.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.pianokids.game.R
import com.pianokids.game.ui.theme.PianoKidsGameTheme
import com.pianokids.game.utils.SoundManager

@Composable
fun LevelIntroDialog(
    heroImageRes: Int,
    storyText: String,
    onFinished: () -> Unit
) {
    var typedText by remember { mutableStateOf("") }
    var showChooseButton by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var skipRequested by remember { mutableStateOf(false) }

    // Entrance animation trigger
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // TYPEWRITER EFFECT
    LaunchedEffect(storyText, skipRequested) {
        if (skipRequested) {
            typedText = storyText
            showChooseButton = true
            return@LaunchedEffect
        }

        delay(300)
        typedText = ""
        showChooseButton = false

        for (char in storyText) {
            typedText += char
            SoundManager.playTyping()
            delay(35)
        }
        showChooseButton = true
    }

    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // BACKDROP WITH GRADIENT
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A237E).copy(alpha = 0.6f),
                            Color(0xFF283593).copy(alpha = 0.7f),
                            Color(0xFF1A1A2E).copy(alpha = 0.8f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            // Animated background particles
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF667EEA).copy(alpha = glowAlpha * 0.15f),
                                Color.Transparent,
                                Color(0xFF00D9FF).copy(alpha = glowAlpha * 0.15f)
                            )
                        )
                    )
            )

            // MAIN CARD
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .wrapContentHeight()
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF667EEA).copy(alpha = glowAlpha),
                                    Color(0xFF00D9FF).copy(alpha = glowAlpha),
                                    Color(0xFF64B5F6).copy(alpha = glowAlpha)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .clip(RoundedCornerShape(32.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFAFAFA)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {

                        // HEADER WITH SKIP BUTTON
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = "🎯",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MISSION BRIEFING",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2C3E50),
                                    letterSpacing = 2.sp
                                )
                            }

                            // Skip button
                            TextButton(
                                onClick = { skipRequested = true },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFF667EEA)
                                )
                            ) {
                                Text(
                                    text = "Skip ⏭",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Divider(
                            color = Color(0xFF667EEA).copy(alpha = 0.2f),
                            thickness = 2.dp
                        )

                        // CONTENT AREA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.Top
                        ) {

                            // HERO IMAGE WITH ENHANCED GLOW
                            Box(
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(300.dp)
                            ) {
                                // Multi-layer glow effect
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .offset(y = 8.dp)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF667EEA).copy(alpha = glowAlpha * 0.6f),
                                                    Color(0xFF00D9FF).copy(alpha = glowAlpha * 0.4f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                )

                                // Main image
                                Image(
                                    painter = painterResource(id = heroImageRes),
                                    contentDescription = "Hero",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = 4.dp,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF667EEA),
                                                    Color(0xFF00D9FF)
                                                )
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .clip(RoundedCornerShape(24.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // STORY TEXT SECTION
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(300.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFF8F9FA),
                                                Color(0xFFE8EAF6).copy(alpha = 0.5f)
                                            )
                                        ),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF667EEA).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = typedText,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2C3E50),
                                    lineHeight = 24.sp,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }

                        // START BUTTON
                        AnimatedVisibility(visible = showChooseButton) {
                            Button(
                                onClick = onFinished,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text(
                                    "CHOOSE SUBLEVEL",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        // BOTTOM HINT
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFF9C4).copy(alpha = 0.8f),
                                            Color(0xFFFFE082).copy(alpha = 0.8f)
                                        )
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFFFD54F).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Listen carefully and play the correct notes to win!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF57C00),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}