// HomeScreen.kt
package com.pianokids.game.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.pianokids.game.R
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import kotlin.math.sin

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
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPrefs = remember { UserPreferences(context) }
    val authRepository = remember { AuthRepository(context) }
    val socialLoginManager = remember { SocialLoginManager(context) }
    val scope = rememberCoroutineScope()

    var showComingSoonDialog by remember { mutableStateOf(false) }
    var showGuestLimitDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()   // <-- REACTIVE!
    val user = userPrefs.getUser()

    val userName = when {
        user != null -> user.name
        isLoggedIn -> userPrefs.getFullName()
        else -> "Guest Player"
    }

    val userPhotoUrl = user?.photoUrl

    val userLevel = when {
        user != null -> user.level
        isLoggedIn -> userPrefs.getLevel()
        else -> 1
    }

    val totalStars = when {
        user != null -> user.score / 100
        isLoggedIn -> userPrefs.getTotalStars()
        else -> 0
    }

    val levels = remember(isLoggedIn) {
        listOf(
            GameLevel(1, "Basic Keys", "Pirate Ship", true, 0, "🚢", OceanDeep, IslandPosition(0f, 1f)),
            GameLevel(2, "Simple Melody", "Japanese Garden", isLoggedIn, 0, "🏯", RainbowPink, IslandPosition(-0.6f, 1.2f)),
            GameLevel(3, "Follow Rhythm", "Anime City", isLoggedIn, 0, "🌸", RainbowViolet, IslandPosition(0.5f, 1.3f)),
            GameLevel(4, "Both Hands", "Ninja Village", isLoggedIn, 0, "🥷", RainbowIndigo, IslandPosition(-0.4f, 1.2f)),
            GameLevel(5, "Speed Challenge", "Dragon Temple", isLoggedIn, 0, "🐉", RainbowRed, IslandPosition(0.6f, 1.3f)),
            GameLevel(6, "Cartoon World", "Superhero City", isLoggedIn, 0, "🦸", RainbowOrange, IslandPosition(-0.5f, 1.2f)),
            GameLevel(7, "Hero Training", "Marvel Universe", isLoggedIn, 0, "⚡", RainbowYellow, IslandPosition(0.4f, 1.3f)),
            GameLevel(8, "Final Concert", "New York", isLoggedIn, 0, "🗽", RainbowGreen, IslandPosition(0f, 1.2f))
        )
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            socialLoginManager.handleGoogleSignInResult(task)
        }
    }

    LaunchedEffect(Unit) { SoundManager.startBackgroundMusic() }

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

        // Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RainbowYellow)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            GameHeader(
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                userLevel = userLevel,
                totalStars = totalStars,
                maxStars = levels.size * 3,
                isLoggedIn = isLoggedIn,
                onProfileClick = onNavigateToProfile
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Roadmap path
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((levels.size * 350).dp)
                ) {
                    val width = size.width
                    var currentY = 180f

                    for (i in 0 until levels.size - 1) {
                        val current = levels[i]
                        val next = levels[i + 1]
                        val startX = width / 2 + (current.position.xOffset * width * 0.3f)
                        val startY = currentY
                        val nextY = startY + (350 * next.position.ySpacing)
                        val endX = width / 2 + (next.position.xOffset * width * 0.3f)

                        val path = Path().apply {
                            moveTo(startX, startY)
                            val midY = (startY + nextY) / 2
                            quadraticBezierTo(startX, midY, endX, nextY)
                        }

                        drawPath(
                            path = path,
                            color = if (current.isUnlocked)
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    levels.forEachIndexed { index, level ->
                        val spacing = (350 * level.position.ySpacing).dp
                        Spacer(modifier = Modifier.height(if (index == 0) 0.dp else spacing - 350.dp))

                        FloatingIslandLevel(
                            level = level,
                            onClick = {
                                if (level.isUnlocked) {
                                    showComingSoonDialog = true
                                } else {
                                    showGuestLimitDialog = true
                                }
                                SoundManager.playClick()
                            }
                        )
                    }
                    Spacer(Modifier.height(100.dp))
                }
            }
        }

        // Dialogs
        if (showComingSoonDialog) {
            ComingSoonDialog { showComingSoonDialog = false }
        }

        if (showGuestLimitDialog) {
            GuestLimitDialog(
                onDismiss = { showGuestLimitDialog = false },
                onLoginClick = {
                    showGuestLimitDialog = false
                    showLoginDialog = true
                }
            )
        }

        if (showLoginDialog) {
            LoginChooserDialog(
                onDismiss = { showLoginDialog = false },
                onGoogleClick = {
                    isLoading = true
                    socialLoginManager.signInWithGoogle(
                        launcher = googleSignInLauncher,
                        onSuccess = { idToken ->
                            scope.launch {
                                val result = authRepository.loginWithSocial(
                                    token = idToken,
                                    provider = "google"
                                )
                                isLoading = false
                                result.onSuccess {
                                    showLoginDialog = false
                                    // Refresh screen
                                }
                                result.onFailure {
                                    // Show error
                                }
                            }
                        },
                        onFailure = {
                            isLoading = false
                        }
                    )
                },
                onFacebookClick = {
                    activity?.let { act ->
                        isLoading = true
                        socialLoginManager.loginWithFacebook(
                            activity = act,
                            onSuccess = { accessToken ->
                                scope.launch {
                                    val result = authRepository.loginWithSocial(
                                        token = accessToken,
                                        provider = "facebook"
                                    )
                                    isLoading = false
                                    result.onSuccess {
                                        showLoginDialog = false
                                        // Refresh screen
                                    }
                                    result.onFailure {
                                        // Show error
                                    }
                                }
                            },
                            onFailure = {
                                isLoading = false
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun GameHeader(
    userName: String,
    userPhotoUrl: String?,
    userLevel: Int,
    totalStars: Int,
    maxStars: Int,
    isLoggedIn: Boolean,
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
                // Avatar
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
                    if (userPhotoUrl != null) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = if (isLoggedIn) "🎹" else "👤",
                            fontSize = 32.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            maxLines = 1
                        )

                        // Guest badge
                        if (!isLoggedIn) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RainbowOrange.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "GUEST",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = RainbowOrange
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

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

            // Stars
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
        Box(
            modifier = Modifier
                .offset(x = (level.position.xOffset * 120).dp, y = floatOffset.dp)
                .rotate(if (level.isUnlocked) rotation else 0f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    // Island base
                    Canvas(modifier = Modifier.size(280.dp, 200.dp)) {
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

                    // Level card
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .offset(y = (-40).dp)
                            .clickable(enabled = true, onClick = onClick),
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
                        Box(modifier = Modifier.fillMaxWidth()) {
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
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(4.dp, level.color, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (level.isUnlocked) {
                                        Text(text = level.emoji, fontSize = 36.sp)
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

                                if (level.isUnlocked) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

        fun drawCloud(x: Float, y: Float, scale: Float) {
            translate(x, y) {
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
        icon = { Text(text = "🎵", fontSize = 64.sp) },
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
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
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
                colors = ButtonDefaults.buttonColors(containerColor = RainbowBlue),
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

@Composable
fun GuestLimitDialog(
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text(text = "🔒", fontSize = 64.sp) },
        title = {
            Text(
                text = "Login Required",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = RainbowOrange
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
                    text = "This level is locked for guests!",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Create an account or login to unlock all levels and save your progress! 🎹✨",
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
                    onLoginClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RainbowBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Login Now",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                SoundManager.playClick()
                onDismiss()
            }) {
                Text("Maybe Later", color = TextLight)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

