package com.pianokids.game.view.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.data.repository.LevelRepository
import com.pianokids.game.data.models.Avatar
import com.pianokids.game.data.models.KidProfile
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.viewmodel.AuthViewModel
import com.pianokids.game.viewmodel.AvatarViewModel
import com.pianokids.game.utils.components.AvatarsSection
import com.pianokids.game.utils.components.AvatarCreationDialog
import com.pianokids.game.utils.components.AvatarDetailDialog
import com.pianokids.game.utils.components.AvatarImageView
import com.pianokids.game.utils.components.AIAvatarPreviewDialog
import com.pianokids.game.utils.components.KidFriendlyErrorDialog
import com.pianokids.game.data.models.AvatarGenerationResponse
import com.pianokids.game.data.repository.SublevelProgressRepository
import kotlin.math.sin
import kotlin.math.cos
import android.graphics.Color as AndroidColor

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val authRepository = remember { AuthRepository(context) }
    val socialLoginManager = remember { SocialLoginManager(context) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showCreateAvatarDialog by remember { mutableStateOf(false) }
    var showAvatarDetailDialog by remember { mutableStateOf(false) }
    var showAIPreviewDialog by remember { mutableStateOf(false) }
    var showAIErrorDialog by remember { mutableStateOf(false) }
    var pendingAIAvatar by remember { mutableStateOf<AvatarGenerationResponse?>(null) }
    var pendingAIAvatarName by remember { mutableStateOf("") }
    var pendingPrompt by remember { mutableStateOf("") }
    var pendingStyle by remember { mutableStateOf("") }
    var aiErrorMessage by remember { mutableStateOf("") }

    val authViewModel: AuthViewModel = viewModel()
    val avatarViewModel: AvatarViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Avatar states
    val avatars by avatarViewModel.avatars.collectAsState()
    val activeAvatar by avatarViewModel.activeAvatar.collectAsState()
    val isLoadingAvatars by avatarViewModel.isLoading.collectAsState()
    val isGeneratingAI by avatarViewModel.isGeneratingAI.collectAsState()
    val avatarError by avatarViewModel.error.collectAsState()
    val aiGenerationResponse by avatarViewModel.aiGenerationResponse.collectAsState()

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // sublevel stars
    val maxStarsState = remember { mutableStateOf(0) }

    // Load avatars when logged in
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            avatarViewModel.loadUserAvatars()
        }
    }
    
    // Listen for AI generation success and show preview
    LaunchedEffect(aiGenerationResponse) {
        aiGenerationResponse?.let { response ->
            android.util.Log.d("ProfileScreen", "🎉 AI Generation Response Received!")
            android.util.Log.d("ProfileScreen", "Avatar Name: ${response.name}")
            android.util.Log.d("ProfileScreen", "Description: ${response.aiGeneratedDescription}")
            android.util.Log.d("ProfileScreen", "Image URL: ${response.avatarImageUrl}")
            
            pendingAIAvatar = response
            showAIPreviewDialog = true
            android.util.Log.d("ProfileScreen", "✅ Preview dialog should show now: $showAIPreviewDialog")
            avatarViewModel.clearAIGenerationResponse()
        }
    }

    // Show error if any (for AI avatar generation errors)
    LaunchedEffect(avatarError) {
        avatarError?.let { error ->
            // Check if this is an AI generation error
            if (error.contains("generate", ignoreCase = true) || 
                error.contains("appropriate", ignoreCase = true) ||
                error.contains("Bad Request", ignoreCase = true)) {
                // Show kid-friendly error dialog
                aiErrorMessage = error
                showAIErrorDialog = true
                snackbarHostState.currentSnackbarData?.dismiss()
            } else {
                // Show regular snackbar for other errors
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Error: $error",
                        duration = SnackbarDuration.Short
                    )
                }
            }
            avatarViewModel.clearError()
        }
    }

    val userName by authViewModel.userName.collectAsState()
    val user = userPrefs.getUser()
    var kidProfile by remember { mutableStateOf(userPrefs.getKidProfile()) }

    val kidEmailAlias = kidProfile?.uniqueName?.let { "$it@pianokids.fun" }

    val userEmail = when {
        user != null -> user.email
        isLoggedIn -> userPrefs.getEmail() ?: ""
        kidProfile != null -> kidEmailAlias ?: ""
        else -> "Guest Mode 🎮"
    }

    val userPhotoUrl = user?.photoUrl
    val userProvider = when {
        user != null -> user.provider
        kidProfile != null -> "kid_profile"
        else -> null
    }

    val userLevel = remember { mutableStateOf(
        when {
            user != null -> user.level
            isLoggedIn -> userPrefs.getLevel()
            else -> 1
        }
    ) }

    val totalStars = remember { mutableStateOf(0) }

    // Auto-refresh level and stars periodically while on screen
    // ⭐ Correct stars logic using sublevels
    LaunchedEffect(Unit) {
        val repo = LevelRepository()
        val subProgRepo = SublevelProgressRepository()

        while (true) {
            // 1) Load all levels
            val levels = repo.getAllLevels() ?: emptyList()

            // 2) For each level, load sublevels & compute stars
            var earned = 0
            var available = 0

            for (level in levels) {
                val sublevels = subProgRepo.getUserSublevels(
                    user?.id ?: "guest",
                    level._id
                ) ?: emptyList()

                earned += sublevels.sumOf { it.starsEarned }
                available += sublevels.sumOf { it.maxStars }
            }

            // Update reactive states
            if (earned != totalStars.value) totalStars.value = earned
            if (available != maxStarsState.value) maxStarsState.value = available

            kotlinx.coroutines.delay(1500)
        }
    }

    val maxStars = 24
    val scrollState = rememberScrollState()

    LaunchedEffect(activeAvatar?.id, activeAvatar?.avatarImageUrl, kidProfile?.uniqueName) {
        val currentKid = kidProfile ?: return@LaunchedEffect
        val latestAvatarUrl = activeAvatar?.avatarImageUrl ?: userPrefs.getAvatarThumbnail()
        if (!latestAvatarUrl.isNullOrBlank() && currentKid.backendAvatarImageUrl != latestAvatarUrl) {
            val updatedKid = currentKid.copy(
                backendAvatarId = activeAvatar?.id ?: currentKid.backendAvatarId,
                backendAvatarName = activeAvatar?.name ?: currentKid.backendAvatarName,
                backendAvatarImageUrl = latestAvatarUrl
            )
            kidProfile = updatedKid
            userPrefs.saveKidProfile(updatedKid)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2),
                        Color(0xFFF093FB),
                        Color(0xFFFFD3A5)
                    )
                )
            )
    ) {
        // Enhanced animated background
        AnimatedKidsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar
            FunTopBar(
                onNavigateBack = {
                    SoundManager.playClick()
                    onNavigateBack()
                },
                onSettings = {
                    SoundManager.playClick()
                    showSettingsDialog = true
                },
                onLogout = if (isLoggedIn) {
                    {
                        SoundManager.playClick()
                        showLogoutDialog = true
                    }
                } else null
            )

            Spacer(Modifier.height(20.dp))

            // Profile Avatar with Fun Animation
            PlayfulProfileAvatar(
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                userProvider = userProvider,
                kidProfile = kidProfile,
                activeAvatar = activeAvatar,
                userPrefs = userPrefs,
                onClick = {
                    SoundManager.playClick()
                    showAvatarDetailDialog = true
                }
            )

            Spacer(Modifier.height(24.dp))

            // Name Section with Bouncy Edit Button
            KidsNameSection(
                userName = userName,
                userProvider = userProvider,
                onEditClick = {
                    SoundManager.playClick()
                    showEditNameDialog = true
                },
                kidProfile = kidProfile
            )

            Spacer(Modifier.height(16.dp))

            // Level Badge
            KidsLevelBadge(userLevel = userLevel.value)

            // Guest Mode Banner
            if (!isLoggedIn) {
                Spacer(Modifier.height(12.dp))
                GuestModeBanner()
            }

            Spacer(Modifier.height(24.dp))

            // Fun Stats Cards
            KidsStatsCards(
                userLevel = userLevel.value,
                totalStars = totalStars.value,
                maxStars = maxStarsState.value
            )

            Spacer(Modifier.height(24.dp))

            // Achievements with Animation
            KidsAchievementsSection(
                totalStars = totalStars.value,
                userLevel = userLevel.value
            )

            Spacer(Modifier.height(24.dp))

            // Account Info Card - Kid Friendly
            KidsAccountInfoCard(
                userName = userName,
                userEmail = userEmail,
                userProvider = userProvider,
                totalStars = totalStars.value,
                maxStars = maxStarsState.value,
                userLevel = userLevel.value,
                kidProfile = kidProfile
            )

            Spacer(Modifier.height(24.dp))

            // Avatars Section
            if (isLoggedIn) {
                KidsAvatarsSection(
                    avatars = avatars,
                    activeAvatar = activeAvatar,
                    isLoading = isLoadingAvatars,
                    onCreateAvatar = {
                        SoundManager.playClick()
                        showCreateAvatarDialog = true
                    },
                    onSelectAvatar = { avatar ->
                        SoundManager.playClick()
                        avatarViewModel.setActiveAvatar(avatar.id)
                    },
                    onDeleteAvatar = { avatar ->
                        SoundManager.playClick()
                        avatarViewModel.deleteAvatar(avatar.id)
                    }
                )

                Spacer(Modifier.height(24.dp))
            }

            Spacer(Modifier.height(24.dp))
        }

        // Edit Name Dialog
        if (showEditNameDialog) {
            KidsEditNameDialog(
                currentName = userName,
                onDismiss = { showEditNameDialog = false },
                onConfirm = { newName ->
                    userPrefs.saveFullName(newName)
                    authViewModel.updateUserName(newName)
                    showEditNameDialog = false
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(onDismiss = { showSettingsDialog = false })
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

                    coroutineScope.launch {
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
                        
                        coroutineScope.launch {
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
                    
                    coroutineScope.launch {
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
                    
                    coroutineScope.launch {
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "💡 Tip: Complex prompts may take longer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = { /* No button while loading */ }
            )
        }
        
        // Kid-Friendly Error Dialog
        if (showAIErrorDialog) {
            KidFriendlyErrorDialog(
                errorMessage = aiErrorMessage,
                onDismiss = {
                    showAIErrorDialog = false
                    aiErrorMessage = ""
                },
                onTryAgain = {
                    showAIErrorDialog = false
                    aiErrorMessage = ""
                    showCreateAvatarDialog = true
                }
            )
        }

        // Avatar Detail Dialog
        if (showAvatarDetailDialog) {
            AvatarDetailDialog(
                avatar = activeAvatar,
                onDismiss = { showAvatarDetailDialog = false }
            )
        }

        // Logout Dialog
        if (showLogoutDialog) {
            KidsLogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    authRepository.logout()
                    socialLoginManager.signOutGoogle()
                    socialLoginManager.signOutFacebook()
                    userPrefs.clearKidProfile()
                    authViewModel.onLogout()
                    onLogout()
                }
            )
        }

        // Fun Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { snackbarData ->
            KidsFunSnackbar(snackbarData)
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 🎨 KIDS-FRIENDLY COMPONENTS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun AnimatedKidsBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    // Multiple floating elements
    val float1 by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "float1"
    )
    val float2 by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "float2"
    )
    val float3 by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "float3"
    )

    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Floating stars
        drawCircle(
            Color.White.copy(alpha = 0.8f),
            12f,
            center = Offset(width * 0.1f, height * float1 * 0.3f)
        )
        drawCircle(
            Color(0xFFFFEB3B).copy(alpha = 0.7f),
            10f,
            center = Offset(width * 0.85f, height * 0.2f + float2 * 50f)
        )
        drawCircle(
            Color(0xFFFF4081).copy(alpha = 0.6f),
            8f,
            center = Offset(width * 0.3f, height * 0.15f + float3 * 40f)
        )

        // Draw some fun shapes
        drawCircle(
            Color(0xFFFFFFFF).copy(alpha = 0.1f),
            100f,
            center = Offset(width * 0.9f, height * 0.1f)
        )
        drawCircle(
            Color(0xFFFFFFFF).copy(alpha = 0.08f),
            150f,
            center = Offset(width * 0.1f, height * 0.8f)
        )
    }
}

