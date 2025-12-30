package com.pianokids.game.view.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.utils.SoundManager
import kotlinx.coroutines.delay

@Composable
fun MainMenuScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToPractice: () -> Unit,
    onNavigateToMiniGames: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    // Entrance animation
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Floating animation for decorative elements
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

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
                            Color(0xFFFFF9E6),
                            Color(0xFFFFFBF5),
                            Color(0xFFFFF0F8)
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
                                Color(0xFF667EEA).copy(alpha = glowAlpha * 0.1f),
                                Color.Transparent,
                                Color(0xFFFF6B9D).copy(alpha = glowAlpha * 0.08f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // Title with floating animation
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -100 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Floating musical notes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.offset(y = floatOffset.dp)
                        ) {
                            Text("🎵", fontSize = 32.sp)
                            Text("🎹", fontSize = 40.sp)
                            Text("🎵", fontSize = 32.sp)
                        }

                        Text(
                            text = "Choose Your Adventure!",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2C3E50),
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Learn piano with fun & music! ✨",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7B68A9),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))

                // Centered Cards Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Staggered entrance animations
                        AnimatedModeCard(
                            title = "Map & Levels",
                            subtitle = "Explore islands • Earn stars • Beat bosses",
                            emoji = "🗺️",
                            icon = Icons.Default.Map,
                            colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                            delay = 100,
                            isVisible = isVisible,
                            glowAlpha = glowAlpha,
                            onClick = {
                                SoundManager.playClick()
                                onNavigateToMap()
                            }
                        )

                        AnimatedModeCard(
                            title = "Practice Mode",
                            subtitle = "Warm up • Repeat notes • Get better fast",
                            emoji = "🎼",
                            icon = Icons.Default.MusicNote,
                            colors = listOf(Color(0xFF00C9A7), Color(0xFF76C893)),
                            delay = 200,
                            isVisible = isVisible,
                            glowAlpha = glowAlpha,
                            onClick = {
                                SoundManager.playClick()
                                onNavigateToPractice()
                            }
                        )

                        AnimatedModeCard(
                            title = "Mini Games",
                            subtitle = "Quick fun games to learn notes",
                            emoji = "🎮",
                            icon = Icons.Default.SportsEsports,
                            colors = listOf(Color(0xFFFF6B9D), Color(0xFFE91E63)),
                            delay = 300,
                            isVisible = isVisible,
                            glowAlpha = glowAlpha,
                            onClick = {
                                SoundManager.playClick()
                                onNavigateToMiniGames()
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AnimatedModeCard(
    title: String,
    subtitle: String,
    emoji: String,
    icon: ImageVector,
    colors: List<Color>,
    delay: Int,
    isVisible: Boolean,
    glowAlpha: Float,
    onClick: () -> Unit
) {
    var cardVisible by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(delay.toLong())
            cardVisible = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    // Icon rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "iconRotate")
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    AnimatedVisibility(
        visible = cardVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .scale(scale)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colors[0].copy(alpha = glowAlpha),
                            colors[1].copy(alpha = glowAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .clip(RoundedCornerShape(28.dp))
                .clickable {
                    onClick()
                },
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.White,
                                colors[0].copy(alpha = 0.08f),
                                colors[1].copy(alpha = 0.12f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Large Emoji + Icon Display
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        // Background glow circle
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            colors[0].copy(alpha = 0.3f),
                                            colors[1].copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        // Icon circle
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = colors
                                    ),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 3.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .rotate(iconRotation),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 36.sp
                                )
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Text Content
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2C3E50),
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF546E7A),
                            lineHeight = 20.sp
                        )

                        // Stars decoration
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(3) {
                                Text(
                                    text = "⭐",
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // Arrow indicator
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colors[0].copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "→",
                            fontSize = 28.sp,
                            color = colors[0],
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}