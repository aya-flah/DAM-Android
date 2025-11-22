package com.pianokids.game.utils.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LevelCompletedDialog(
    stars: Int,
    theme: String = "Default",
    onDismiss: () -> Unit,
    onNextLevel: () -> Unit = {}
) {
    // Batman-themed messages
    val (title, subtitle, emoji) = when (theme) {
        "Batman" -> Triple(
            "GOTHAM IS SAVED!",
            "The Dark Knight approves! 🦇",
            "🦇"
        )
        "Spider-Man" -> Triple(
            "WEB-TASTIC!",
            "Your friendly neighbor is proud! 🕷️",
            "🕷️"
        )
        else -> Triple(
            "Level Complete!",
            "Amazing job! 🎉",
            "🎉"
        )
    }

    val backgroundColor = when (theme) {
        "Batman" -> Color(0xFF1A1A1A)
        "Spider-Man" -> Color(0xFFE53935)
        else -> Color.White
    }

    val textColor = when (theme) {
        "Batman", "Spider-Man" -> Color.White
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {

        // Confetti behind dialog
        ConfettiExplosion(
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Large emoji
                Text(
                    text = emoji,
                    fontSize = 72.sp
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = if (theme == "Batman") Color(0xFFFFD700) else textColor
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    fontSize = 18.sp,
                    color = textColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(24.dp))

                // STAR EARNED VIEW
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(3) { i ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i < stars) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Next Level Button
                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (theme == "Batman") Color(0xFFFFD700) else Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Next Level ➜",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (theme == "Batman") Color.Black else Color.White
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Exit Button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Return to Map",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
