package com.pianokids.game.view.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.content.res.Configuration
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.pianokids.game.R
import com.pianokids.game.data.models.KidProfile
import com.pianokids.game.data.models.AvatarGenerationResponse
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.ui.theme.OceanDeep
import com.pianokids.game.ui.theme.OceanLight
import com.pianokids.game.ui.theme.RainbowYellow
import com.pianokids.game.ui.theme.SkyBlue
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UniqueNameLoginManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.utils.components.AnimatedOceanWithIslands
import com.pianokids.game.utils.components.AIAvatarPreviewDialog
import com.pianokids.game.utils.components.AvatarCreationDialog
import com.pianokids.game.utils.components.SettingsDialog
import com.pianokids.game.viewmodel.AuthViewModel
import com.pianokids.game.viewmodel.AvatarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.blur
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi

private enum class OnboardingStep { AccountChoice, ReturningLogin, UniqueName, ProfileDetails, Greeting }

private data class AvatarOption(
    val emoji: String,
    val label: String,
    val color: Color
)

@Composable
fun WelcomeScreen(
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val authRepository = remember { AuthRepository(context) }
    val socialLoginManager = remember { SocialLoginManager(context) }
    val uniqueNameLoginManager = remember { UniqueNameLoginManager(context) }
    val scope = rememberCoroutineScope()
    val authViewModel: AuthViewModel = viewModel()
    val avatarViewModel: AvatarViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val avatarError by avatarViewModel.error.collectAsState()
    val activeAvatar by avatarViewModel.activeAvatar.collectAsState()
    val isGeneratingAI by avatarViewModel.isGeneratingAI.collectAsState()
    val aiGenerationResponse by avatarViewModel.aiGenerationResponse.collectAsState()

    var showIntro by remember { mutableStateOf(!userPrefs.getSeenWelcome()) }

    var showSettings by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var activeKidProfile by remember { mutableStateOf(userPrefs.getKidProfile()) }
    var backendAvatarId by remember { mutableStateOf(activeKidProfile?.backendAvatarId) }
    var backendAvatarName by remember { mutableStateOf(activeKidProfile?.backendAvatarName) }
    var backendAvatarImageUrl by remember { mutableStateOf(activeKidProfile?.backendAvatarImageUrl) }
    var awaitingBackendAvatar by remember { mutableStateOf(false) }
    var isSavingAvatar by remember { mutableStateOf(false) }
    var hasAutoNavigated by remember { mutableStateOf(false) }
    var hasAutoLoginAttempted by remember { mutableStateOf(false) }
    var showCreateAvatarDialog by remember { mutableStateOf(false) }
    var showAIPreviewDialog by remember { mutableStateOf(false) }
    var pendingAIAvatar by remember { mutableStateOf<AvatarGenerationResponse?>(null) }
    var pendingAIAvatarName by remember { mutableStateOf("") }
    var pendingPrompt by remember { mutableStateOf("") }
    var pendingStyle by remember { mutableStateOf("cartoon") }

    var currentStep by remember {
        mutableStateOf(
            if (activeKidProfile != null) OnboardingStep.Greeting else OnboardingStep.AccountChoice
        )
    }

    var uniqueName by remember { mutableStateOf(activeKidProfile?.uniqueName ?: "") }
    var uniqueNameError by remember { mutableStateOf<String?>(null) }
    var kidName by remember { mutableStateOf(activeKidProfile?.displayName ?: "") }
    var kidAgeInput by remember { mutableStateOf(activeKidProfile?.age?.toString() ?: "") }
    val avatarPalette = remember { defaultAvatarOptions() }
    var selectedAvatar by remember {
        mutableStateOf(
            activeKidProfile?.let { avatarFromProfile(it, avatarPalette) } ?: avatarPalette.first()
        )
    }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var profileError by remember { mutableStateOf<String?>(null) }
    var returningUniqueName by remember { mutableStateOf("") }
    var returningError by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        socialLoginManager.handleGoogleSignInResult(task)
    }

    LaunchedEffect(Unit) {
        SoundManager.startBackgroundMusic()
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            avatarViewModel.loadActiveAvatar()
        }
    }

    LaunchedEffect(activeKidProfile?.backendAvatarId, activeKidProfile?.backendAvatarImageUrl, activeKidProfile?.backendAvatarName) {
        backendAvatarId = activeKidProfile?.backendAvatarId
        backendAvatarImageUrl = activeKidProfile?.backendAvatarImageUrl
        backendAvatarName = activeKidProfile?.backendAvatarName
    }

    LaunchedEffect(aiGenerationResponse) {
        aiGenerationResponse?.let { response ->
            pendingAIAvatar = response
            showAIPreviewDialog = true
            avatarViewModel.clearAIGenerationResponse()
        }
    }

    LaunchedEffect(avatarError) {
        avatarError?.let { error ->
            val isNotFound = error.contains("404", ignoreCase = true) || error.contains("not found", ignoreCase = true)
            awaitingBackendAvatar = false
            isSavingAvatar = false
            if (isNotFound) {
                backendAvatarId = null
                backendAvatarName = null
                backendAvatarImageUrl = null
                activeKidProfile = activeKidProfile?.copy(
                    backendAvatarId = null,
                    backendAvatarName = null,
                    backendAvatarImageUrl = null
                )
                selectedAvatar = avatarPalette.first()
                avatarViewModel.clearError()
                return@LaunchedEffect
            }
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            avatarViewModel.clearError()
        }
    }

    LaunchedEffect(activeAvatar?.id, isLoggedIn) {
        val avatar = activeAvatar ?: return@LaunchedEffect
        if (!isLoggedIn) return@LaunchedEffect

        backendAvatarId = avatar.id
        backendAvatarName = avatar.name
        backendAvatarImageUrl = avatar.avatarImageUrl

        val currentProfile = activeKidProfile ?: userPrefs.getKidProfile()
        currentProfile?.let { profile ->
            val updatedProfile = profile.copy(
                backendAvatarId = avatar.id,
                backendAvatarName = avatar.name,
                backendAvatarImageUrl = avatar.avatarImageUrl
            )
            activeKidProfile = updatedProfile
            userPrefs.saveKidProfile(updatedProfile)
        }

        if (awaitingBackendAvatar) {
            awaitingBackendAvatar = false
            isSavingAvatar = false
            Toast.makeText(context, "Avatar saved!", Toast.LENGTH_SHORT).show()
        }
    }

    fun loginExistingProfile(profile: KidProfile, autoNavigateToHome: Boolean = false) {
        val aliasEmail = uniqueNameLoginManager.buildKidEmail(profile.uniqueName)
        isLoading = true
        scope.launch {
            try {
                val result = authRepository.loginWithDevUser(
                    email = aliasEmail,
                    name = profile.displayName
                )

                result.onSuccess {
                    uniqueNameLoginManager.saveProfile(userPrefs, profile)
                    userPrefs.saveFullName(profile.displayName)
                    userPrefs.clearGuestMode()
                    userPrefs.setSeenWelcome(true)
                    authViewModel.onLoginSuccess()
                    avatarViewModel.loadActiveAvatar()
                    activeKidProfile = profile
                    currentStep = OnboardingStep.Greeting
                    returningError = null
                    profileError = null
                    Toast.makeText(context, "Welcome ${profile.displayName}!", Toast.LENGTH_SHORT).show()
                    if (autoNavigateToHome) {
                        delay(150)
                        hasAutoNavigated = true
                        onNavigateToHome()
                    }
                }.onFailure { error ->
                    returningError = error.message ?: "We can't locate that profile."
                    Toast.makeText(context, returningError, Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(activeKidProfile?.uniqueName, isLoggedIn) {
        val profile = activeKidProfile ?: return@LaunchedEffect

        if (!isLoggedIn && !hasAutoLoginAttempted) {
            hasAutoLoginAttempted = true
            loginExistingProfile(profile, autoNavigateToHome = true)
            return@LaunchedEffect
        }

        if (isLoggedIn && userPrefs.getSeenWelcome() && !hasAutoNavigated) {
            hasAutoNavigated = true
            onNavigateToHome()
        }
    }

    fun buildKidProfileOrNull(): KidProfile? {
        val normalizedUniqueName = uniqueName.trim()
        if (normalizedUniqueName.isEmpty()) {
            profileError = "Pick a unique name."
            currentStep = OnboardingStep.UniqueName
            return null
        }

        val ageValue = kidAgeInput.toIntOrNull()
        if (ageValue == null || ageValue !in 3..15) {
            profileError = "Age must be between 3 and 15."
            return null
        }

        val display = kidName.ifBlank { normalizedUniqueName.replaceFirstChar { it.uppercase() } }
        profileError = null

        return KidProfile(
            uniqueName = normalizedUniqueName,
            displayName = display,
            age = ageValue,
            avatarEmoji = selectedAvatar.emoji,
            avatarColorHex = selectedAvatar.color.toHexString(),
            backendAvatarId = backendAvatarId,
            backendAvatarName = backendAvatarName,
            backendAvatarImageUrl = backendAvatarImageUrl
        )
    }

    fun persistKidProfile(profile: KidProfile, onReady: (() -> Unit)? = null) {
        val normalizedUniqueName = profile.uniqueName
        val reservedByCurrent = activeKidProfile?.uniqueName.equals(normalizedUniqueName, true)
        val alreadyTaken = uniqueNameLoginManager.isNameTaken(normalizedUniqueName)
        if (alreadyTaken && !reservedByCurrent) {
            profileError = "That magic name is already in use. Try another one."
            currentStep = OnboardingStep.UniqueName
            return
        }

        val aliasEmail = uniqueNameLoginManager.buildKidEmail(normalizedUniqueName)

        if (isLoggedIn && reservedByCurrent) {
            uniqueNameLoginManager.saveProfile(userPrefs, profile)
            userPrefs.saveFullName(profile.displayName)
            userPrefs.clearGuestMode()
            userPrefs.setSeenWelcome(true)
            activeKidProfile = profile
            currentStep = OnboardingStep.Greeting
            profileError = null
            onReady?.invoke()
            return
        }

        isLoading = true
        scope.launch {
            try {
                val result = authRepository.loginWithDevUser(
                    email = aliasEmail,
                    name = profile.displayName
                )

                result.onSuccess {
                    if (activeKidProfile != null && !reservedByCurrent) {
                        uniqueNameLoginManager.releaseName(activeKidProfile!!.uniqueName)
                    }
                    uniqueNameLoginManager.saveProfile(userPrefs, profile)
                    userPrefs.saveFullName(profile.displayName)
                    userPrefs.clearGuestMode()
                    userPrefs.setSeenWelcome(true)
                    authViewModel.onLoginSuccess()
                    avatarViewModel.loadActiveAvatar()
                    activeKidProfile = profile
                    currentStep = OnboardingStep.Greeting
                    profileError = null
                    Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
                    onReady?.invoke()
                }.onFailure { error ->
                    profileError = error.message ?: "We can't save right now."
                    Toast.makeText(context, profileError, Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun ensureProfileThenLaunchAvatar() {
        if (!isLoggedIn || activeKidProfile == null) {
            val profile = buildKidProfileOrNull() ?: return
            persistKidProfile(profile) { showCreateAvatarDialog = true }
            return
        }
        showCreateAvatarDialog = true
    }

    val scrollState = rememberScrollState()

    if (showIntro) {
        FirstRunOnboarding {
            userPrefs.setSeenWelcome(true)
            showIntro = false
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OceanDeep, OceanLight)))
    ) {
        AnimatedOceanWithIslands()
        WelcomeLogoTopLeft()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            KidWelcomeCard(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(0.85f)
                    .padding(vertical = 12.dp),
                step = currentStep,
                uniqueName = uniqueName,
                uniqueNameError = uniqueNameError,
                onUniqueNameChange = {
                    uniqueNameError = null
                    uniqueName = it.trimStart()
                },
                onUniqueNameConfirm = {
                    SoundManager.playClick()
                    val validationMessage = uniqueNameLoginManager.validateUniqueName(uniqueName)
                    if (validationMessage != null) {
                        uniqueNameError = validationMessage
                        return@KidWelcomeCard
                    }
                    val normalized = uniqueName.trim()
                    val alreadyTaken = uniqueNameLoginManager.isNameTaken(normalized)
                    val reservedByCurrent = activeKidProfile?.uniqueName.equals(normalized, true)
                    if (alreadyTaken && !reservedByCurrent) {
                        uniqueNameError = "That magic name is already in use. Try another one."
                        return@KidWelcomeCard
                    }
                    kidName = kidName.ifBlank { normalized.replaceFirstChar { it.uppercase() } }
                    currentStep = OnboardingStep.ProfileDetails
                },
                kidName = kidName,
                onKidNameChange = {
                    kidName = it
                    profileError = null
                },
                kidAgeInput = kidAgeInput,
                onAgeChange = {
                    kidAgeInput = it.filter { char -> char.isDigit() }.take(2)
                    profileError = null
                },
                profileError = profileError,
                selectedAvatar = selectedAvatar,
                backendAvatarImageUrl = backendAvatarImageUrl,
                backendAvatarName = backendAvatarName,
                onAvatarClick = {
                    SoundManager.playClick()
                    showAvatarPicker = true
                },
                onLaunchAvatarCreator = {
                    SoundManager.playClick()
                    ensureProfileThenLaunchAvatar()
                },
                onCreateProfile = {
                    SoundManager.playClick()
                    val profile = buildKidProfileOrNull() ?: return@KidWelcomeCard
                    persistKidProfile(profile)
                },
                onContinue = {
                    SoundManager.playClick()
                    userPrefs.setSeenWelcome(true)
                    onNavigateToHome()
                },
                kidProfile = activeKidProfile,
                isAvatarSaving = isSavingAvatar || awaitingBackendAvatar,
                onCreateMagicAvatar = {
                    SoundManager.playClick()
                    ensureProfileThenLaunchAvatar()
                },
                onChooseExisting = {
                    SoundManager.playClick()
                    returningError = null
                    returningUniqueName = ""
                    currentStep = OnboardingStep.ReturningLogin
                },
                onChooseNew = {
                    SoundManager.playClick()
                    currentStep = OnboardingStep.UniqueName
                },
                returningUniqueName = returningUniqueName,
                onReturningNameChange = {
                    returningUniqueName = it.trimStart()
                    returningError = null
                },
                onReturningLogin = {
                    val normalized = returningUniqueName.trim()
                    if (normalized.isEmpty()) {
                        returningError = "Enter your magic name."
                        return@KidWelcomeCard
                    }
                    val storedProfile = uniqueNameLoginManager.getStoredProfile(normalized)
                        ?: userPrefs.getKidProfile()?.takeIf { it.uniqueName.equals(normalized, true) }

                    val fallbackProfile = storedProfile ?: run {
                        val defaultAvatar = avatarPalette.first()
                        KidProfile(
                            uniqueName = normalized,
                            displayName = normalized.replaceFirstChar { it.uppercase() },
                            age = 8,
                            avatarEmoji = defaultAvatar.emoji,
                            avatarColorHex = defaultAvatar.color.toHexString(),
                            backendAvatarId = null,
                            backendAvatarName = null,
                            backendAvatarImageUrl = null
                        )
                    }

                    loginExistingProfile(fallbackProfile, autoNavigateToHome = true)
                },
                returningError = returningError,
                onBackToChoice = {
                    SoundManager.playClick()
                    currentStep = OnboardingStep.AccountChoice
                },
                onGoogleClick = {
                    SoundManager.playClick()
                    isLoading = true
                    socialLoginManager.signInWithGoogle(
                        launcher = googleSignInLauncher,
                        onSuccess = { idToken ->
                            scope.launch {
                                val result = authRepository.loginWithSocial(idToken, "google")
                                isLoading = false
                                result.onSuccess {
                                    authViewModel.onLoginSuccess()
                                    userPrefs.setSeenWelcome(true)
                                    userPrefs.clearGuestMode()
                                    Toast.makeText(context, "Signed in!", Toast.LENGTH_SHORT).show()
                                    delay(200)
                                    onNavigateToHome()
                                }.onFailure {
                                    Toast.makeText(context, it.message ?: "Error", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onFailure = {
                            isLoading = false
                            Toast.makeText(context, it.message ?: "Google unavailable", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                onDevLogin = {
                    SoundManager.playClick()
                    isLoading = true
                    scope.launch {
                        val result = authRepository.loginWithDevUser(
                            email = "dev@pianokids.local",
                            name = "Dev User"
                        )
                        isLoading = false
                        result.onSuccess {
                            authViewModel.onLoginSuccess()
                            userPrefs.setSeenWelcome(true)
                            userPrefs.clearGuestMode()
                            Toast.makeText(context, "Dev login ok", Toast.LENGTH_SHORT).show()
                            delay(200)
                            onNavigateToHome()
                        }.onFailure {
                            Toast.makeText(context, it.message ?: "Erreur dev", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onChangeProfile = {
                    SoundManager.playClick()
                    activeKidProfile?.let { profile ->
                        uniqueNameLoginManager.releaseName(profile.uniqueName)
                    }
                    userPrefs.clearKidProfile()
                    userPrefs.clearAvatarThumbnail()
                    activeKidProfile = null
                    uniqueName = ""
                    kidName = ""
                    kidAgeInput = ""
                    selectedAvatar = avatarPalette.first()
                    backendAvatarId = null
                    backendAvatarName = null
                    backendAvatarImageUrl = null
                    awaitingBackendAvatar = false
                    isSavingAvatar = false
                    pendingAIAvatar = null
                    showAIPreviewDialog = false
                    hasAutoNavigated = false
                    hasAutoLoginAttempted = false
                    currentStep = OnboardingStep.AccountChoice
                }
            )
        }

        IconButton(
            onClick = {
                SoundManager.playClick()
                showSettings = true
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .zIndex(1f)
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RainbowYellow)
            }
        }
    }

    if (showSettings) {
        SettingsDialog(onDismiss = { showSettings = false })
    }

    if (showAvatarPicker) {
        AvatarPickerDialog(
            options = avatarPalette,
            onDismiss = { showAvatarPicker = false },
            onSelect = {
                selectedAvatar = it
                showAvatarPicker = false
            }
        )
    }

    if (showCreateAvatarDialog) {
        AvatarCreationDialog(
            onDismiss = { showCreateAvatarDialog = false },
            onCreateAvatar = { name, avatarImageUrl ->
                showCreateAvatarDialog = false
                awaitingBackendAvatar = true
                isSavingAvatar = true
                avatarViewModel.createAvatar(name, avatarImageUrl)
            },
            onCreateAvatarWithAI = { name, prompt, style ->
                showCreateAvatarDialog = false
                pendingAIAvatarName = name
                pendingPrompt = prompt
                pendingStyle = style
                avatarViewModel.generateAvatarFromPrompt(prompt, name, style)
            }
        )
    }

    if (showAIPreviewDialog && pendingAIAvatar != null) {
        AIAvatarPreviewDialog(
            avatarName = pendingAIAvatarName.ifBlank { kidName.ifBlank { uniqueName.ifBlank { "Avatar" } } },
            generationResponse = pendingAIAvatar!!,
            onSave = {
                pendingAIAvatar?.previewData?.let {
                    awaitingBackendAvatar = true
                    isSavingAvatar = true
                    avatarViewModel.saveAIAvatar(it)
                }
                showAIPreviewDialog = false
                pendingAIAvatar = null
            },
            onRegenerate = {
                showAIPreviewDialog = false
                pendingAIAvatar = null
                avatarViewModel.generateAvatarFromPrompt(pendingPrompt, pendingAIAvatarName, pendingStyle)
            },
            onDismiss = {
                showAIPreviewDialog = false
                pendingAIAvatar = null
            },
            isSaving = isSavingAvatar
        )
    }

    if (isGeneratingAI) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Building your AI avatar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(56.dp),
                        color = Color(0xFF667EEA),
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = "We are painting your magical avatar...",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "It usually takes 10-30 seconds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun KidWelcomeCard(
    modifier: Modifier = Modifier,
    step: OnboardingStep,
    uniqueName: String,
    uniqueNameError: String?,
    onUniqueNameChange: (String) -> Unit,
    onUniqueNameConfirm: () -> Unit,
    kidName: String,
    onKidNameChange: (String) -> Unit,
    kidAgeInput: String,
    onAgeChange: (String) -> Unit,
    profileError: String?,
    selectedAvatar: AvatarOption,
    backendAvatarImageUrl: String?,
    backendAvatarName: String?,
    onAvatarClick: () -> Unit,
    onLaunchAvatarCreator: () -> Unit,
    onCreateProfile: () -> Unit,
    onContinue: () -> Unit,
    kidProfile: KidProfile?,
    isAvatarSaving: Boolean,
    onCreateMagicAvatar: () -> Unit,
    onChooseExisting: () -> Unit,
    onChooseNew: () -> Unit,
    returningUniqueName: String,
    onReturningNameChange: (String) -> Unit,
    onReturningLogin: () -> Unit,
    returningError: String?,
    onBackToChoice: () -> Unit,
    onGoogleClick: () -> Unit,
    onDevLogin: () -> Unit,
    onChangeProfile: () -> Unit
) {
    val outerShape = RoundedCornerShape(40.dp)
    val innerShape = RoundedCornerShape(32.dp)

    Card(
        modifier = modifier
            .padding(vertical = 16.dp),
        shape = outerShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = 3.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF667EEA),
                    Color(0xFF00D9FF),
                    Color(0xFFFFC857)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(innerShape)
        ) {
            // 🔹 Blurred glass background ONLY
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.10f)
                            )
                        )
                    )
                    .blur(22.dp) // this uses Compose modifier, works with minSdk 24
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.30f),
                        innerShape
                    )
            )

            // 🔹 Foreground content (NOT blurred)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                KidStepIndicator(currentStep = step)
                when (step) {
                    OnboardingStep.AccountChoice -> AccountChoiceStep(
                        onChooseExisting = onChooseExisting,
                        onChooseNew = onChooseNew
                    )

                    OnboardingStep.ReturningLogin -> ReturningLoginStep(
                        uniqueName = returningUniqueName,
                        onUniqueNameChange = onReturningNameChange,
                        onSubmit = onReturningLogin,
                        errorMessage = returningError,
                        onBack = onBackToChoice
                    )

                    OnboardingStep.UniqueName -> UniqueNameStep(
                        uniqueName = uniqueName,
                        errorMessage = uniqueNameError,
                        onUniqueNameChange = onUniqueNameChange,
                        onSubmit = onUniqueNameConfirm,
                        onBack = onBackToChoice,
                        onGoogleClick = onGoogleClick,
                        onDevLogin = onDevLogin
                    )

                    OnboardingStep.ProfileDetails -> ProfileDetailsStep(
                        selectedAvatar = selectedAvatar,
                        backendAvatarImageUrl = backendAvatarImageUrl,
                        backendAvatarName = backendAvatarName,
                        onAvatarClick = onAvatarClick,
                        onLaunchAvatarCreator = onLaunchAvatarCreator,
                        kidName = kidName,
                        onKidNameChange = onKidNameChange,
                        kidAgeInput = kidAgeInput,
                        onAgeChange = onAgeChange,
                        errorMessage = profileError,
                        onCreateProfile = onCreateProfile
                    )

                    OnboardingStep.Greeting -> GreetingStep(
                        kidProfile = kidProfile,
                        isAvatarSaving = isAvatarSaving,
                        onCreateMagicAvatar = onCreateMagicAvatar,
                        onContinue = onContinue,
                        onChangeProfile = onChangeProfile
                    )
                }
            }
        }
    }
}


@Composable
private fun UniqueNameStep(
    uniqueName: String,
    errorMessage: String?,
    onUniqueNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onGoogleClick: () -> Unit,
    onDevLogin: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign up",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Pick a unique name to keep your progress safe.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uniqueName,
                onValueChange = onUniqueNameChange,
                placeholder = { Text("Magic name") },
                singleLine = true,
                isError = errorMessage != null,
                leadingIcon = { Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFFFFC857)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp)
            )
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFFFB4AB),
                style = MaterialTheme.typography.labelLarge
            )
        }
        KidPrimaryButton(
            text = "SIGN UP",
            onClick = onSubmit,
            enabled = uniqueName.isNotBlank()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f))
            Text("or", color = Color.White.copy(alpha = 0.8f))
            HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f))
        }
        KidSocialButton(
            text = "Continue with Google",
            iconRes = R.drawable.ic_google,
            onClick = onGoogleClick
        )
        TextButton(onClick = onDevLogin) {
            Text(
                "Developer login",
                color = Color.White.copy(alpha = 0.8f),
                textDecoration = TextDecoration.Underline
            )
        }
        TextButton(onClick = onBack) {
            Text(
                "← Back",
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AccountChoiceStep(
    onChooseExisting: () -> Unit,
    onChooseNew: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Do you already have a magic account?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Choose an option to continue.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        KidPrimaryButton(
            text = "Yes, I have one",
            onClick = onChooseExisting
        )
        KidPrimaryButton(
            text = "No, make a new profile",
            onClick = onChooseNew
        )
    }
}

@Composable
private fun ReturningLoginStep(
    uniqueName: String,
    onUniqueNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    errorMessage: String?,
    onBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Type your magic name",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "We'll load your adventure.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uniqueName,
                onValueChange = onUniqueNameChange,
                placeholder = { Text("Magic name") },
                singleLine = true,
                isError = errorMessage != null,
                leadingIcon = { Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF67E8F9)) },
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(22.dp))
            )
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFFFB4AB),
                style = MaterialTheme.typography.labelLarge
            )
        }
        KidPrimaryButton(text = "ENTER THE WORLD", onClick = onSubmit, enabled = uniqueName.isNotBlank())
        TextButton(onClick = onBack) {
            Text(
                "← Back",
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ProfileDetailsStep(
    selectedAvatar: AvatarOption,
    backendAvatarImageUrl: String?,
    backendAvatarName: String?,
    onAvatarClick: () -> Unit,
    onLaunchAvatarCreator: () -> Unit,
    kidName: String,
    onKidNameChange: (String) -> Unit,
    kidAgeInput: String,
    onAgeChange: (String) -> Unit,
    errorMessage: String?,
    onCreateProfile: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create a profile",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "We tune the adventure to your age.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        KidAvatarBadge(
            option = selectedAvatar,
            imageUrl = backendAvatarImageUrl,
            onClick = onAvatarClick
        )
        OutlinedTextField(
            value = kidName,
            onValueChange = onKidNameChange,
            label = { Text("First name") },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = kidAgeInput,
            onValueChange = onAgeChange,
            label = { Text("Age") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFFFB4AB),
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (backendAvatarImageUrl != null) {
            Text(
                text = "You already have a magical avatar${backendAvatarName?.let { ": $it" } ?: "" }.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            OutlinedButton(
                onClick = onLaunchAvatarCreator,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Switch magical avatar ✨")
            }
        } else {
            Text(
                text = "You can also create an AI or 3D avatar to replace your animal friend.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            OutlinedButton(
                onClick = onLaunchAvatarCreator,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Create a magical avatar ✨")
            }
        }

        KidPrimaryButton(
            text = "SAVE",
            onClick = onCreateProfile,
            enabled = kidName.isNotBlank() && kidAgeInput.isNotBlank()
        )
    }
}

@Composable
private fun GreetingStep(
    kidProfile: KidProfile?,
    isAvatarSaving: Boolean,
    onCreateMagicAvatar: () -> Unit,
    onContinue: () -> Unit,
    onChangeProfile: () -> Unit
) {
    val name = kidProfile?.displayName ?: "friend"
    val emailAlias = kidProfile?.uniqueName?.let { "$it@pianokids.fun" }
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome ${name.lowercase()}!",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            textAlign = TextAlign.Center
        )
        if (kidProfile != null) {
            KidAvatarBadge(
                option = avatarFromProfile(kidProfile),
                imageUrl = kidProfile.backendAvatarImageUrl,
                onClick = { }
            )
            Text(
                text = "Magic name: ${kidProfile.uniqueName}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Age: ${kidProfile.age}",
                style = MaterialTheme.typography.bodyLarge
            )
            emailAlias?.let {
                Text(
                    text = "Magic email: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
            val hasMagicAvatar = !kidProfile.backendAvatarImageUrl.isNullOrEmpty()
            Text(
                text = if (hasMagicAvatar) "Your magical avatar is ready!" else "Create a magical avatar before you start.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onCreateMagicAvatar,
                enabled = !isAvatarSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasMagicAvatar) Color(0xFF7B1FA2) else Color(0xFF43A047),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    text = if (hasMagicAvatar) "Switch magical avatar" else "Create a magical avatar ✨",
                    fontWeight = FontWeight.Bold
                )
            }
            if (isAvatarSaving) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Text(
                text = "Personnalisons ton aventure musicale.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onCreateMagicAvatar,
                enabled = !isAvatarSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) {
                Text("Create a magical avatar ✨", fontWeight = FontWeight.Bold)
            }
        }
        KidPrimaryButton(text = "CONTINUE", onClick = onContinue)
        TextButton(onClick = onChangeProfile) {
            Text(
                "Switch profile",
                color = Color.White.copy(alpha = 0.8f),
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
private fun KidStepIndicator(currentStep: OnboardingStep) {
    val steps = listOf(
        OnboardingStep.AccountChoice,
        OnboardingStep.ReturningLogin,
        OnboardingStep.UniqueName,
        OnboardingStep.ProfileDetails,
        OnboardingStep.Greeting
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEach { step ->
            val isActive = step == currentStep
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1.2f else 1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 120f),
                label = "indicator"
            )
            Box(
                modifier = Modifier
                    .size(if (isActive) 18.dp else 12.dp)
                    .scale(scale)
                    .background(
                        color = if (isActive) Color(0xFFFFC107) else Color.White.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )
        }
    }
}

private data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val footer: String,
    val emoji: String,
    val accent: Color,
    val gradient: List<Color>,
    val imageRes: Int,
    val themeTag: String
)

@Composable
private fun FirstRunOnboarding(onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val slides = listOf(
        OnboardingSlide(
            title = "Welcome to Melody Island!",
            subtitle = "Play with our mascot and start your musical adventure.",
            footer = "Rainbow keys, fluffy clouds, and endless music magic.",
            emoji = "🎹",
            accent = Color(0xFF6A5AE0),
            gradient = listOf(Color(0xFFB3E5FC), Color(0xFFFFF1F3)),
            imageRes = R.drawable.cat_logo,
            themeTag = "Rainbow Keys"
        ),
        OnboardingSlide(
            title = "Play Songs You Love",
            subtitle = "Cartoons, heroes, and favorite tunes are ready to learn!",
            footer = "Pick a cape, put on headphones, and tap the beat.",
            emoji = "🎧",
            accent = Color(0xFFFF8A65),
            gradient = listOf(Color(0xFFE1BEE7), Color(0xFFFFE0B2)),
            imageRes = R.drawable.spiderman,
            themeTag = "Hero Beats"
        ),
        OnboardingSlide(
            title = "Become a Piano Star",
            subtitle = "Collect trophies, level up, and surprise your family!",
            footer = "Shiny coins, sparkling badges, and big spotlights await.",
            emoji = "🏆",
            accent = Color(0xFFFFC107),
            gradient = listOf(Color(0xFFB2EBF2), Color(0xFFFFF9C4)),
            imageRes = R.drawable.batman,
            themeTag = "Star Stage"
        )
    )

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { slides.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFB3E5FC), Color(0xFFFDF5FF)),
                    center = Offset(0.35f, 0.45f),
                    radius = 1400f
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        OnboardingFloaters()
        // floating hero stickers for playful background
        Image(
            painter = painterResource(R.drawable.batman),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(140.dp)
                .padding(8.dp)
                .graphicsLayer(alpha = 0.16f, rotationZ = 8f),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(R.drawable.spiderman),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(160.dp)
                .padding(12.dp)
                .graphicsLayer(alpha = 0.16f, rotationZ = -6f),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(R.drawable.ironman),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(120.dp)
                .padding(8.dp)
                .graphicsLayer(alpha = 0.12f, rotationZ = 4f),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDone) {
                    Text("Skip", color = Color(0xFF1B2559), fontWeight = FontWeight.Bold)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { index ->
                        val isActive = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .width(if (isActive) 20.dp else 10.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isActive) slides[index].accent else Color.White.copy(alpha = 0.6f))
                        )
                    }
                }
                Button(
                    onClick = {
                        if (pagerState.currentPage < slides.lastIndex) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onDone()
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5AE0)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(if (pagerState.currentPage == slides.lastIndex) "Let’s play!" else "Next", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                val slide = slides[page]
                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OnboardingSlideCard(slide, modifier = Modifier.weight(1f))
                        OnboardingIllustration(slide, modifier = Modifier.weight(1f))
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OnboardingIllustration(slide, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        OnboardingSlideCard(slide, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun OnboardingSlideCard(slide: OnboardingSlide, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.verticalGradient(slide.gradient))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(slide.themeTag, color = Color.White) },
                        leadingIcon = { Text(slide.emoji) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = slide.accent.copy(alpha = 0.3f))
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = slide.accent.copy(alpha = 0.9f)
                    )
                }

                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B2559)
                    ),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = slide.subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF2F3A6A)),
                    textAlign = TextAlign.Start
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = slide.accent.copy(alpha = 0.14f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(slide.emoji, fontSize = 22.sp)
                        Column {
                            Text(
                                text = slide.footer,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF1B2559),
                                    fontStyle = FontStyle.Italic
                                )
                            )
                            Text(
                                text = "Play • Learn • Shine",
                                style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFF1B2559))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingIllustration(slide: OnboardingSlide, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(slide.gradient)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = slide.imageRes),
                contentDescription = "Onboarding art",
                modifier = Modifier
                    .padding(18.dp)
                    .sizeIn(maxWidth = 420.dp, maxHeight = 320.dp)
                    .graphicsLayer(alpha = 0.9f, translationY = rememberFloatyOffset(slide.themeTag)),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun rememberFloatyOffset(key: String): Float {
    val transition = rememberInfiniteTransition(label = "float-$key")
    return transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(animation = tween(2200, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "floatAnim-$key"
    ).value
}

@Composable
private fun OnboardingFloaters() {
    val transition = rememberInfiniteTransition(label = "floaters")
    val offsets = listOf(0.1f to 0.2f, 0.3f to 0.7f, 0.75f to 0.4f, 0.55f to 0.15f)
    val drift = transition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(animation = tween(2600, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "drift"
    )
    offsets.forEachIndexed { idx, (x, y) ->
        val scaleAnim = transition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(animation = tween(1800 + idx * 120, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
            label = "scale-$idx"
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFF59D).copy(alpha = 0.35f),
                modifier = Modifier
                    .offset(x = (x * 300f + drift.value).dp, y = (y * 400f + drift.value).dp)
                    .size((28 * scaleAnim.value).dp)
                    .alpha(0.8f)
            )
        }
    }
}

@Composable
private fun KidPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val buttonShape = RoundedCornerShape(20.dp)
    val transition = rememberInfiniteTransition(label = "cta")
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(2800, easing = LinearEasing)),
        label = "ctaShimmer"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "ctaBreathe"
    )
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF5F6D), Color(0xFFFFC371), Color(0xFF48C6EF)),
        start = Offset(shimmer * 600f, 0f),
        end = Offset(shimmer * 600f + 600f, 200f)
    )
    val disabledGradient = Brush.linearGradient(listOf(Color(0xFF8E8E93), Color(0xFF4C4C4C)))

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(if (enabled) breathe else 1f)
            .clip(buttonShape)
            .background(if (enabled) gradient else disabledGradient, buttonShape),
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun KidSocialButton(text: String, iconRes: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Text(text = text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun KidAvatarBadge(option: AvatarOption, imageUrl: String? = null, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(2000), repeatMode = RepeatMode.Reverse),
        label = "avatarPulse"
    )
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(pulse)
            .clip(CircleShape)
            .background(option.color.copy(alpha = 0.85f))
            .border(4.dp, Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(text = option.emoji, fontSize = 48.sp)
        }
    }
}

