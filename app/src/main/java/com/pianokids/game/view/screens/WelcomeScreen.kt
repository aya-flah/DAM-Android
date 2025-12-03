package com.pianokids.game.view.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
import com.pianokids.game.viewmodel.AuthViewModel
import com.pianokids.game.viewmodel.AvatarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    var showSettings by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var activeKidProfile by remember { mutableStateOf(userPrefs.getKidProfile()) }
    var backendAvatarId by remember { mutableStateOf(activeKidProfile?.backendAvatarId) }
    var backendAvatarName by remember { mutableStateOf(activeKidProfile?.backendAvatarName) }
    var backendAvatarImageUrl by remember { mutableStateOf(activeKidProfile?.backendAvatarImageUrl) }
    var awaitingBackendAvatar by remember { mutableStateOf(false) }
    var isSavingAvatar by remember { mutableStateOf(false) }
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

    DisposableEffect(Unit) {
        SoundManager.startBackgroundMusic()
        onDispose { }
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
            awaitingBackendAvatar = false
            isSavingAvatar = false
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
            persistKidProfile(profile) {
                showCreateAvatarDialog = true
            }
        } else {
            showCreateAvatarDialog = true
        }
    }

    fun loginExistingProfile(profile: KidProfile) {
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
                    authViewModel.onLoginSuccess()
                    avatarViewModel.loadActiveAvatar()
                    activeKidProfile = profile
                    currentStep = OnboardingStep.Greeting
                    returningError = null
                    profileError = null
                    Toast.makeText(context, "Welcome ${profile.displayName}!", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    returningError = error.message ?: "We can't locate that profile."
                    Toast.makeText(context, returningError, Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyBlue, OceanLight, OceanDeep)))
    ) {
        AnimatedOceanWithIslands()

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
                    if (storedProfile == null) {
                        returningError = "We can't find that name. Make a new profile."
                        return@KidWelcomeCard
                    }
                    loginExistingProfile(storedProfile)
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
    val innerShape = RoundedCornerShape(32.dp)

    Card(
        modifier = modifier
            .padding(vertical = 16.dp)
            .shadow(28.dp, RoundedCornerShape(40.dp))
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFB6D6C),
                        Color(0xFFFFC857),
                        Color(0xFF5BE7C4)
                    )
                ),
                shape = RoundedCornerShape(40.dp)
            ),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(innerShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4E54C8),
                            Color(0xFF8F94FB),
                            Color(0xFFFF9A9E)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cat_logo),
                    contentDescription = "Piano Kids Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                )

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
        OutlinedTextField(
            value = uniqueName,
            onValueChange = onUniqueNameChange,
            placeholder = { Text("Magic name") },
            singleLine = true,
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        )
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
        OutlinedTextField(
            value = uniqueName,
            onValueChange = onUniqueNameChange,
            placeholder = { Text("Magic name") },
            singleLine = true,
            isError = errorMessage != null,
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
