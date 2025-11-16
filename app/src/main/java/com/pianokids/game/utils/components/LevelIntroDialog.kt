package com.pianokids.game.utils.components

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.pianokids.game.R
import com.pianokids.game.utils.SoundManager

@Composable
fun LevelIntroDialog(
    heroImageRes: Int,
    storyText: String,
    onFinished: () -> Unit
) {
    var typedText by remember { mutableStateOf("") }
    var showButton by remember { mutableStateOf(false) }

    // TYPEWRITER EFFECT ----------------------------------------
    LaunchedEffect(Unit) {
        typedText = ""
        for (char in storyText) {
            typedText += char
            SoundManager.playTyping()       // 🔊 Now works per character (SoundPool)
            delay(35)                       // typing speed
        }
        showButton = true
    }

    // BACKDROP --------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {

        // CARD --------------------------------------------------
        Card(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(26.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)  // translucent glass
            ),
            elevation = CardDefaults.cardElevation(0.dp)          // remove default shadow
        ) {

            Row(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // HERO IMAGE (left) ------------------------------
                Image(
                    painter = painterResource(id = heroImageRes),
                    contentDescription = "Hero",
                    modifier = Modifier
                        .width(200.dp)
                        .height(300.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Crop
                )

                // STORY TEXT (right) -----------------------------
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = typedText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AnimatedVisibility(
                        visible = showButton,
                        enter = fadeIn() + slideInVertically(),
                    ) {
                        Button(
                            onClick = onFinished,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00BCD4)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Level", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
