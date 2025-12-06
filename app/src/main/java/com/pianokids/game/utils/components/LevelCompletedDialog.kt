package com.pianokids.game.utils.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.R

@Composable
fun LevelCompletedDialog(
    stars: Int,
    theme: String = "Default",
    onDismiss: () -> Unit,
    onNextLevel: () -> Unit = {}
) {

    // -----------------------
    // THEME STRINGS & COLORS
    // -----------------------
    val (title, subtitle, emoji) = when (theme) {
        "Batman" -> Triple("GOTHAM IS SAVED!", "The Dark Knight approves! 🦇", "🦇")
        "Spider-Man", "spiderman" -> Triple("WEB-TASTIC!", "Your friendly neighbor is proud! 🕷️", "🕷️")
        "Detective Conan", "conan" -> Triple("CASE CLOSED!", "Conan solved the mystery thanks to you! 🔎", "🔎")
        "Black Panther", "wakanda" -> Triple("WAKANDA FOREVER!", "Your rhythm protected the kingdom! 🐾", "🐾")
        "avengers", "Avengers Mix", "marvel mix" -> Triple("HEROIC VICTORY!", "Earth’s Mightiest Heroes salute you! ⚡", "⚡")
        "Hunter x Hunter", "hxh" -> Triple("HUNTER RANK UP!", "Your Nen is growing stronger! 🟢", "🟢")
        else -> Triple("Level Complete!", "Amazing job! 🎉", "🎉")
    }

    val backgroundColor = when (theme) {
        "Batman" -> Color(0xFF1A1A1A)
        "Spider-Man", "spiderman" -> Color(0xFFE53935)
        "Detective Conan", "conan" -> Color(0xFF1E4BA3)
        "Black Panther", "wakanda" -> Color(0xFF5528FF)
        "avengers", "Avengers Mix", "marvel mix" -> Color(0xFF673AB7)
        "Hunter x Hunter", "hxh" -> Color(0xFF1B5E20)
        else -> Color.White
    }

    val textColor = when (theme.lowercase()) {
        "batman", "spider-man", "spiderman", "detective conan", "conan",
        "black panther", "wakanda", "avengers", "avengers mix", "marvel mix",
        "hunter x hunter", "hxh" -> Color.White
        else -> Color.Black
    }

    val outerShape = RoundedCornerShape(28.dp)
    val innerShape = RoundedCornerShape(24.dp)

    // -----------------------
    // OVERLAY
    // -----------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {

        ConfettiExplosion(
            modifier = Modifier.fillMaxSize()
        )

        // -----------------------
        // GLASSY CARD CONTAINER
        // -----------------------
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
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
                        Color(0xFF667EEA),  // purple-blue
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

                // -----------------------
                // FROSTED BACKGROUND LAYER
                // -----------------------
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    backgroundColor.copy(alpha = 0.22f),
                                    backgroundColor.copy(alpha = 0.10f)
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

                // -----------------------
                // FOREGROUND CONTENT
                // -----------------------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // Emoji
                    Text(
                        text = emoji,
                        fontSize = 72.sp
                    )

                    // Title
                    Text(
                        text = title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (theme == "Batman") Color(0xFFFFD700) else textColor
                    )

                    // Subtitle
                    Text(
                        text = subtitle,
                        fontSize = 18.sp,
                        color = textColor.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )

                    // Stars
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { i ->
                            val starRes = if (i < stars) {
                                R.drawable.star_filled
                            } else {
                                R.drawable.star_empty
                            }

                            Image(
                                painter = painterResource(id = starRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                            )
                        }
                    }

                    // Buttons
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (theme == "Batman") Color(0xFFFFD700)
                                else Color(0xFF4CAF50)
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
}
