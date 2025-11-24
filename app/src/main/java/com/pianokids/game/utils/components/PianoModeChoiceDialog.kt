package com.pianokids.game.utils.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.R
import com.pianokids.game.utils.SoundManager

enum class PianoMode {
    APP_PIANO,      // Virtual keyboard
    REAL_PIANO      // Microphone listening
}

@Composable
fun PianoModeChoiceDialog(
    heroImageRes: Int,
    storyText: String,
    onModeSelected: (PianoMode) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

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
                            Color(0xFF87CEEB).copy(alpha = 0.5f),
                            Color(0xFF64B5F6).copy(alpha = 0.6f),
                            Color(0xFF2C3E50).copy(alpha = 0.7f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "MISSION BRIEFING",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B9D),
                            letterSpacing = 2.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Hero and Story Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hero Image
                        Card(
                            modifier = Modifier
                                .size(120.dp)
                                .border(
                                    width = 3.dp,
                                    color = Color(0xFF00D9FF),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = heroImageRes),
                                contentDescription = "Hero",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Story Text
                        Text(
                            text = storyText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF2C3E50),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color(0xFFE0E0E0)
                    )

                    // Question
                    Text(
                        text = "Which piano would like to use for this mission?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Mode Selection Buttons + Start Button Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App Piano Button
                        var selectedMode by remember { mutableStateOf<PianoMode?>(null) }
                        
                        OutlinedButton(
                            onClick = {
                                SoundManager.playClick()
                                selectedMode = PianoMode.APP_PIANO
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
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
                            border = BorderStroke(
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
                                containerColor = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF64B5F6),
                                        Color(0xFF81C784)
                                    )
                                ).let { Color(0xFF64B5F6) },
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

                    // Hint
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFFFF9C4),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Listen carefully play the correct notes to win!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF795548),
                                fontSize = 12.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
