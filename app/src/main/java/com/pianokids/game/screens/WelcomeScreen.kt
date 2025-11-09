package com.pianokids.game.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.R
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.components.AnimatedOceanWithIslands
import com.pianokids.game.utils.components.RainbowButton
import com.pianokids.game.utils.components.SettingsDialog
import com.pianokids.game.utils.SoundManager
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(true) }
    var musicEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var showButtons by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val inf = rememberInfiniteTransition(label = "welcome")

    // Logo pulse animation
    val logoScale by inf.animateFloat(
        1f, 1.1f,
        infiniteRepeatable(tween(1000, easing = EaseInOutCubic), RepeatMode.Reverse),
        "logoScale"
    )
    val logoAlpha by inf.animateFloat(
        0.8f, 1f,
        infiniteRepeatable(tween(1500, easing = EaseInOutCubic), RepeatMode.Reverse),
        "logoAlpha"
    )

    LaunchedEffect(Unit) { delay(1500); showButtons = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyBlue, OceanLight, OceanDeep)))
    ) {
        AnimatedOceanWithIslands()

        // ⚙️ Settings button
        if (showButtons) {
            IconButton(
                onClick = { showSettings = true; SoundManager.playClick() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 200.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp) // prevents taking too much space
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painterResource(R.drawable.cat_logo),
                    "Logo",
                    modifier = Modifier
                        .size(280.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    "Piano Kids",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.W600,
                        color = Color.White,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            Color.Black.copy(0.3f),
                            Offset(4f, 4f),
                            8f
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    "Learn Piano with Us!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = RainbowYellow,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(64.dp))

                if (showButtons) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        RainbowButton(
                            "Start as Guest",
                            onNavigateToHome,
                            listOf(RainbowOrange, RainbowYellow)
                        )
                        RainbowButton(
                            "Login",
                            onNavigateToAuth,
                            listOf(RainbowBlue, RainbowIndigo)
                        )
                    }
                }
            }
        }

        // ⚙️ Settings Dialog
        if (showSettings) {
            SettingsDialog(
                onDismiss = { showSettings = false },
                soundEnabled = soundEnabled,
                musicEnabled = musicEnabled,
                vibrationEnabled = vibrationEnabled,
                onSoundToggle = {
                    soundEnabled = it
                    if (it) SoundManager.enableSound() else SoundManager.disableSound()
                },
                onMusicToggle = {
                    musicEnabled = it
                    if (it) SoundManager.enableMusic() else SoundManager.disableMusic()
                },
                onVibrationToggle = {
                    vibrationEnabled = it
                    if (it) SoundManager.enableVibration() else SoundManager.disableVibration()
                }
            )
        }
    }

    LaunchedEffect(Unit) { SoundManager.startBackgroundMusic() }
}
