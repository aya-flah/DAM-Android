package com.pianokids.game.utils.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun KidFriendlyErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit,
    onTryAgain: () -> Unit
) {
    // Extract kid-friendly message
    val friendlyMessage = when {
        errorMessage.contains("not appropriate", ignoreCase = true) ||
        errorMessage.contains("Prompt is not appropriate", ignoreCase = true) -> {
            // Extract the reason from the error message
            val reason = errorMessage.substringAfter(":", "").trim()
            if (reason.isNotBlank() && reason.length < 200) {
                reason
            } else {
                "Oops! Let's try a different idea. Remember to keep it fun and friendly! 🎨"
            }
        }
        errorMessage.contains("violence", ignoreCase = true) ||
        errorMessage.contains("violent", ignoreCase = true) -> {
            "Oops! Let's make something happy and fun instead! How about a friendly cartoon character? 🌈"
        }
        errorMessage.contains("scary", ignoreCase = true) ||
        errorMessage.contains("horror", ignoreCase = true) -> {
            "That might be too spooky! Let's create something cheerful and cute! 🌟"
        }
        errorMessage.contains("copyright", ignoreCase = true) ||
        errorMessage.contains("copyrighted", ignoreCase = true) -> {
            "Let's create an original character! You can describe what they look like instead of using character names. Try: 'a ninja in orange clothes' or 'a princess with blue dress' 👑"
        }
        errorMessage.contains("network", ignoreCase = true) ||
        errorMessage.contains("connection", ignoreCase = true) -> {
            "Oops! Can't reach the internet. Check your Wi-Fi and try again! 📡"
        }
        else -> {
            "Something went wrong, but don't worry! Let's try again with a different idea! 🎨"
        }
    }

    var bounceState by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        bounceState = true
    }

    val bounceAnimation by animateFloatAsState(
        targetValue = if (bounceState) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF9A9E),
                                Color(0xFFFAD0C4)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Animated Icon
                    Box(
                        modifier = Modifier
                            .size(100.dp * bounceAnimation)
                            .shadow(12.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎨",
                            fontSize = (48 * bounceAnimation).sp
                        )
                    }

                    // Title
                    Text(
                        text = "Let's Try Something Else!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    // Kid-Friendly Message Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = friendlyMessage,
                                fontSize = 16.sp,
                                color = Color(0xFF333333),
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Helpful Tips
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💡 Try these ideas:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            TipItem("A superhero with a blue cape 🦸")
                            TipItem("A cute robot friend 🤖")
                            TipItem("A magical unicorn 🦄")
                            TipItem("A funny dinosaur 🦕")
                        }
                    }

                    // Action Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Try Again Button
                        Button(
                            onClick = onTryAgain,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF667EEA)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✨", fontSize = 24.sp)
                                Text(
                                    text = "Try Again!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Cancel Button
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Maybe Later",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}
