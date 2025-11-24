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
    onModeSelected: (PianoMode) -> Unit
) {
    var typedText by remember { mutableStateOf("") }
    var showButtons by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf<PianoMode?>(null) }
    var skipRequested by remember { mutableStateOf(false) }

    // Entrance animation trigger
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // TYPEWRITER EFFECT
    LaunchedEffect(storyText, skipRequested) {
        if (skipRequested) {
            typedText = storyText
            showButtons = true
            return@LaunchedEffect
        }

        delay(300)
        typedText = ""
        showButtons = false

        for (char in storyText) {
            typedText += char
            SoundManager.playTyping()
            delay(35)
        }
        showButtons = true
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

    // Button hover effect simulation
    val appPianoScale by animateFloatAsState(
        targetValue = if (selectedMode == PianoMode.APP_PIANO) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val realPianoScale by animateFloatAsState(
        targetValue = if (selectedMode == PianoMode.REAL_PIANO) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
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

                        // ENHANCED PIANO MODE SELECTION
                        AnimatedVisibility(
                            visible = showButtons,
                            enter = fadeIn(animationSpec = tween(400)) +
                                    expandVertically(animationSpec = tween(400))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Section header
                                Text(
                                    text = "Choose Your Instrument",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C3E50),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                // Mode selection buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // APP PIANO CARD
                                    Card(
                                        onClick = {
                                            SoundManager.playClick()
                                            selectedMode = PianoMode.APP_PIANO
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(140.dp)
                                            .scale(appPianoScale),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedMode == PianoMode.APP_PIANO)
                                                Color(0xFF667EEA)
                                            else
                                                Color(0xFFFFFFFF)
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 0.dp
                                        ),
                                        border = BorderStroke(
                                            width = if (selectedMode == PianoMode.APP_PIANO) 3.dp else 2.dp,
                                            color = if (selectedMode == PianoMode.APP_PIANO)
                                                Color(0xFF667EEA)
                                            else
                                                Color(0xFF667EEA).copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    if (selectedMode == PianoMode.APP_PIANO)
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color(0xFF667EEA),
                                                                Color(0xFF764BA2)
                                                            )
                                                        )
                                                    else
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color(0xFFFFFFFF),
                                                                Color(0xFFF5F7FA)
                                                            )
                                                        )
                                                )
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "🎹",
                                                    fontSize = 48.sp
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "App Piano",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selectedMode == PianoMode.APP_PIANO)
                                                        Color.White
                                                    else
                                                        Color(0xFF667EEA)
                                                )
                                                Text(
                                                    text = "Virtual keys",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (selectedMode == PianoMode.APP_PIANO)
                                                        Color.White.copy(alpha = 0.9f)
                                                    else
                                                        Color(0xFF667EEA).copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }

                                    // REAL PIANO CARD
                                    Card(
                                        onClick = {
                                            SoundManager.playClick()
                                            selectedMode = PianoMode.REAL_PIANO
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(140.dp)
                                            .scale(realPianoScale),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedMode == PianoMode.REAL_PIANO)
                                                Color(0xFF00D9FF)
                                            else
                                                Color(0xFFFFFFFF)
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 0.dp
                                        ),
                                        border = BorderStroke(
                                            width = if (selectedMode == PianoMode.REAL_PIANO) 3.dp else 2.dp,
                                            color = if (selectedMode == PianoMode.REAL_PIANO)
                                                Color(0xFF00D9FF)
                                            else
                                                Color(0xFF00D9FF).copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    if (selectedMode == PianoMode.REAL_PIANO)
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color(0xFF00D9FF),
                                                                Color(0xFF0099CC)
                                                            )
                                                        )
                                                    else
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color(0xFFFFFFFF),
                                                                Color(0xFFF5F7FA)
                                                            )
                                                        )
                                                )
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "🎼",
                                                    fontSize = 48.sp
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "My Piano",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selectedMode == PianoMode.REAL_PIANO)
                                                        Color.White
                                                    else
                                                        Color(0xFF00D9FF)
                                                )
                                                Text(
                                                    text = "Physical keyboard",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (selectedMode == PianoMode.REAL_PIANO)
                                                        Color.White.copy(alpha = 0.9f)
                                                    else
                                                        Color(0xFF00D9FF).copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }
                                }

                                // START BUTTON
                                Button(
                                    onClick = {
                                        SoundManager.playClick()
                                        selectedMode?.let { onModeSelected(it) }
                                    },
                                    enabled = selectedMode != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50),
                                        disabledContainerColor = Color(0xFFBDBDBD)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 0.dp,
                                        disabledElevation = 0.dp
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "START MISSION",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            letterSpacing = 1.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "🚀",
                                            fontSize = 24.sp
                                        )
                                    }
                                }
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