@Composable
fun FunTopBar(
    onNavigateBack: () -> Unit,
    onSettings: () -> Unit,
    onLogout: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button with Bounce
        BouncyButton(
            onClick = onNavigateBack,
            backgroundColor = Color.White.copy(alpha = 0.9f),
            icon = Icons.Default.ArrowBack,
            contentDescription = "Go Back"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Settings Button
            BouncyButton(
                onClick = onSettings,
                backgroundColor = Color(0xFFFFEB3B).copy(alpha = 0.9f),
                icon = Icons.Default.Settings,
                contentDescription = "Settings"
            )

            // Logout Button (if logged in)
            if (onLogout != null) {
                BouncyButton(
                    onClick = onLogout,
                    backgroundColor = Color(0xFFFF4081).copy(alpha = 0.9f),
                    icon = Icons.Default.ExitToApp,
                    contentDescription = "Logout"
                )
            }
        }
    }
}

@Composable
fun BouncyButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    icon: ImageVector,
    contentDescription: String
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable {
                isPressed = true
                onClick()
            }
            .border(3.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFF667EEA),
            modifier = Modifier.size(30.dp)
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
fun PlayfulProfileAvatar(
    userName: String,
    userPhotoUrl: String?,
    userProvider: String?,
    kidProfile: KidProfile?,
    activeAvatar: Avatar?,
    userPrefs: UserPreferences,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )
    val scale by infiniteTransition.animateFloat(
        1f, 1.1f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(160.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Rotating colorful ring
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFFF6B6B),
                        Color(0xFFFFEB3B),
                        Color(0xFF4CAF50),
                        Color(0xFF2196F3),
                        Color(0xFFE91E63),
                        Color(0xFFFF6B6B)
                    )
                ),
                radius = radius,
                center = center,
                alpha = 0.3f
            )
        }

        // Avatar Image
        Box(
            modifier = Modifier
                .size(140.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFEB3B),
                            Color(0xFFFF4081)
                        )
                    )
                )
                .border(5.dp, Color.White, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            val kidAvatarUrl = kidProfile?.backendAvatarImageUrl
            val avatarUrl = when {
                !kidAvatarUrl.isNullOrBlank() -> kidAvatarUrl
                kidProfile != null -> null
                else -> activeAvatar?.avatarImageUrl ?: userPrefs.getAvatarThumbnail()
            }

            if (!kidAvatarUrl.isNullOrBlank()) {
                AvatarImageView(
                    avatarUrl = kidAvatarUrl,
                    size = 140,
                    borderColor = Color.Transparent,
                    borderWidth = 0
                )
            } else if (kidProfile != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(kidProfile.avatarColorHex.toColorOrDefault(Color(0xFF7E57C2))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = kidProfile.avatarEmoji,
                        fontSize = 56.sp
                    )
                }
            } else if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                AvatarImageView(
                    avatarUrl = avatarUrl,
                    size = 140,
                    borderColor = Color.Transparent,
                    borderWidth = 0
                )
            } else if (userPhotoUrl != null) {
                AsyncImage(
                    model = userPhotoUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = userName.take(2).uppercase(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

   
    }
}





@Composable
fun KidsNameSection(
    userName: String,
    userProvider: String?,
    onEditClick: () -> Unit,
    kidProfile: KidProfile?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .border(3.dp, Brush.linearGradient(
                colors = listOf(Color(0xFFFFEB3B), Color(0xFFFF4081))
            ), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "👋",
                    fontSize = 32.sp
                )

                Column {
                    Text(
                        text = "Hi there!",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    if (kidProfile != null) {
                        Text(
                            text = "Âge : ${kidProfile.age} ans",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        )
                    }
                }

                 if (userProvider != null) {
                    FunProviderBadge(provider = userProvider)
                }
            }

            // Edit Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEB3B))
                    .clickable(onClick = onEditClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun FunProviderBadge(provider: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (provider.lowercase()) {
            "google" -> Color(0xFF4285F4)
            "facebook" -> Color(0xFF1877F2)
            else -> Color.Gray
        },
        modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = when (provider.lowercase()) {
                    "google" -> "G"
                    "facebook" -> "f"
                    else -> provider.take(1).uppercase()
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun KidsLevelBadge(userLevel: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge")
    val shimmer by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "shimmer"
    )

    Card(
        modifier = Modifier
            .border(3.dp, Brush.linearGradient(
                colors = listOf(Color(0xFF4CAF50), Color(0xFF2196F3))
            ), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = getLevelEmoji(userLevel),
                fontSize = 36.sp,
                modifier = Modifier.scale(1f + shimmer * 0.2f)
            )

            Column {
                Text(
                    text = "Your Level",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = getLevelTitle(userLevel),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun GuestModeBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .border(3.dp, Color(0xFFFFEB3B), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEB3B).copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "🎮", fontSize = 28.sp)
            Text(
                text = "Guest Mode - Progress not saved!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun KidsStatsCards(
    userLevel: Int,
    totalStars: Int,
    maxStars: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FunStatsCard(
            emoji = "🏆",
            title = "Level",
            value = userLevel.toString(),
            backgroundColor = Color(0xFFFFEB3B),
            modifier = Modifier.weight(1f)
        )

        FunStatsCard(
            emoji = "⭐",
            title = "Stars",
            value = "$totalStars/$maxStars",
            backgroundColor = Color(0xFFFF4081),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun FunStatsCard(
    emoji: String,
    title: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stats")
    val bounce by infiniteTransition.animateFloat(
        0f, 10f,
        infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "bounce"
    )

    Card(
        modifier = modifier
            .border(3.dp, Brush.linearGradient(
                colors = listOf(backgroundColor.copy(alpha = 0.8f), backgroundColor)
            ), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Emoji with bounce
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = (-bounce).dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 36.sp)
            }

            Text(
                title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 28.sp
                )
            )
        }
    }
}

@Composable
fun KidsAchievementsSection(totalStars: Int, userLevel: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .border(3.dp, Brush.linearGradient(
                colors = listOf(Color(0xFFFF9800), Color(0xFFFFEB3B))
            ), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🏅", fontSize = 36.sp)
                Text(
                    "Your Achievements",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    val starsNeeded = (index + 1) * 5
                    val earned = totalStars >= starsNeeded
                    AnimatedAchievementStar(earned = earned, index = index)
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Progress text
            Text(
                text = "$totalStars / 25 stars earned",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Progress bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Keep going! You're doing great! 🎉",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun RowScope.AnimatedAchievementStar(earned: Boolean, index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "star$index")
    val scale by infiniteTransition.animateFloat(
        1f, if (earned) 1.15f else 1f,
        infiniteRepeatable(
            tween((1000 + index * 200), delayMillis = index * 300),
            RepeatMode.Reverse
        ),
        label = "scale$index"
    )
    
    val rotation by infiniteTransition.animateFloat(
        0f, if (earned) 360f else 0f,
        infiniteRepeatable(
            tween(3000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "rotation$index"
    )

    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = if (earned) Color(0xFFFFD700) else Color.LightGray,
            modifier = Modifier
                .size(56.dp)
                .scale(scale)
                .rotate(if (earned) rotation else 0f)
                .shadow(
                    elevation = if (earned) 8.dp else 0.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFFFFEB3B),
                    spotColor = Color(0xFFFF9800)
                )
        )
    }
}

@Composable
fun KidsAccountInfoCard(
    userName: String,
    userEmail: String,
    userProvider: String?,
    totalStars: Int,
    maxStars: Int,
    userLevel: Int,
    kidProfile: KidProfile?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .border(3.dp, Brush.linearGradient(
                colors = listOf(Color(0xFF667EEA), Color(0xFFE91E63))
            ), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF667EEA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "My Info",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.3f),
                thickness = 2.dp
            )

            // Details with fun icons
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FunInfoRow(emoji = "👤", label = "Name", value = userName)
                FunInfoRow(emoji = "📧", label = if (kidProfile != null) "Nom unique" else "Email", value = userEmail)
                kidProfile?.let {
                    FunInfoRow(emoji = "🎂", label = "Âge", value = "${it.age} ans")
                }
                if (userProvider != null) {
                    FunInfoRow(
                        emoji = "🔐",
                        label = "Login",
                        value = userProvider.replaceFirstChar { it.uppercase() }
                    )
                }
                FunInfoRow(
                    emoji = "🎯",
                    label = "Progress",
                    value = "$totalStars/$maxStars stars"
                )
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.3f),
                thickness = 2.dp
            )

            // Level summary with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = getLevelEmoji(userLevel),
                            fontSize = 32.sp
                        )
                        Text(
                            text = getLevelTitle(userLevel),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = "Level $userLevel",
                            color = Color(0xFF667EEA),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FunInfoRow(emoji: String, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFFFFEB3B).copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun KidsAvatarsSection(
    avatars: List<Avatar>,
    activeAvatar: Avatar?,
    isLoading: Boolean,
    onCreateAvatar: () -> Unit,
    onSelectAvatar: (Avatar) -> Unit,
    onDeleteAvatar: (Avatar) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .border(3.dp, Brush.linearGradient(
                colors = listOf(Color(0xFF2196F3), Color(0xFF00BCD4))
            ), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            AvatarsSection(
                avatars = avatars,
                activeAvatar = activeAvatar,
                isLoading = isLoading,
                onCreateAvatar = onCreateAvatar,
                onSelectAvatar = onSelectAvatar,
                onDeleteAvatar = onDeleteAvatar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun KidsEditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEB3B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(40.dp)
                )
            }
        },
        title = {
            Text(
                text = "Change Your Name! ✏️",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Color(0xFF667EEA)
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "What would you like to be called?",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        color = Color.Gray
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        errorMessage = when {
                            it.isBlank() -> "Oops! Name can't be empty!"
                            it.length < 2 -> "Name needs at least 2 letters!"
                            it.length > 30 -> "That's too long! Max 30 letters!"
                            else -> null
                        }
                    },
                    label = { Text("Your Name") },
                    placeholder = { Text("Enter your awesome name!") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    leadingIcon = {
                        Text("👤", fontSize = 24.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667EEA),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (errorMessage == null && newName.isNotBlank()) {
                        SoundManager.playClick()
                        onConfirm(newName.trim())
                    }
                },
                enabled = errorMessage == null && newName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("✅", fontSize = 20.sp)
                    Text(
                        text = "Save Changes!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    SoundManager.playClick()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Cancel",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun KidsLogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFFF4081)),
                contentAlignment = Alignment.Center
            ) {
                Text("👋", fontSize = 48.sp)
            }
        },
        title = {
            Text(
                "See You Soon! 🌟",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Color(0xFF667EEA)
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "Are you sure you want to leave?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    color = Color.Gray
                ),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    SoundManager.playClick()
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF4081)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("👋", fontSize = 20.sp)
                    Text(
                        "Yes, Logout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    SoundManager.playClick()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Stay Here!",
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun KidsFunSnackbar(snackbarData: SnackbarData) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}



fun getLevelTitle(level: Int): String = when (level) {
    1 -> " Beginner"
    in 2..3 -> "🎶 Learner"
    in 4..5 -> "🎵 Player"
    in 6..7 -> "⭐ Skilled"
    in 8..10 -> "🏆 Expert"
    else -> "👑 Master"
}

fun getLevelEmoji(level: Int): String = when (level) {
    1 -> "🎹"
    in 2..3 -> "🎶"
    in 4..5 -> "🎵"
    in 6..7 -> "⭐"
    in 8..10 -> "🏆"
    else -> "👑"
}

private fun String.toColorOrDefault(default: Color): Color = try {
    Color(AndroidColor.parseColor(this))
} catch (_: IllegalArgumentException) {
    default
}

