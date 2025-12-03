package com.pianokids.game.view.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.pianokids.game.R
import com.pianokids.game.data.models.Level
import com.pianokids.game.data.models.UnlockedLevelItem
import com.pianokids.game.data.models.getEffectivePosition
import com.pianokids.game.data.models.KidProfile
import com.pianokids.game.data.models.Avatar
import com.pianokids.game.data.models.AvatarCustomization
import com.pianokids.game.data.repository.LevelRepository
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.data.repository.SublevelProgressRepository
import com.pianokids.game.data.repository.SublevelRepository
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.viewmodel.AuthViewModel
import com.pianokids.game.viewmodel.AvatarViewModel
import com.pianokids.game.utils.components.AvatarCreationDialog
import com.pianokids.game.utils.components.AIAvatarPreviewDialog
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
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
    onNavigateToLevel: (String) -> Unit,
    onNavigateToMusic: () -> Unit = {},
    onNavigateToKaraoke: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPrefs = remember { UserPreferences(context) }
    val authRepository = remember { AuthRepository(context) }
    val socialLoginManager = remember { SocialLoginManager(context) }
    val levelRepository = remember { LevelRepository() }
    val sublevelProgRepository = remember { SublevelProgressRepository() }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showComingSoonDialog by remember { mutableStateOf(false) }
    var showGuestLimitDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showCreateAvatarDialog by remember { mutableStateOf(false) }
    var resumeAvatarFlowAfterLogin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    val authViewModel: AuthViewModel = viewModel()
    val avatarViewModel: AvatarViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    var kidProfile by remember { mutableStateOf(userPrefs.getKidProfile()) }

    // Handle device back button - close drawer first, otherwise exit
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    BackHandler(enabled = !drawerState.isOpen) {
        activity?.finish()
    }

    // Snackbar and avatar states
    val snackbarHostState = remember { SnackbarHostState() }
    val avatarError by avatarViewModel.error.collectAsState()
    val activeAvatar by avatarViewModel.activeAvatar.collectAsState()

    LaunchedEffect(isLoggedIn, activeAvatar?.id) {
        kidProfile = userPrefs.getKidProfile()
    }
    
    // AI Avatar Generation states
    val isGeneratingAI by avatarViewModel.isGeneratingAI.collectAsState()
    val aiGenerationResponse by avatarViewModel.aiGenerationResponse.collectAsState()
    var showAIPreviewDialog by remember { mutableStateOf(false) }
    var pendingAIAvatar by remember { mutableStateOf<com.pianokids.game.data.models.AvatarGenerationResponse?>(null) }
    var pendingAIAvatarName by remember { mutableStateOf("") }
    var pendingPrompt by remember { mutableStateOf("") }
    var pendingStyle by remember { mutableStateOf("cartoon") }

    // Show avatar error
    LaunchedEffect(avatarError) {
        avatarError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Error: $error",
                    duration = SnackbarDuration.Short
                )
            }
            avatarViewModel.clearError()
        }
    }
    
    // Listen for AI generation success and show preview
    LaunchedEffect(aiGenerationResponse) {
        aiGenerationResponse?.let { response ->
            android.util.Log.d("HomeScreen", "🎉 AI Generation Response Received!")
            android.util.Log.d("HomeScreen", "Avatar Name: ${response.name}")
            android.util.Log.d("HomeScreen", "Description: ${response.aiGeneratedDescription}")
            android.util.Log.d("HomeScreen", "Image URL: ${response.avatarImageUrl}")
            
            pendingAIAvatar = response
            showAIPreviewDialog = true
            android.util.Log.d("HomeScreen", "✅ Preview dialog should show now: $showAIPreviewDialog")
            avatarViewModel.clearAIGenerationResponse()
        }
    }
    val userName by authViewModel.userName.collectAsState()
    val user = userPrefs.getUser()

    val userId = user?.id ?: "guest"
    val isGuest = userPrefs.isGuestMode()

    val userPhotoUrl = user?.photoUrl

    var levels by remember { mutableStateOf<List<Level>>(emptyList()) }
    var progressMap by remember {
        mutableStateOf<Map<String, UnlockedLevelItem>>(emptyMap())
    }

