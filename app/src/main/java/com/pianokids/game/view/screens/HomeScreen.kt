package com.pianokids.game.view.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.pianokids.game.R
import com.pianokids.game.data.models.Level
import com.pianokids.game.data.models.UnlockedLevelItem
import com.pianokids.game.data.models.getEffectivePosition
import com.pianokids.game.data.models.KidProfile
import com.pianokids.game.data.models.Avatar
import com.pianokids.game.data.models.AvatarCustomization
import com.pianokids.game.data.models.LevelHistoryEntry
import com.pianokids.game.data.repository.LevelRepository
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.data.repository.SublevelProgressRepository
import com.pianokids.game.data.repository.SublevelRepository
import com.pianokids.game.data.repository.LevelHistoryRepository
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.ImageMapper
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.utils.PianoSoundManager
import com.pianokids.game.viewmodel.AuthViewModel
import com.pianokids.game.viewmodel.AvatarViewModel
import com.pianokids.game.utils.components.AvatarCreationDialog
import com.pianokids.game.utils.components.AIAvatarPreviewDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.math.sin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    onNavigateToKaraoke: () -> Unit = {},
    onNavigateToMiniGames: () -> Unit = {}
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
    var showLoginDialog by remember { mutableStateOf(false) }
    var showCreateAvatarDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val historyRepository = remember { LevelHistoryRepository() }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var historyEntries by remember { mutableStateOf<List<LevelHistoryEntry>>(emptyList()) }
    var historyLoading by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf<String?>(null) }
    var selectedHistoryEntry by remember { mutableStateOf<LevelHistoryEntry?>(null) }
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
            val isNotFound = error.contains("404", ignoreCase = true) || error.contains("not found", ignoreCase = true)
            if (!isNotFound) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Error: $error",
                        duration = SnackbarDuration.Short
                    )
                }
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

    val userId = user?.id

    LaunchedEffect(showHistoryDialog, userId) {
        if (showHistoryDialog && !userId.isNullOrBlank()) {
            historyLoading = true
            historyError = null
            val entries = historyRepository.getHistory(userId)
            historyEntries = entries ?: emptyList()
            if (entries == null) {
                historyError = "Unable to load history"
            }
            historyLoading = false
        }
    }

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
        if (isLoggedIn) {
            showLoginDialog = false
        }
    }

    // ----- LOAD LEVELS FROM BACKEND -----
    // ----- LOAD LEVELS + UNLOCK STATE FROM BACKEND (SUBLEVEL-BASED) -----
    LaunchedEffect(isLoggedIn) {
        isLoading = true

        // 1️⃣ Load all levels
        val allLevels = levelRepository.getAllLevels() ?: emptyList()

        // Fix missing or zero `order`
        levels = allLevels.mapIndexed { index, level ->
            if (level.order == 0) level.copy(order = index + 1) else level
        }.sortedBy { it.order }

        if (!isLoggedIn) {
            progressMap = levels.associate { level ->
                level._id to UnlockedLevelItem(
                    levelId = level._id,
                    title = level.title,
                    theme = level.theme,
                    unlocked = false,
                    starsUnlocked = 0,
                    backgroundUrl = level.backgroundUrl,
                    bossUrl = level.bossUrl,
                    musicUrl = level.musicUrl
                )
            }

            totalStars = 0
            maxStars = 0
            isLoading = false
            showLoginDialog = true
            return@LaunchedEffect
        }

        if (userId.isNullOrBlank()) {
            progressMap = levels.associate { level ->
                level._id to UnlockedLevelItem(
                    levelId = level._id,
                    title = level.title,
                    theme = level.theme,
                    unlocked = false,
                    starsUnlocked = 0,
                    backgroundUrl = level.backgroundUrl,
                    bossUrl = level.bossUrl,
                    musicUrl = level.musicUrl
                )
            }

            totalStars = 0
            maxStars = 0
            isLoading = false
            showLoginDialog = false
            return@LaunchedEffect
        }

        val safeUserId = userId!!

        // Logged-in user → use SUBLEVEL PROGRESS to drive everything
        var sumStars = 0
        var sumMaxStars = 0
        val mapBuilder = mutableMapOf<String, UnlockedLevelItem>()

        for (level in levels) {
            // Get all sublevels for this level from backend (enriched with unlocked, stars, etc.)
            val sublevels = sublevelProgRepository.getUserSublevels(safeUserId, level._id) ?: emptyList()

            val earnedForLevel = sublevels.sumOf { it.starsEarned }
            val possibleForLevel = sublevels.sumOf { it.maxStars }

            sumStars += earnedForLevel
            sumMaxStars += possibleForLevel

            val isHiddenMelody = level.title.contains("hidden", ignoreCase = true)

            // 🔑 Unlock logic: Level 1 always open; others unlock when they have progress.
            val isUnlockedForLevel = when {
                level.order == 1 -> true                           // Level 1 always unlocked
                sublevels.isEmpty() -> false                       // no data → keep locked
                isHiddenMelody -> sublevels.any { it.unlocked && it.starsEarned > 0 }
                else -> sublevels.any { it.unlocked }
            }

            mapBuilder[level._id] = UnlockedLevelItem(
                levelId = level._id,
                title = level.title,
                theme = level.theme,
                unlocked = isUnlockedForLevel,
                starsUnlocked = earnedForLevel,                    // stars earned in this level
                backgroundUrl = level.backgroundUrl,
                bossUrl = level.bossUrl,
                musicUrl = level.musicUrl
            )
        }

        progressMap = mapBuilder
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
                onHistoryClick = {
                    if (!isLoggedIn || userId.isNullOrBlank()) {
                        showLoginDialog = true
                        return@HomeProfileDrawer
                    }
                    scope.launch { drawerState.close() }
                    showHistoryDialog = true
                },
                onMiniGamesClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToMiniGames()
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
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    showSettingsDialog = true
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
                                    !isLoggedIn -> showLoginDialog = true
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

        if (showComingSoonDialog) {
            ComingSoonDialog { showComingSoonDialog = false }
        }

        if (showSettingsDialog) {
            com.pianokids.game.utils.components.SettingsDialog(
                onDismiss = { showSettingsDialog = false }
            )
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

        if (showHistoryDialog) {
            LevelHistoryDialog(
                entries = historyEntries,
                isLoading = historyLoading,
                errorMessage = historyError,
                onDismissRequest = { showHistoryDialog = false },
                onEntrySelected = {
                    selectedHistoryEntry = it
                    showHistoryDialog = false
                }
            )
        }

        selectedHistoryEntry?.let { entry ->
            LevelHistoryPlaybackDialog(
                entry = entry,
                onDismiss = {
                    PianoSoundManager.stopAllSounds()
                    selectedHistoryEntry = null
                }
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
    onHistoryClick: () -> Unit,
    onMiniGamesClick: () -> Unit,
    onAddAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val drawerBackground = accentColor.mixWith(Color.Black, 0.55f)
    val avatarFrameColor = accentColor.mixWith(Color.White, 0.25f)
    val drawerScroll = rememberScrollState()

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
                .padding(horizontal = 20.dp, vertical = 32.dp)
                .verticalScroll(drawerScroll),
            verticalArrangement = Arrangement.Top
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
                    icon = Icons.Default.History,
                    label = "Historic",
                    description = "Replay your latest runs",
                    accentColor = RainbowViolet
                ) {
                    SoundManager.playClick()
                    onHistoryClick()
                }

                DrawerActionButton(
                    icon = Icons.Default.SportsEsports,
                    label = "Mini Games",
                    description = "Learn notes with fun games",
                    accentColor = RainbowGreen
                ) {
                    SoundManager.playClick()
                    onMiniGamesClick()
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

                DrawerActionButton(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    description = "Music, sound, vibration",
                    accentColor = RainbowYellow
                ) {
                    SoundManager.playClick()
                    onSettingsClick()
                }
            }
            Spacer(Modifier.height(24.dp))
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


@Composable
private fun LevelHistoryDialog(
    entries: List<LevelHistoryEntry>,
    isLoading: Boolean,
    errorMessage: String?,
    onDismissRequest: () -> Unit,
    onEntrySelected: (LevelHistoryEntry) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF667EEA).copy(alpha = 0.95f),
                            Color(0xFF764BA2).copy(alpha = 0.95f)
                        )
                    ),
                    RoundedCornerShape(32.dp)
                ),
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with fun emojis
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎮",
                        fontSize = 32.sp
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "My Gaming History",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "🎵 Tap to replay your amazing performances! 🎵", 
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close history",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 4.dp,
                                    modifier = Modifier.size(60.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading your memories...",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFFF8A80).copy(alpha = 0.2f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontSize = 14.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    entries.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🎼",
                                fontSize = 48.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = "No memories yet!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Play a level to start recording your performances!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(entries.size) { index ->
                                val entry = entries[index]
                                val starEmoji = when (entry.stars) {
                                    3 -> "⭐⭐⭐"
                                    2 -> "⭐⭐"
                                    1 -> "⭐"
                                    else -> "✨"
                                }
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.15f),
                                                    Color.White.copy(alpha = 0.08f)
                                                )
                                            ),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .clickable { onEntrySelected(entry) }
                                        .border(
                                            width = 2.dp,
                                            color = Color.White.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 0.dp
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp)
                                    ) {
                                        // Title with emoji
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "🎹 ${entry.levelTitle}",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = Color.White
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = entry.sublevelTitle ?: "Challenge",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color.White.copy(alpha = 0.75f),
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }
                                            Text(
                                                text = starEmoji,
                                                fontSize = 20.sp
                                            )
                                        }
                                        
                                        Divider(
                                            color = Color.White.copy(alpha = 0.2f),
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                        
                                        // Stats row with icons
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Duration
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(text = "⏱️", fontSize = 14.sp)
                                                Text(
                                                    text = formatDurationMs(entry.durationMs),
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }
                                            
                                            // Mistakes
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(text = "🎯", fontSize = 14.sp)
                                                Text(
                                                    text = "${entry.wrongNotes} oops${if (entry.wrongNotes != 1) "es" else ""}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.weight(1f))
                                            
                                            // Date
                                            Text(
                                                text = "📅 ${formatHistoryTimestamp(entry.createdAt)}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White.copy(alpha = 0.65f),
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "👆 Tap a session to replay and hear your awesome performance!",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LevelHistoryPlaybackDialog(
    entry: LevelHistoryEntry,
    onDismiss: () -> Unit
) {
    val playbackScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF764BA2).copy(alpha = 0.95f),
                                Color(0xFF667EEA).copy(alpha = 0.95f)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button (top right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close playback",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Title Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎬 Ready to watch your performance! 🎬",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🎹 ${entry.levelTitle}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = entry.sublevelTitle ?: "Amazing Challenge",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    )
                }

                // Stats Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Duration card
                    StatsCard(
                        emoji = "⏱️",
                        label = "Time",
                        value = formatDurationMs(entry.durationMs)
                    )
                    
                    // Stars card
                    StatsCard(
                        emoji = "⭐",
                        label = "Score",
                        value = "${entry.stars} Star${if (entry.stars != 1) "s" else ""}"
                    )
                    
                    // Mistakes card
                    StatsCard(
                        emoji = "🎯",
                        label = "Oops",
                        value = "${entry.wrongNotes} time${if (entry.wrongNotes != 1) "s" else ""}"
                    )
                }

                // Notes info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFFFD700).copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎵",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Recorded ${entry.notes.size} amazing note${if (entry.notes.size != 1) "s" else ""}!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // Play Button
                Button(
                    onClick = {
                        if (isPlaying || entry.notes.isEmpty()) return@Button
                        playbackScope.launch {
                            isPlaying = true
                            entry.notes.forEachIndexed { index, note ->
                                val playbackNote = resolvePlaybackNoteName(note)
                                playbackNote?.let { PianoSoundManager.playNote(it) }
                                val delayMs = entry.noteDurations.getOrNull(index)
                                    ?.times(1000f)?.toLong()?.coerceIn(120L, 2500L)
                                    ?: 400L
                                delay(delayMs)
                            }
                            isPlaying = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = entry.notes.isNotEmpty() && !isPlaying
                ) {
                    if (isPlaying) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "🎵 Now Playing...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(
                            "▶️ Play My Recording",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Info text
                Text(
                    text = "Close the dialog to stop playback",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun RowScope.StatsCard(
    emoji: String,
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .background(
                Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
    }
}

private val historyDisplayFormatter = SimpleDateFormat("MMM d • HH:mm", Locale.getDefault())
private val historyIsoParsers = listOf(
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
).onEach {
    it.timeZone = TimeZone.getTimeZone("UTC")
}

private fun formatHistoryTimestamp(raw: String): String {
    val parsed = historyIsoParsers.asSequence().mapNotNull { parser ->
        try {
            parser.parse(raw)
        } catch (_: Exception) {
            null
        }
    }.firstOrNull()

    return if (parsed != null) {
        historyDisplayFormatter.format(parsed)
    } else raw
}

private fun formatDurationMs(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun resolvePlaybackNoteName(note: String): String? {
    return when (note.lowercase()) {
        "do" -> "Do"
        "re" -> "Ré"
        "mi" -> "Mi"
        "fa" -> "Fa"
        "sol" -> "Sol"
        "la" -> "La"
        "si" -> "Si"
        "do#", "reb" -> "Do#"
        "re#", "mib" -> "Ré#"
        "fa#", "solb" -> "Fa#"
        "sol#", "lab" -> "Sol#"
        "la#", "sib" -> "La#"
        "dob", "si#" -> "Si"
        "fab" -> "Mi"
        else -> note.replaceFirstChar { it.uppercaseChar() }
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
    // 🔥 KEEPING YOUR ORIGINAL POSITIONING (unchanged)
    val pos = when (level.order) {
        1 -> Offset(0.15f, 0.65f)
        2 -> Offset(0.35f, 0.45f)
        3 -> Offset(0.55f, 0.25f)
        4 -> Offset(0.75f, 0.35f)
        5 -> Offset(0.95f, 0.55f)
        6 -> Offset(1.15f, 0.40f)
        7 -> Offset(1.35f, 0.60f)
        8 -> Offset(1.55f, 0.30f)
        else -> {
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

            // ----------------------------------------------------
            // 🎨 NEW CUTE LEVEL CARD UI
            // ----------------------------------------------------
            CuteLevelCard(
                level = level,
                isUnlocked = isUnlocked,
                stars = stars,
                onClick = onClick
            )

            Spacer(Modifier.height(6.dp))

            // ----------------------------------------------------
            // 🏝️ ISLAND IMAGE (unchanged)
            // ----------------------------------------------------
            if (!level.islandImageUrl.isNullOrEmpty()) {
                Image(
                    painter = painterResource(
                        id = ImageMapper.islandImage(level.theme)
                    ),
                    contentDescription = "Island",
                    modifier = Modifier.size(280.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF4CAF50).copy(alpha = if (isUnlocked) 0.8f else 0.4f),
                                    Color(0xFF2E7D32).copy(alpha = if (isUnlocked) 0.6f else 0.3f)
                                )
                            ),
                            CircleShape
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


// new level card

@Composable
fun CuteLevelCard(
    level: Level,
    isUnlocked: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    // 🎨 Color palettes per level (fun, bright, kid-friendly)
    val colors = when (level.order) {
        1 -> listOf(Color(0xFFFFCDD2), Color(0xFFE57373))
        2 -> listOf(Color(0xFFFFE0B2), Color(0xFFFFB74D))
        3 -> listOf(Color(0xFFFFF9C4), Color(0xFFFFF176))
        4 -> listOf(Color(0xFFC8E6C9), Color(0xFF81C784))
        5 -> listOf(Color(0xFFBBDEFB), Color(0xFF64B5F6))
        6 -> listOf(Color(0xFFE1BEE7), Color(0xFFBA68C8))
        else -> listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E))
    }

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp)
            .shadow(18.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(colors))
            .border(
                width = 4.dp,
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // 🎵 Emoji (from ImageMapper)
            Text(
                text = ImageMapper.levelEmoji(level.theme),
                fontSize = 32.sp
            )

            // 📛 Level title
            Text(
                text = level.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = Color.White
            )

        }

        // 🔒 Locked overlay
        if (!isUnlocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clip(RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
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
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.ocean)
                .decoderFactory(if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoderDecoder.Factory()
                } else {
                    GifDecoder.Factory()
                })
                .build(),
            contentDescription = "Animated Ocean",
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

// -------------------------------------------------------------
// DIALOGS
// -------------------------------------------------------------
@Composable
fun ComingSoonDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF222741),
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                "🔒 Level Locked!",
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Earn more stars in earlier missions to unlock this level!",
                    color = Color(0xFFE0E0E0),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
            }
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
                                !isLoggedIn -> "Sign in to keep your progress"
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