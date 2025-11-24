package com.pianokids.game.utils.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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

    // Entrance animation trigger
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // TYPEWRITER EFFECT
    LaunchedEffect(Unit) {
        delay(300) // Wait for entrance animation
        typedText = ""
        for (char in storyText) {
            typedText += char
            SoundManager.playTyping()
            delay(35)
        }
        showButtons = true
    }

    // Pulsing glow animation for the card
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Shimmer effect for button
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // BACKDROP WITH BALANCED GRADIENT
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
                            Color(0xFF87CEEB).copy(alpha = 0.5f),  // SkyBlue
                            Color(0xFF64B5F6).copy(alpha = 0.6f),  // RainbowBlue
                            Color(0xFF2C3E50).copy(alpha = 0.7f)   // TextDark
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            // Animated particles/stars in background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF64B5F6).copy(alpha = glowAlpha * 0.2f),  // RainbowBlue
                                Color.Transparent,
                                Color(0xFF81C784).copy(alpha = glowAlpha * 0.2f)   // RainbowGreen
                            )
                        )
                    )
            )

            // MAIN CARD WITH SCALE ANIMATION
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
                        .fillMaxWidth(0.88f)
                        .wrapContentHeight()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0xFF64B5F6).copy(alpha = 0.5f)  // RainbowBlue
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF64B5F6).copy(alpha = glowAlpha),  // RainbowBlue
                                    Color(0xFF81C784).copy(alpha = glowAlpha),  // RainbowGreen
                                    Color(0xFFF0628A).copy(alpha = glowAlpha)   // RainbowPink
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .clip(RoundedCornerShape(28.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFFFF).copy(alpha = 0.95f)  // CardBackground with slight transparency
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // TITLE/HEADER
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "MISSION BRIEFING",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.tertiary,  // TextDark
                                letterSpacing = 1.5.sp
                            )
                        }

                        Divider(
                            color = Color(0xFF64B5F6).copy(alpha = 0.3f),  // RainbowBlue
                            thickness = 1.dp
                        )

                        // CONTENT AREA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.Top
                        ) {

                            // HERO IMAGE WITH GLOW
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(280.dp)
                            ) {
                                // Glow effect behind image
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .offset(y = 6.dp)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF64B5F6).copy(alpha = glowAlpha * 0.5f),  // RainbowBlue
                                                    Color(0xFF81C784).copy(alpha = glowAlpha * 0.3f),  // RainbowGreen
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                )

                                // Main image
                                Image(
                                    painter = painterResource(id = heroImageRes),
                                    contentDescription = "Hero",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shadow(12.dp, RoundedCornerShape(20.dp))
                                        .border(
                                            width = 3.dp,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF64B5F6),  // RainbowBlue
                                                    Color(0xFF81C784)   // RainbowGreen
                                                )
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // STORY TEXT SECTION
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(280.dp)
                                    .background(
                                        Color(0xFFF3F9FF).copy(alpha = 0.8f),  // GameBackground
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = typedText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2C3E50),  // TextDark
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }

                        // PIANO MODE CHOICE BUTTONS (below content area)
                        AnimatedVisibility(
                            visible = showButtons,
                            enter = fadeIn() + expandVertically()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // App Piano Button
                                OutlinedButton(
                                    onClick = {
                                        SoundManager.playClick()
                                        selectedMode = PianoMode.APP_PIANO
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (selectedMode == PianoMode.APP_PIANO) 3.dp else 2.dp,
                                        color = if (selectedMode == PianoMode.APP_PIANO) 
                                            Color(0xFF667EEA) 
                                        else 
                                            Color(0xFF667EEA).copy(alpha = 0.5f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedMode == PianoMode.APP_PIANO)
                                            Color(0xFF667EEA).copy(alpha = 0.1f)
                                        else
                                            Color.Transparent,
                                        contentColor = Color(0xFF667EEA)
                                    )
                                ) {
                                    Text(
                                        text = "PLAY WITH APP PIANO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Real Piano Button
                                OutlinedButton(
                                    onClick = {
                                        SoundManager.playClick()
                                        selectedMode = PianoMode.REAL_PIANO
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (selectedMode == PianoMode.REAL_PIANO) 3.dp else 2.dp,
                                        color = if (selectedMode == PianoMode.REAL_PIANO)
                                            Color(0xFF00D9FF)
                                        else
                                            Color(0xFF00D9FF).copy(alpha = 0.5f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedMode == PianoMode.REAL_PIANO)
                                            Color(0xFF00D9FF).copy(alpha = 0.1f)
                                        else
                                            Color.Transparent,
                                        contentColor = Color(0xFF00D9FF)
                                    )
                                ) {
                                    Text(
                                        text = "PLAY WITH MY PIANO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                
                                // START Button
                                Button(
                                    onClick = {
                                        SoundManager.playClick()
                                        selectedMode?.let { onModeSelected(it) }
                                    },
                                    enabled = selectedMode != null,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF64B5F6),
                                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "START",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = " 🚀",
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // BOTTOM DECORATIVE HINT
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFF3F9FF).copy(alpha = 0.8f),  // GameBackground
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Listen carefully and play the correct notes to win!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64B5F6),  // RainbowBlue
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}