
package com.pianokids.game.view.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
    val position: IslandPosition,
    val landImageRes: Int
)

data class IslandPosition(
    val x: Float, // 0..1 across map
    val y: Float  // 0..1 down map
)

data class Cloud(
    val id: Int,
    var x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

data class Bird(
    val id: Int,
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float
)

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToLevel1: () -> Unit = {}
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
    val verticalScrollState = rememberScrollState()
    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val user = userPrefs.getUser()

    val userPhotoUrl = user?.photoUrl
    val totalStars = when {
        user != null -> user.score / 100
        isLoggedIn -> userPrefs.getTotalStars()
        else -> 0
    }

    var waveOffset by remember { mutableStateOf(0f) }
    var cloudOffset by remember { mutableStateOf(0f) }
    var birdOffset by remember { mutableStateOf(0f) }
    var islandFloat by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            waveOffset += 0.5f
            cloudOffset += 0.3f
            birdOffset += 0.8f
            islandFloat += 0.05f
            kotlinx.coroutines.delay(50)
        }
    }
// In HomeScreen, replace the levels definition with this:

    val levels = remember(isLoggedIn, user) {
        // Get the user's unlocked levels (only level 1 for now)
        val unlockedLevels = userPrefs.getUnlockedLevels() // Returns listOf(1)

        listOf(
            GameLevel(
                number = 1,
                title = "Gotham Nights",
                world = "Dark Knight City",
                isUnlocked = true, // Level 1 is always unlocked for everyone
                stars = 3,
                emoji = "🦇",
                color = Color(0xFF1A1A1A),
                position = IslandPosition(0.15f, 0.30f),
                landImageRes = R.drawable.level_1
            ),
            GameLevel(
                number = 2,
                title = "Web of Justice",
                world = "Hero's Landing",
                isUnlocked = unlockedLevels.contains(2), // Unlocked only if in user's progress
                stars = 2,
                emoji = "🕷️",
                color = RainbowRed,
                position = IslandPosition(0.32f, 0.48f),
                landImageRes = R.drawable.level_2
            ),
            GameLevel(
                number = 3,
                title = "Moonlight Magic",
                world = "Crystal Kingdom",
                isUnlocked = unlockedLevels.contains(3),
                stars = 1,
                emoji = "🌙",
                color = RainbowPink,
                position = IslandPosition(0.48f, 0.28f),
                landImageRes = R.drawable.level_3
            ),
            GameLevel(
                number = 6,
                title = "Electric Meadow",
                world = "Pokémon Plains",
                isUnlocked = unlockedLevels.contains(6),
                stars = 0,
                emoji = "⚡",
                color = RainbowYellow,
                position = IslandPosition(0.18f, 0.65f),
                landImageRes = R.drawable.level_4
            ),
            GameLevel(
                number = 5,
                title = "Shield of Justice",
                world = "Avengers Base",
                isUnlocked = unlockedLevels.contains(5),
                stars = 0,
                emoji = "🛡️",
                color = RainbowBlue,
                position = IslandPosition(0.55f, 0.63f),
                landImageRes = R.drawable.level_5
            ),
            GameLevel(
                number = 4,
                title = "Hidden Village",
                world = "Ninja Path",
                isUnlocked = unlockedLevels.contains(4),
                stars = 0,
                emoji = "🥷",
                color = RainbowIndigo,
                position = IslandPosition(0.76f, 0.42f),
                landImageRes = R.drawable.level_6
            )
        )
    }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            socialLoginManager.handleGoogleSignInResult(task)
        }
    }

    LaunchedEffect(Unit) {
        SoundManager.startBackgroundMusic()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. ANIMATED OCEAN WITH CLOUDS AND BIRDS
        OceanMapBackground(
            waveOffset = waveOffset,
            cloudOffset = cloudOffset,
            birdOffset = birdOffset
        )

        // 2. LOADING
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
            // 3. HEADER
            CompactGameHeader(
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                totalStars = totalStars,
                maxStars = levels.size * 3,
                isLoggedIn = isLoggedIn,
                onBackClick = onNavigateBack,
                onProfileClick = onNavigateToProfile
            )

            // 4. 2D SCROLLABLE MAP
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
                    .verticalScroll(verticalScrollState)
                    .padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(2800.dp)
                        .height(1800.dp)
                ) {
                    // 5. ANIMATED ISLANDS ON MAP
                    levels.forEach { level ->
                        MapIsland(
                            level = level,
                            mapWidth = 1600.dp,
                            mapHeight = 1400.dp,
                            floatOffset = islandFloat,
                            onClick = {
                                if (level.isUnlocked) {
                                    if (level.number == 1) {
                                        onNavigateToLevel1()
                                    } else {
                                        showComingSoonDialog = true
                                    }
                                } else {
                                    showGuestLimitDialog = true
                                }
                                SoundManager.playClick()
                            }
                        )
                    }
                }
            }
        }

        // DIALOGS
        if (showComingSoonDialog) {
            ComingSoonDialog { showComingSoonDialog = false }
        }
        if (showGuestLimitDialog) {
            GuestLimitDialog(
                onDismiss = { showGuestLimitDialog = false },
                onLoginClick = { showGuestLimitDialog = false; showLoginDialog = true }
            )
        }
        if (showLoginDialog) {
            LoginChooserDialog(
                onDismiss = { showLoginDialog = false },
                // In HomeScreen, update the login callbacks:

// For Google login:
                onGoogleClick = {
                    isLoading = true
                    socialLoginManager.signInWithGoogle(
                        launcher = googleSignInLauncher,
                        onSuccess = { idToken ->
                            scope.launch {
                                val result = authRepository.loginWithSocial(token = idToken, provider = "google")
                                isLoading = false
                                result.onSuccess {
                                    authViewModel.onLoginSuccess() // ✅ ADD THIS
                                    showLoginDialog = false
                                }
                            }
                        },
                        onFailure = { isLoading = false }
                    )
                },

// For Facebook login:
                onFacebookClick = {
                    activity?.let { act ->
                        isLoading = true
                        socialLoginManager.loginWithFacebook(
                            activity = act,
                            onSuccess = { accessToken ->
                                scope.launch {
                                    val result = authRepository.loginWithSocial(token = accessToken, provider = "facebook")
                                    isLoading = false
                                    result.onSuccess {
                                        authViewModel.onLoginSuccess() // ✅ ADD THIS
                                        showLoginDialog = false
                                    }
                                }
                            },
                            onFailure = { isLoading = false }
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun MapIsland(
    level: GameLevel,
    mapWidth: androidx.compose.ui.unit.Dp,
    mapHeight: androidx.compose.ui.unit.Dp,
    floatOffset: Float,
    onClick: () -> Unit
) {
    // Calculate floating animation
    val floatY = sin(floatOffset + level.number * 0.5) * 8f

    Box(
        modifier = Modifier
            .offset(
                x = (level.position.x * mapWidth.value).dp,
                y = (level.position.y * mapHeight.value + floatY).dp
            )
            .size(400.dp)
            .zIndex(level.number.toFloat()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // LEVEL CARD
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .height(140.dp)
                    .clickable(onClick = onClick),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (level.isUnlocked) Color.White else Color(0xFFE0E0E0)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (level.isUnlocked) 10.dp else 4.dp
                ),
                border = BorderStroke(
                    width = 3.dp,
                    color = if (level.isUnlocked) level.color else Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (level.isUnlocked) {
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White,
                                        level.color.copy(alpha = 0.05f)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFF5F5F5),
                                        Color(0xFFEEEEEE)
                                    )
                                )
                            }
                        )
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Emoji/Lock icon
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (level.isUnlocked) level.color.copy(alpha = 0.15f)
                                else Color.Gray.copy(alpha = 0.2f)
                            )
                            .border(3.dp, level.color.copy(alpha = if (level.isUnlocked) 1f else 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (level.isUnlocked) {
                            Text(text = level.emoji, fontSize = 28.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Level number
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(level.color.copy(alpha = if (level.isUnlocked) 1f else 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${level.number}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        )
                    }

                    // Level title
                    Text(
                        text = level.title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (level.isUnlocked) Color.Black else Color.Gray
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 11.sp
                    )

                    // Stars
                    if (level.isUnlocked) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(3) { i ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star ${i + 1}",
                                    tint = if (i < level.stars) RainbowYellow else Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Locked",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // LAND IMAGE with subtle shadow
            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Shadow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawOval(
                        color = Color.Black.copy(alpha = 0.2f),
                        topLeft = Offset(size.width * 0.2f, size.height * 0.85f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.1f)
                    )
                }

                // Island image
                Image(
                    painter = painterResource(id = level.landImageRes),
                    contentDescription = "Island ${level.number}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    alpha = if (level.isUnlocked) 1f else 0.6f
                )
            }
        }
    }
}

// OCEAN with sea.png background, animated waves, clouds, and birds
@Composable
fun OceanMapBackground(
    waveOffset: Float,
    cloudOffset: Float,
    birdOffset: Float
) {
    // Initialize clouds
    val clouds = remember {
        listOf(
            Cloud(1, -200f, 100f, 120f, 1.2f, 0.7f),
            Cloud(2, 300f, 180f, 150f, 0.8f, 0.6f),
            Cloud(3, 800f, 80f, 100f, 1.5f, 0.8f),
            Cloud(4, 1200f, 220f, 130f, 1.0f, 0.65f),
            Cloud(5, 1600f, 140f, 140f, 0.9f, 0.75f),
            Cloud(6, 2000f, 190f, 110f, 1.3f, 0.7f)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image (sea.png)
        Image(
            painter = painterResource(id = R.drawable.sea),
            contentDescription = "Ocean Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Canvas for waves, clouds, and birds
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // DRAW CLOUDS
            clouds.forEach { cloud ->
                val cloudX = (cloud.x + cloudOffset * cloud.speed) % (width + 400f) - 200f

                // Draw cloud using circles
                val cloudColor = Color.White.copy(alpha = cloud.alpha)

                // Main cloud body (3 overlapping circles)
                drawCircle(
                    color = cloudColor,
                    radius = cloud.size * 0.5f,
                    center = Offset(cloudX, cloud.y)
                )
                drawCircle(
                    color = cloudColor,
                    radius = cloud.size * 0.6f,
                    center = Offset(cloudX + cloud.size * 0.4f, cloud.y)
                )
                drawCircle(
                    color = cloudColor,
                    radius = cloud.size * 0.55f,
                    center = Offset(cloudX + cloud.size * 0.8f, cloud.y + cloud.size * 0.1f)
                )
                drawCircle(
                    color = cloudColor,
                    radius = cloud.size * 0.45f,
                    center = Offset(cloudX - cloud.size * 0.3f, cloud.y + cloud.size * 0.15f)
                )
            }



            // ANIMATED WAVES
            val waveCount = 8
            val baseWaveHeight = 15f
            val waveLength = width / 3f

            for (i in 0 until waveCount) {
                val yOffset = height * 0.3f + i * 60f
                val phase = waveOffset + i * 0.5f
                val waveHeight = baseWaveHeight * (1f - i * 0.06f)

                val path = Path().apply {
                    var x = -waveLength
                    moveTo(x, yOffset)

                    while (x < width + waveLength) {
                        val angle = (x + phase * 10f) / waveLength * Math.PI * 2
                        val y = yOffset + waveHeight * sin(angle).toFloat()
                        lineTo(x, y)
                        x += 10f
                    }

                    lineTo(width + waveLength, height)
                    lineTo(-waveLength, height)
                    close()
                }

                // Subtle wave overlay
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.08f - i * 0.008f)
                )

                // Wave highlights
                if (i % 2 == 0) {
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.15f - i * 0.015f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
        }
    }
}









// Compact game header
@Composable
fun CompactGameHeader(
    userName: String,
    userPhotoUrl: String?,
    totalStars: Int,
    maxStars: Int,
    isLoggedIn: Boolean,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { SoundManager.playClick(); onBackClick() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RainbowBlue.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = RainbowBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(RainbowOrange, RainbowPink)))
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (userPhotoUrl != null) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(text = "🎹", fontSize = 24.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        maxLines = 1
                    )
                    if (!isLoggedIn) {
                        Text(
                            text = "Guest Mode",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = RainbowOrange
                            )
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(RainbowYellow.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Stars",
                    tint = RainbowYellow,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$totalStars/$maxStars",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = RainbowOrange
                    )
                )
            }
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("This level is under construction!", fontSize = 20.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("We're working hard to bring you an amazing piano experience! ✨", fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(
                onClick = { SoundManager.playClick(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = RainbowBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Got it!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}
@Composable
fun GuestLimitDialog(onDismiss: () -> Unit, onLoginClick: () -> Unit) {
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("This level is locked for guests!", fontSize = 20.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Login to unlock all levels and save your progress! 🎮", fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(
                onClick = { SoundManager.playClick(); onLoginClick() },
                colors = ButtonDefaults.buttonColors(containerColor = RainbowBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login Now", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { SoundManager.playClick(); onDismiss() }) {
                Text("Maybe Later", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}