@Composable
private fun AvatarPickerDialog(
    options: List<AvatarOption>,
    onDismiss: () -> Unit,
    onSelect: (AvatarOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = {
            Text(
                text = "Choisis ton avatar",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(240.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(options) { option ->
                    KidAvatarBadge(option = option, onClick = { onSelect(option) })
                }
            }
        }
    )
}

private fun defaultAvatarOptions(): List<AvatarOption> = listOf(
    AvatarOption("🦥", "Paresseux", Color(0xFF7E57C2)),
    AvatarOption("🦊", "Renard", Color(0xFFFF7043)),
    AvatarOption("🐼", "Panda", Color(0xFF26C6DA)),
    AvatarOption("🐵", "Singe", Color(0xFFFFB74D)),
    AvatarOption("🦄", "Licorne", Color(0xFFEC407A)),
    AvatarOption("🐯", "Tigre", Color(0xFFFFA726))
)

private fun avatarFromProfile(profile: KidProfile, palette: List<AvatarOption> = defaultAvatarOptions()): AvatarOption {
    return palette.firstOrNull { it.emoji == profile.avatarEmoji }
        ?: AvatarOption(profile.avatarEmoji, "Custom", profile.avatarColorHex.toColorOrDefault(Color(0xFF6A5AE0)))
}

private fun Color.toHexString(): String = "#%06X".format(0xFFFFFF and toArgb())

private fun String.toColorOrDefault(default: Color): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (_: IllegalArgumentException) {
    default
}

@OptIn(UnstableApi::class)
@Composable
private fun WelcomeLogoTopLeft() {
    val context = LocalContext.current

    // Build ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoItem = MediaItem.fromUri("android.resource://${context.packageName}/raw/cat_logo_animation")
            setMediaItem(videoItem)
            repeatMode = ExoPlayer.REPEAT_MODE_ALL     // loop forever
            volume = 0f // mute intro video sound
            prepare()
            playWhenReady = true
        }
    }

    // Cleanup when Composable leaves
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .padding(start = 50.dp, top = 50.dp)
            .size(200.dp)
            .clip(RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(18.dp)),
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false   // hide playback buttons
                    setShutterBackgroundColor(Color.Transparent.toArgb())
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )
    }
}

