
package com.pianokids.game.view.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.pianokids.game.R
import com.pianokids.game.data.models.Level
import com.pianokids.game.data.repository.LevelRepository
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.viewmodel.AuthViewModel
import com.pianokids.game.viewmodel.AvatarViewModel
import com.pianokids.game.utils.components.AvatarCreationDialog
import kotlinx.coroutines.launch
import kotlin.math.sin

// ---- Ocean Clouds Data ----
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
    onNavigateToLevel: (String, String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPrefs = remember { UserPreferences(context) }
    val authRepository = remember { AuthRepository(context) }
    val socialLoginManager = remember { SocialLoginManager(context) }
    val levelRepository = remember { LevelRepository() }
    val scope = rememberCoroutineScope()

    var showComingSoonDialog by remember { mutableStateOf(false) }
    var showGuestLimitDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showCreateAvatarDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    val authViewModel: AuthViewModel = viewModel()
    val avatarViewModel: AvatarViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val user = userPrefs.getUser()

    val userId = user?.id ?: "guest"
    val isGuest = userPrefs.isGuestMode()

    val userPhotoUrl = user?.photoUrl

    var levels by remember { mutableStateOf<List<Level>>(emptyList()) }
    var unlockedMap by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    val totalStars = user?.score ?: 0

    // Animation offsets
    var waveOffset by remember { mutableStateOf(0f) }
    var cloudOffset by remember { mutableStateOf(0f) }
    var floatOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        SoundManager.startBackgroundMusic()
        while (true) {
            waveOffset += 0.5f
            cloudOffset += 0.3f
            floatOffset += 0.05f
            kotlinx.coroutines.delay(50)
        }
    }

    // ----- LOAD LEVELS FROM BACKEND -----
    LaunchedEffect(isLoggedIn) {
        isLoading = true

        val allLevels = levelRepository.getAllLevels() ?: emptyList()
        levels = allLevels.sortedBy { it.order }

        // Unlocking logic
        unlockedMap =
            if (isLoggedIn && userId != "guest") {
                val response = levelRepository.getUnlockedLevels(userId)
                response?.levels?.associate { it.levelId to it.unlocked } ?: emptyMap()
            } else {
                allLevels.firstOrNull()?.let { lvl -> mapOf(lvl._id to true) } ?: emptyMap()
            }

        isLoading = false
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            socialLoginManager.handleGoogleSignInResult(task)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. ANIMATED OCEAN WITH CLOUDS AND BIRDS
        OceanMapBackground(
            waveOffset = waveOffset,
            cloudOffset = cloudOffset,
            birdOffset = birdOffset
        )

        // Background ocean
        OceanMapBackground(waveOffset = waveOffset, cloudOffset = cloudOffset)

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RainbowYellow)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {

            // ---- HEADER ----
            CompactGameHeader(
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                totalStars = totalStars,
                maxStars = levels.size * 3,
                isLoggedIn = isLoggedIn,
                onBackClick = onNavigateBack,
                onProfileClick = onNavigateToProfile,
                onAddAvatarClick = if (isLoggedIn) {
                    { showCreateAvatarDialog = true }
                } else null
            )

            // ---- MAP ----
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
                    levels.forEach { level ->

                        val isUnlocked = unlockedMap[level._id] == true

                        MapIsland(
                            level = level,
                            isUnlocked = isUnlocked,
                            floatOffset = floatOffset,
                            onClick = {
                                SoundManager.playClick()

                                when {
                                    isGuest && level.order != 1 -> {
                                        showGuestLimitDialog = true
                                    }
                                    !isUnlocked -> {
                                        showComingSoonDialog = true
                                    }
                                    else -> {
                                        onNavigateToLevel(level._id, userId)
                                    }
                                }
                                SoundManager.playClick()
                            }
                        )
                    }
                }
            }
        }

        // ---- DIALOGS ----
        if (showLoginDialog) {
            LoginChooserDialog(
                onDismiss = { showLoginDialog = false },
                onGoogleClick = {
                    isLoading = true
                    socialLoginManager.signInWithGoogle(
                        launcher = googleSignInLauncher,
                        onSuccess = { idToken ->
                            scope.launch {
                                val result =
                                    authRepository.loginWithSocial(token = idToken, provider = "google")
                                isLoading = false
                                result.onSuccess {
                                    authViewModel.onLoginSuccess()
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
                                    val result = authRepository.loginWithSocial(
                                        token = accessToken,
                                        provider = "facebook"
                                    )
                                    isLoading = false
                                    result.onSuccess {
                                        authViewModel.onLoginSuccess()
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

        if (showGuestLimitDialog) {
            GuestLimitDialog(
                onDismiss = { showGuestLimitDialog = false },
                onLoginClick = {
                    showGuestLimitDialog = false
                    showLoginDialog = true
                }
            )
        }

        if (showComingSoonDialog) {
            ComingSoonDialog { showComingSoonDialog = false }
        }

        // Avatar Creation Dialog
        if (showCreateAvatarDialog) {
            AvatarCreationDialog(
                onDismiss = {
                    showCreateAvatarDialog = false
                },
                onCreateAvatar = { name, avatarImageUrl ->
                    avatarViewModel.createAvatar(name, avatarImageUrl)
                    showCreateAvatarDialog = false
                }
            )
        }
    }
}


    }
}

// -------------------------------------------------------------
// MAP ISLAND
// -------------------------------------------------------------
@Composable
fun MapIsland(
    level: Level,
    isUnlocked: Boolean,
    floatOffset: Float,
    onClick: () -> Unit
) {
    val pos = level.mapPosition
    val floatY = sin(floatOffset + level.order * 0.5) * 8f

    Box(
        modifier = Modifier
            .offset(
                x = (pos.x * 1600f).dp,
                y = (pos.y * 1400f + floatY).dp
            )
            .size(350.dp)
            .zIndex(level.order.toFloat()),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // -------- LEVEL CARD --------
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .height(130.dp)
                    .clickable(onClick = onClick),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) Color.White else Color(0xFFE0E0E0)
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (!isUnlocked) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                    } else {
                        Text("🎵", fontSize = 26.sp)
                    }

                    Spacer(Modifier.height(4.dp))

                    // Level title
                    Text(
                        text = level.title,
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )

                    Spacer(Modifier.height(4.dp))

                    if (isUnlocked) {
                        Row {
                            repeat(3) { i ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i < level.starsUnlocked) RainbowYellow else Color.LightGray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else {
                        Text("Locked", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // -------- ISLAND IMAGE FROM BACKEND --------
            AsyncImage(
                model = level.islandImageUrl,
                contentDescription = "Island ${level.title}",
                modifier = Modifier.size(280.dp),
                contentScale = ContentScale.Fit,
                alpha = if (isUnlocked) 1f else 0.5f
            )
        }
    }
}

// -------------------------------------------------------------
// BACKGROUND
// -------------------------------------------------------------
@Composable
fun OceanMapBackground(waveOffset: Float, cloudOffset: Float) {
    Image(
        painter = painterResource(R.drawable.sea),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

// -------------------------------------------------------------
// DIALOGS (unchanged)
// -------------------------------------------------------------
@Composable
fun ComingSoonDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Level Locked", fontWeight = FontWeight.Bold) },
        text = { Text("Complete previous levels to unlock this one!") },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
fun GuestLimitDialog(onDismiss: () -> Unit, onLoginClick: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login required") },
        text = { Text("Guests can only play Level 1.") },
        confirmButton = {
            Button(onClick = onLoginClick) { Text("Login") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CompactGameHeader(
    userName: String,
    userPhotoUrl: String?,
    totalStars: Int,
    maxStars: Int,
    isLoggedIn: Boolean,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddAvatarClick: (() -> Unit)? = null
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
                    imageVector = Icons.Default.ArrowBack,
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add Avatar Button (only for logged in users)
                if (isLoggedIn && onAddAvatarClick != null) {
                    IconButton(
                        onClick = {
                            SoundManager.playClick()
                            onAddAvatarClick()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(RainbowBlue.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Avatar",
                            tint = RainbowBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Stars Display
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
}