// BACKEND-DRIVEN STAR COUNT (SUBLEVEL BASED)
    var totalStars by remember { mutableStateOf(0) }
    var maxStars by remember { mutableStateOf(0) }

    // Animation offsets
    var waveOffset by remember { mutableStateOf(0f) }
    var cloudOffset by remember { mutableStateOf(0f) }
    var birdOffset by remember { mutableStateOf(0f) }
    var floatOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        SoundManager.startBackgroundMusic()
        while (true) {
            waveOffset += 0.5f
            cloudOffset += 0.3f
            floatOffset += 0.05f
            birdOffset += 0.3f
            kotlinx.coroutines.delay(50)
        }
    }

    // ----- LOAD ACTIVE AVATAR -----
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            avatarViewModel.loadActiveAvatar()
        }
    }

    // ----- LOAD LEVELS FROM BACKEND -----
    LaunchedEffect(isLoggedIn) {
        isLoading = true

        // 1️⃣ Load all levels
        val allLevels = levelRepository.getAllLevels() ?: emptyList()

        // Fix missing or zero `order`
        levels = allLevels.mapIndexed { index, level ->
            if (level.order == 0) level.copy(order = index + 1) else level
        }.sortedBy { it.order }

        // 2️⃣ Load user unlock info ONCE (no repetition)
        val unlockResponse =
            if (isLoggedIn && userId != "guest")
                levelRepository.getUnlockedLevels(userId)
            else null

        val unlockedBackendList = unlockResponse?.levels ?: emptyList()

        // Build a lookup table for quick access
        val unlockedMapBackend = unlockedBackendList.associateBy { it.levelId }

        // 4️⃣ Build final progress map (Frontend interpretation)
        progressMap = levels.associate { level ->

            val backend = unlockedMapBackend[level._id]

            val isUnlocked =
                when {
                    level.order == 1 -> true // Level 1 always unlocked
                    isLoggedIn && backend != null -> backend.unlocked
                    isLoggedIn -> false
                    else -> false // guest: only level 1 is unlocked, others false
                }

            val stars = backend?.starsUnlocked ?: 0

            level._id to UnlockedLevelItem(
                levelId = level._id,
                title = level.title,
                theme = level.theme,
                unlocked = isUnlocked,
                starsUnlocked = stars,
                backgroundUrl = level.backgroundUrl,
                bossUrl = level.bossUrl,
                musicUrl = level.musicUrl
            )
        }

        // sublevel stars
        var sumStars = 0
        var sumMaxStars = 0

        for (level in levels) {
            // Get all sublevels for this level from backend
            val sublevels = sublevelProgRepository.getUserSublevels(userId, level._id) ?: emptyList()

            sumStars += sublevels.sumOf { it.starsEarned }          // earned
            sumMaxStars += sublevels.sumOf { it.maxStars }    // possible
        }

        totalStars = sumStars
        maxStars = sumMaxStars

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

    val headerAvatarUrl = activeAvatar?.avatarImageUrl
        ?: kidProfile?.backendAvatarImageUrl
        ?: userPrefs.getAvatarThumbnail()
    val headerAvatarName = activeAvatar?.name ?: kidProfile?.backendAvatarName
    val fallbackAvatarEmoji = kidProfile?.avatarEmoji ?: "🎹"
    val avatarAccentColor = remember(activeAvatar, kidProfile?.avatarColorHex, fallbackAvatarEmoji) {
        activeAvatar?.customization?.accentColor()
            ?: kidProfile?.avatarColorHex?.toColorOrNull()
            ?: activeAvatar?.accentIdColor()
            ?: fallbackAvatarEmoji.toAccentPaletteColor()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = avatarAccentColor.copy(alpha = 0.25f),
        drawerContent = {
            HomeProfileDrawer(
                userName = userName,
                avatarImageUrl = headerAvatarUrl,
                avatarName = headerAvatarName,
                fallbackEmoji = fallbackAvatarEmoji,
                isLoggedIn = isLoggedIn,
                accentColor = avatarAccentColor,
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToProfile()
                },
                onRecognizeClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToMusic()
                },
                onAddAvatarClick = {
                    scope.launch { drawerState.close() }
                    if (isLoggedIn) {
                        resumeAvatarFlowAfterLogin = false
                        showCreateAvatarDialog = true
                    } else {
                        resumeAvatarFlowAfterLogin = true
                        showLoginDialog = true
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. ANIMATED OCEAN WITH CLOUDS AND BIRDS
            OceanMapBackground(
                waveOffset = waveOffset,
                cloudOffset = cloudOffset,
                birdOffset = birdOffset
            )

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

            CompactGameHeader(
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                avatarImageUrl = headerAvatarUrl,
                avatarName = headerAvatarName,
                fallbackEmoji = fallbackAvatarEmoji,
                totalStars = totalStars,
                maxStars = maxStars,
                isLoggedIn = isLoggedIn,
                accentColor = avatarAccentColor,
                onAvatarClick = {
                    SoundManager.playClick()
                    scope.launch {
                        if (drawerState.isOpen) drawerState.close() else drawerState.open()
                    }
                }
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

                        val progress = progressMap[level._id]
                        val isUnlocked = progress?.unlocked == true
                        val stars = progress?.starsUnlocked ?: 0

                        MapIsland(
                            level = level,
                            isUnlocked = isUnlocked,
                            stars = stars,
                            floatOffset = floatOffset,
                            onClick = {
                                SoundManager.playClick()

                                when {
                                    isGuest && level.order != 1 -> showGuestLimitDialog = true
                                    !isUnlocked -> showComingSoonDialog = true
                                    else -> onNavigateToLevel(level._id)
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
                onDismiss = {
                    showLoginDialog = false
                    resumeAvatarFlowAfterLogin = false
                },
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
                                if (result.isSuccess) {
                                    authViewModel.onLoginSuccess()
                                    showLoginDialog = false
                                    if (resumeAvatarFlowAfterLogin) {
                                        resumeAvatarFlowAfterLogin = false
                                        showCreateAvatarDialog = true
                                    }
                                } else {
                                    val errorMessage = result.exceptionOrNull()?.message
                                        ?: "Google login failed"
                                    snackbarHostState.showSnackbar(
                                        message = errorMessage,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        onFailure = { error ->
                            isLoading = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = error.message ?: "Google login unavailable",
                                    duration = SnackbarDuration.Short
                                )
                            }
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
                                    if (result.isSuccess) {
                                        authViewModel.onLoginSuccess()
                                        showLoginDialog = false
                                        if (resumeAvatarFlowAfterLogin) {
                                            resumeAvatarFlowAfterLogin = false
                                            showCreateAvatarDialog = true
                                        }
                                    } else {
                                        val errorMessage = result.exceptionOrNull()?.message
                                            ?: "Facebook login failed"
                                        snackbarHostState.showSnackbar(
                                            message = errorMessage,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            onFailure = { error ->
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = error.message ?: "Facebook login unavailable",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
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
                    showCreateAvatarDialog = false
                    avatarViewModel.createAvatar(name, avatarImageUrl)

                    // Show success message
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "✨ Avatar '$name' created successfully!",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onCreateAvatarWithAI = { name, prompt, style ->
                    showCreateAvatarDialog = false
                    pendingAIAvatarName = name
                    pendingPrompt = prompt
                    pendingStyle = style
                    
                    // Generate avatar - will trigger preview or error
                    avatarViewModel.generateAvatarFromPrompt(prompt, name, style)
                }
            )
        }
        
        // AI Avatar Preview Dialog
        if (showAIPreviewDialog && pendingAIAvatar != null) {
            AIAvatarPreviewDialog(
                avatarName = pendingAIAvatarName,
                generationResponse = pendingAIAvatar!!,
                onSave = {
                    // Save avatar to database (was only preview before)
                    pendingAIAvatar?.previewData?.let { previewData ->
                        avatarViewModel.saveAIAvatar(previewData)
                        
                        showAIPreviewDialog = false
                        pendingAIAvatar = null
                        snackbarHostState.currentSnackbarData?.dismiss()
                        
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "✨ Avatar saved! Welcome, ${pendingAIAvatarName}!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                onRegenerate = {
                    // Regenerate with same prompt (don't save current preview)
                    showAIPreviewDialog = false
                    pendingAIAvatar = null
                    
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "🔄 Creating a new version...",
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                    
                    avatarViewModel.generateAvatarFromPrompt(pendingPrompt, pendingAIAvatarName, pendingStyle)
                },
                onDismiss = {
                    // Cancel - don't save avatar to database
                    showAIPreviewDialog = false
                    pendingAIAvatar = null
                    snackbarHostState.currentSnackbarData?.dismiss()
                    
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "❌ Avatar not saved",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                isSaving = false
            )
        }
        
        // AI Generation Loading Dialog
        if (isGeneratingAI) {
            AlertDialog(
                onDismissRequest = { /* Can't dismiss while generating */ },
                title = { 
                    Text("🎨 Creating Your AI Avatar", style = MaterialTheme.typography.headlineSmall) 
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(64.dp),
                            color = Color(0xFF667EEA),
                            strokeWidth = 6.dp
                        )
                        Text(
                            "Please wait...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "⏱️ Usually takes 10-30 seconds",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF667EEA),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Creating unique AI art for your avatar...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {}
            )
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = Color.White,
                contentColor = Color(0xFF667EEA),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

}


@Composable
fun HomeProfileDrawer(
    userName: String,
    avatarImageUrl: String?,
    avatarName: String?,
    fallbackEmoji: String,
    isLoggedIn: Boolean,
    accentColor: Color,
    onProfileClick: () -> Unit,
    onRecognizeClick: () -> Unit,
    onAddAvatarClick: () -> Unit
) {
    val drawerBackground = accentColor.mixWith(Color.Black, 0.55f)
    val avatarFrameColor = accentColor.mixWith(Color.White, 0.25f)

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp),
        drawerContainerColor = drawerBackground,
        drawerContentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(avatarFrameColor.copy(alpha = 0.15f))
                            .border(
                                width = 3.dp,
                                color = avatarFrameColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val displayUrl = avatarImageUrl
                        if (displayUrl != null) {
                            AsyncImage(
                                model = displayUrl,
                                contentDescription = "Active avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(text = fallbackEmoji, fontSize = 42.sp)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (userName.isBlank()) "Hey there!" else "Hi, $userName",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = when {
                                !isLoggedIn -> "Sign in to save your music journey"
                                avatarName != null -> "Avatar: $avatarName"
                                else -> "Create an avatar to join the band"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.18f))

                DrawerActionButton(
                    icon = Icons.Default.Person,
                    label = "Profile",
                    description = "View progress & settings",
                    accentColor = RainbowBlue
                ) {
                    SoundManager.playClick()
                    onProfileClick()
                }

                DrawerActionButton(
                    icon = Icons.Default.GraphicEq,
                    label = "Recognize",
                    description = "Identify what you're playing",
                    accentColor = RainbowIndigo
                ) {
                    SoundManager.playClick()
                    onRecognizeClick()
                }

                DrawerActionButton(
                    icon = Icons.Default.Add,
                    label = if (isLoggedIn) "Add Avatar" else "Sign In & Add Avatar",
                    description = if (isLoggedIn) "Create a new hero" else "Tap to unlock custom avatars",
                    accentColor = RainbowPink
                ) {
                    SoundManager.playClick()
                    onAddAvatarClick()
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Need help?",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "support@pianokids.app",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DrawerActionButton(
    icon: ImageVector,
    label: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}



// -------------------------------------------------------------
// MAP ISLAND - WITH SOLUTION 1 POSITIONING
// -------------------------------------------------------------
@Composable
fun MapIsland(
    level: Level,
    isUnlocked: Boolean,
    stars: Int,
    floatOffset: Float,
    onClick: () -> Unit
) {
    // SOLUTION 1: Specific positions for nice winding path
    val pos = when (level.order) {
        1 -> Offset(0.15f, 0.65f)   // Bottom left - First island
        2 -> Offset(0.35f, 0.45f)   // Middle left - slightly up
        3 -> Offset(0.55f, 0.25f)   // Upper middle - going up
        4 -> Offset(0.75f, 0.35f)   // Upper right - slight down
        5 -> Offset(0.95f, 0.55f)   // Right side - going down
        6 -> Offset(1.15f, 0.40f)   // Far right - middle height
        7 -> Offset(1.35f, 0.60f)   // Further right - lower
        8 -> Offset(1.55f, 0.30f)   // Even further - up again
        else -> {
            // Fallback for any additional levels
            // Creates a diagonal pattern
            val row = (level.order - 1) / 4
            val col = (level.order - 1) % 4
            Offset(
                x = 0.15f + col * 0.25f + (row % 2) * 0.12f,
                y = 0.65f - row * 0.20f
            )
        }
    }

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

                    if (!isUnlocked) {
                        Text("Locked", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // -------- ISLAND IMAGE FROM BACKEND --------
            if (!level.islandImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = level.islandImageUrl,
                    contentDescription = "Island ${level.title}",
                    modifier = Modifier.size(280.dp),
                    contentScale = ContentScale.Fit,
                    alpha = if (isUnlocked) 1f else 0.5f
                )
            } else {
                // Fallback placeholder when no island image
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50).copy(alpha = if (isUnlocked) 0.8f else 0.4f),
                                    Color(0xFF2E7D32).copy(alpha = if (isUnlocked) 0.6f else 0.3f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏝️",
                        fontSize = 80.sp,
                        modifier = Modifier.alpha(if (isUnlocked) 1f else 0.5f)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// BACKGROUND
// -------------------------------------------------------------
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
            contentScale = ContentScale.FillBounds
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

// -------------------------------------------------------------
// DIALOGS
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
    avatarImageUrl: String?,
    avatarName: String?,
    fallbackEmoji: String = "🎹",
    totalStars: Int,
    maxStars: Int,
    isLoggedIn: Boolean,
    accentColor: Color,
    onAvatarClick: () -> Unit
) {
    val displayImageUrl = avatarImageUrl ?: userPhotoUrl
    val starProgress = if (maxStars <= 0) 0f else totalStars.toFloat() / maxStars.toFloat()
    val headerGradient = listOf(
        accentColor.mixWith(Color.Black, 0.4f),
        accentColor,
        accentColor.mixWith(Color.White, 0.3f)
    )
    val borderColor = accentColor.mixWith(Color.White, 0.4f)
    val badgeColor = accentColor.mixWith(Color.White, 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(brush = Brush.linearGradient(headerGradient))
                .border(1.5.dp, borderColor.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(2.dp, borderColor, CircleShape)
                            .clickable(onClick = onAvatarClick),
                        contentAlignment = Alignment.Center
                    ) {
                        if (displayImageUrl != null) {
                            AsyncImage(
                                model = displayImageUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(text = fallbackEmoji, fontSize = 32.sp)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (userName.isBlank()) "Music Explorer" else userName,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = when {
                                !isLoggedIn -> "Guest mode • tap avatar to sign in"
                                    avatarName != null -> "Avatar: $avatarName"
                                    else -> "Tap avatar to open your quick menu"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Star Journey",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                    Text(
                        text = "$totalStars / $maxStars",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                    LinearProgressIndicator(
                        progress = starProgress.coerceIn(0f, 1f),
                        modifier = Modifier
                            .width(120.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50)),
                        color = badgeColor,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

private val avatarAccentPalette = listOf(
    RainbowRed,
    RainbowOrange,
    RainbowYellow,
    RainbowGreen,
    RainbowBlue,
    RainbowIndigo,
    RainbowViolet,
    RainbowPink
)

private val namedAccentColors = mapOf(
    "red" to RainbowRed,
    "orange" to RainbowOrange,
    "yellow" to RainbowYellow,
    "green" to RainbowGreen,
    "blue" to RainbowBlue,
    "indigo" to RainbowIndigo,
    "violet" to RainbowViolet,
    "purple" to RainbowViolet,
    "pink" to RainbowPink,
    "magenta" to RainbowPink,
    "teal" to RainbowGreen.mixWith(RainbowBlue, 0.5f),
    "aqua" to RainbowGreen.mixWith(RainbowBlue, 0.6f)
)

private fun String.toColorOrNull(): Color? {
    val raw = trim()
    if (raw.isEmpty()) return null

    namedAccentColors[raw.lowercase()]?.let { return it }

    val hexCandidates = buildList {
        add(raw)
        if (!raw.startsWith("#")) add("#$raw")
        if (raw.startsWith("0x", ignoreCase = true)) {
            add("#" + raw.removePrefix("0x"))
            add("#" + raw.removePrefix("0X"))
        }
    }.distinct()

    for (candidate in hexCandidates) {
        try {
            return Color(android.graphics.Color.parseColor(candidate))
        } catch (_: IllegalArgumentException) {
            // try next option
        }
    }

    return null
}

private fun String.toAccentPaletteColor(): Color {
    if (avatarAccentPalette.isEmpty()) return RainbowIndigo
    val index = hashCode().absoluteValue % avatarAccentPalette.size
    return avatarAccentPalette[index]
}

private fun Color.mixWith(target: Color, fraction: Float): Color {
    val clamped = fraction.coerceIn(0f, 1f)
    return androidx.compose.ui.graphics.lerp(this, target, clamped)
}

private fun Avatar.accentIdColor(): Color? {
    val source = avatarImageUrl?.takeIf { it.isNotBlank() }
        ?: name
        ?: id
    return source?.toAccentPaletteColor()
}

private fun AvatarCustomization?.accentColor(): Color? {
    this ?: return null
    val prioritizedColors = listOfNotNull(
        clothingColor,
        accessories?.firstOrNull(),
        hairColor,
        eyeColor
    )
    for (value in prioritizedColors) {
        val parsed = value.toColorOrNull()
        if (parsed != null) return parsed
    }
    return null
}