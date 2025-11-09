package com.pianokids.game.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import kotlin.math.sin
import kotlin.math.cos

data class GameLevel(
    val number: Int,
    val title: String,
    val world: String,
    val isUnlocked: Boolean,
    val stars: Int,
    val emoji: String,
    val color: Color,
    val position: IslandPosition
)

data class IslandPosition(
    val xOffset: Float,
    val ySpacing: Float
)

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit = { }
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    var showComingSoonDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Get user data - use defaults if not logged in (guest mode)
    val isLoggedIn = userPrefs.isLoggedIn()
    val userName = if (isLoggedIn) userPrefs.getFullName() else "Guest Player"
    val userLevel = if (isLoggedIn) userPrefs.getLevel() else 1
    val totalStars = if (isLoggedIn) userPrefs.getTotalStars() else 0

    val levels = remember {
        listOf(
            GameLevel(1, "Basic Keys", "Pirate Ship", true, 0, "🚢", OceanDeep,
                IslandPosition(0f, 1f)),
            GameLevel(2, "Simple Melody", "Japanese Garden", false, 0, "🏯", RainbowPink,
                IslandPosition(-0.6f, 1.2f)),
            GameLevel(3, "Follow Rhythm", "Anime City", false, 0, "🌸", RainbowViolet,
                IslandPosition(0.5f, 1.3f)),
            GameLevel(4, "Both Hands", "Ninja Village", false, 0, "🥷", RainbowIndigo,
                IslandPosition(-0.4f, 1.2f)),
            GameLevel(5, "Speed Challenge", "Dragon Temple", false, 0, "🐉", RainbowRed,
                IslandPosition(0.6f, 1.3f)),
            GameLevel(6, "Cartoon World", "Superhero City", false, 0, "🦸", RainbowOrange,
                IslandPosition(-0.5f, 1.2f)),
            GameLevel(7, "Hero Training", "Marvel Universe", false, 0, "⚡", RainbowYellow,
                IslandPosition(0.4f, 1.3f)),
            GameLevel(8, "Final Concert", "New York", false, 0, "🗽", RainbowGreen,
                IslandPosition(0f, 1.2f))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF87CEEB),
                        Color(0xFFB0E0E6),
                        Color(0xFF98D8E8),
                        Color(0xFF6CB4E0),
                        Color(0xFF4A9FD8)
                    )
                )
            )
    ) {
        AnimatedIslandBackground()
        LaunchedEffect(Unit) { SoundManager.startBackgroundMusic() }


        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with user info and profile button
            GameHeader(
                userName = userName,
                userLevel = userLevel,
                totalStars = totalStars,
                maxStars = levels.size * 3,
                onProfileClick = onNavigateToProfile
            )

            // Island Map with roadmap path
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // The roadmap path connecting islands
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((levels.size * 350).dp)
                ) {
                    val width = size.width
                    var currentY = 180f

                    for (i in 0 until levels.size - 1) {
                        val currentLevel = levels[i]
                        val nextLevel = levels[i + 1]

                        val startX = width / 2 + (currentLevel.position.xOffset * width * 0.3f)
                        val startY = currentY

                        val nextY = startY + (350 * nextLevel.position.ySpacing)
                        val endX = width / 2 + (nextLevel.position.xOffset * width * 0.3f)

                        val path = Path().apply {
                            moveTo(startX, startY)
                            val midY = (startY + nextY) / 2
                            quadraticBezierTo(
                                startX, midY,
                                endX, nextY
                            )
                        }

                        drawPath(
                            path = path,
                            color = if (currentLevel.isUnlocked)
                                Color(0xFFFFD700).copy(alpha = 0.6f)
                            else Color.Gray.copy(alpha = 0.3f),
                            style = Stroke(
                                width = 8f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f))
                            )
                        )

                        currentY = nextY
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    levels.forEachIndexed { index, level ->
                        val spacing = (350 * level.position.ySpacing).dp

                        Spacer(modifier = Modifier.height(if (index == 0) 0.dp else spacing - 350.dp))

                        FloatingIslandLevel(
                            level = level,
                            onClick = {
                                showComingSoonDialog = true
                                SoundManager.playClick()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        if (showComingSoonDialog) {
            ComingSoonDialog(onDismiss = { showComingSoonDialog = false })
        }
    }
}

@Composable
fun GameHeader(
    userName: String,
    userLevel: Int,
    totalStars: Int,
    maxStars: Int,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(RainbowOrange, RainbowPink)
                            )
                        )
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎹",
                        fontSize = 32.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "Level $userLevel - ${getLevelTitle(userLevel)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextLight,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            // Profile button
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(RainbowBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Stars collected
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Stars",
                    tint = RainbowYellow,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "$totalStars/$maxStars",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = RainbowOrange
                    )
                )
            }
        }
    }
}

fun getLevelTitle(level: Int): String {
    return when (level) {
        1 -> "Beginner"
        in 2..3 -> "Learner"
        in 4..5 -> "Player"
        in 6..7 -> "Skilled"
        in 8..10 -> "Expert"
        else -> "Master"
    }
}

