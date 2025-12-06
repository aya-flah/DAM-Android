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
import androidx.compose.ui.draw.blur
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

    val outerShape = RoundedCornerShape(28.dp)
    val innerShape = RoundedCornerShape(24.dp)

    // =============================
    //       ENTRANCE ANIMATION
    // =============================
    LaunchedEffect(Unit) { isVisible = true }

    // =============================
    //          TYPEWRITER
    // =============================
    LaunchedEffect(storyText, skipRequested) {
        if (skipRequested) {
            typedText = storyText
            showChooseButton = true
            return@LaunchedEffect
        }

        delay(250)
        typedText = ""
        showChooseButton = false

        for (char in storyText) {
            typedText += char
            SoundManager.playTyping()
            delay(30)
        }
        showChooseButton = true
    }

    // =============================
    //     BACKGROUND OVERLAY
    // =============================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {

        // =============================
        //     GLASSY INTRO CARD
        // =============================
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = outerShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            border = BorderStroke(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF667EEA),  // purple–blue
                        Color(0xFF00D9FF),  // aqua
                        Color(0xFFFFC857)   // gold
                    )
                )
            )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(innerShape)
            ) {

                // =============================
                //     Frosted Glass Layer
                // =============================
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.18f),
                                    Color.White.copy(alpha = 0.10f)
                                )
                            )
                        )
                        .blur(22.dp)
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.25f),
                            innerShape
                        )
                )

                // =============================
                //     CONTENT (UNCHANGED)
                // =============================
                Column(
                    modifier = Modifier
                        .padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // -----------------------------------
                    // HEADER
                    // -----------------------------------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 Mission Briefing",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        TextButton(onClick = { skipRequested = true }) {
                            Text(
                                text = "Skip ⏭",
                                color = Color(0xFF00D9FF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.15f),
                        thickness = 1.dp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {

                        // -----------------------------------
                        // HERO IMAGE
                        // -----------------------------------
                        Image(
                            painter = painterResource(heroImageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .width(200.dp)
                                .height(260.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .border(
                                    3.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF667EEA),
                                            Color(0xFF00D9FF),
                                            Color(0xFFFFC857)
                                        )
                                    ),
                                    RoundedCornerShape(22.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )

                        // -----------------------------------
                        // STORY TEXT (Glass Panel)
                        // -----------------------------------
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(260.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Color.Black.copy(alpha = 0.25f)
                                )
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.25f),
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = typedText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    // -----------------------------------
                    // START BUTTON
                    // -----------------------------------
                    AnimatedVisibility(showChooseButton) {
                        Button(
                            onClick = onFinished,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6CCF7A)
                            )
                        ) {
                            Text(
                                "Listen To Preview",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // -----------------------------------
                    // TIP BOX
                    // -----------------------------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Listen carefully and play the correct notes!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}