@Composable
fun FloatingIslandLevel(
    level: GameLevel,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "island_float")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        contentAlignment = Alignment.Center
    ) {
        // Calculate position based on level configuration
        Box(
            modifier = Modifier
                .offset(x = (level.position.xOffset * 120).dp, y = floatOffset.dp)
                .rotate(if (level.isUnlocked) rotation else 0f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Floating island with level card
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Island base
                    Canvas(
                        modifier = Modifier
                            .size(280.dp, 200.dp)
                    ) {
                        // Draw island ground
                        val groundPath = Path().apply {
                            moveTo(size.width * 0.5f, size.height * 0.3f)
                            lineTo(size.width * 0.9f, size.height * 0.5f)
                            lineTo(size.width * 0.85f, size.height * 0.7f)
                            quadraticBezierTo(
                                size.width * 0.5f, size.height * 0.85f,
                                size.width * 0.15f, size.height * 0.7f
                            )
                            lineTo(size.width * 0.1f, size.height * 0.5f)
                            close()
                        }

                        // Ground gradient
                        drawPath(
                            path = groundPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF90EE90),
                                    Color(0xFF7CB342),
                                    Color(0xFF558B2F)
                                )
                            )
                        )

                        // Island cliff/rock
                        val cliffPath = Path().apply {
                            moveTo(size.width * 0.15f, size.height * 0.7f)
                            lineTo(size.width * 0.85f, size.height * 0.7f)
                            quadraticBezierTo(
                                size.width * 0.75f, size.height * 0.95f,
                                size.width * 0.5f, size.height
                            )
                            quadraticBezierTo(
                                size.width * 0.25f, size.height * 0.95f,
                                size.width * 0.15f, size.height * 0.7f
                            )
                        }

                        drawPath(
                            path = cliffPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF8B6F47),
                                    Color(0xFF6B5638),
                                    Color(0xFF5D4E37)
                                )
                            )
                        )

                        // Add texture to cliff
                        for (i in 0..5) {
                            drawOval(
                                color = Color(0xFF4A3728).copy(alpha = 0.3f),
                                topLeft = Offset(
                                    size.width * (0.2f + i * 0.1f),
                                    size.height * (0.75f + (i % 2) * 0.05f)
                                ),
                                size = Size(size.width * 0.08f, size.height * 0.06f)
                            )
                        }
                    }

                    // Level card on the island
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .offset(y = (-40).dp)
                            .clickable(enabled = level.isUnlocked, onClick = onClick),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (level.isUnlocked)
                                Color.White
                            else Color.White.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (level.isUnlocked) 12.dp else 4.dp
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Colored header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                level.color,
                                                level.color.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Level icon
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(4.dp, level.color, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (level.isUnlocked) {
                                        Text(
                                            text = level.emoji,
                                            fontSize = 36.sp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = TextLight,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Level ${level.number}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = level.color,
                                        fontSize = 16.sp
                                    )
                                )

                                Text(
                                    text = level.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = level.world,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextLight,
                                        fontSize = 14.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Stars
                                if (level.isUnlocked) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        repeat(3) { index ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Star",
                                                tint = if (index < level.stars)
                                                    RainbowYellow
                                                else Color.LightGray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedIslandBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val cloud1X by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud1"
    )

    val cloud2X by infiniteTransition.animateFloat(
        initialValue = 400f,
        targetValue = 1600f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud2"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Draw clouds
        fun drawCloud(x: Float, y: Float, scale: Float) {
            translate(x, y) {
                // Cloud circles
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = 30f * scale,
                    center = Offset(0f, 0f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = 40f * scale,
                    center = Offset(35f * scale, -5f * scale)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = 35f * scale,
                    center = Offset(70f * scale, 0f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = 25f * scale,
                    center = Offset(35f * scale, 10f * scale)
                )
            }
        }

        drawCloud(cloud1X, height * 0.15f, 1f)
        drawCloud(cloud2X, height * 0.25f, 0.8f)
        drawCloud(cloud1X + 300f, height * 0.35f, 1.2f)

        // Draw rolling hills/water at bottom
        val hillPath = Path().apply {
            moveTo(0f, height * 0.85f)

            for (i in 0..20) {
                val x = (width / 20) * i
                val wave = sin((i * 0.5 + wavePhase * 0.01).toDouble()).toFloat() * 20
                if (i == 0) {
                    moveTo(x, height * 0.85f + wave)
                } else {
                    lineTo(x, height * 0.85f + wave)
                }
            }

            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = hillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF90EE90),
                    Color(0xFF98D8C8),
                    Color(0xFF4FC3F7)
                ),
                startY = height * 0.85f,
                endY = height
            )
        )

        // Draw water waves
        for (i in 0 until 3) {
            val wavePath = Path()
            val amplitude = 15f
            val frequency = 0.02f
            val yOffset = height * (0.88f + i * 0.04f)

            wavePath.moveTo(0f, yOffset)

            for (x in 0 until width.toInt() step 10) {
                val y = yOffset + amplitude * sin((x * frequency) + (wavePhase * 0.02f) + i)
                wavePath.lineTo(x.toFloat(), y)
            }

            drawPath(
                path = wavePath,
                color = Color.White.copy(alpha = 0.2f - i * 0.05f),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
fun ComingSoonDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "🎵",
                fontSize = 64.sp
            )
        },
        title = {
            Text(
                text = "Coming Soon!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = RainbowBlue
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "This level is under construction!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "We're working hard to bring you an amazing piano learning experience! 🎹",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        color = TextLight
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SoundManager.playClick()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RainbowBlue
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Got it!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}