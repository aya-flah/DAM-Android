package com.pianokids.game.view.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.pianokids.game.R
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.components.AnimatedOceanWithIslands
import com.pianokids.game.utils.components.RainbowButton
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPrefs = remember { UserPreferences(context) }
    val authRepository = remember { AuthRepository(context) }
    val socialLoginManager = remember { SocialLoginManager(context) }
    val scope = rememberCoroutineScope()
    val isFacebookAvailable by remember { mutableStateOf(socialLoginManager.isFacebookAvailable()) }

    var soundEnabled by remember { mutableStateOf(true) }
    var musicEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var showButtons by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showLoginChooser by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // ✅ CHECK IF USER HAS PLAYED BEFORE
    val hasPlayedBefore = remember { userPrefs.getSeenWelcome() || isLoggedIn }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        socialLoginManager.handleGoogleSignInResult(task)
    }

    // Animations
    val inf = rememberInfiniteTransition(label = "welcome")
    val logoScale by inf.animateFloat(
        1f, 1.1f,
        infiniteRepeatable(tween(1000, easing = EaseInOutCubic), RepeatMode.Reverse),
        "logoScale"
    )
    val logoAlpha by inf.animateFloat(
        0.8f, 1f,
        infiniteRepeatable(tween(1500, easing = EaseInOutCubic), RepeatMode.Reverse),
        "logoAlpha"
    )

    // Show buttons after delay
    LaunchedEffect(Unit) { delay(1500); showButtons = true }

    // START MUSIC ONCE
    DisposableEffect(Unit) {
        SoundManager.startBackgroundMusic()
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyBlue, OceanLight, OceanDeep)))
    ) {
        AnimatedOceanWithIslands()

        if (showButtons && !isLoading) {
            IconButton(
                onClick = {
                    showSettings = true
                    SoundManager.playClick()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .zIndex(2f)
            ) {
                Icon(
                    Icons.Default.Settings,
                    "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

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

        // MAIN CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 500.dp, end = 5.dp, top = 32.dp, bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(R.drawable.cat_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(280.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(32.dp))

            // Title
            Text(
                "Piano Kids",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.W600,
                    color = Color.White,
                    shadow = Shadow(
                        Color.Black.copy(0.3f),
                        Offset(4f, 4f),
                        8f
                    )
                ),
                textAlign = TextAlign.Center
            )

            Text(
                "Learn Piano with Us!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = RainbowYellow,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(64.dp))

            // Buttons
            if (showButtons && !isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ✅ SHOW "CONTINUE" IF USER HAS PLAYED BEFORE
                    if (hasPlayedBefore) {
                        RainbowButton(
                            text = "Continue Game",
                            onClick = {
                                SoundManager.playClick()
                                onNavigateToHome()
                            },
                            colors = listOf(RainbowGreen, RainbowYellow)
                        )

                        if (!isLoggedIn) {
                            Spacer(Modifier.height(12.dp))

                            RainbowButton(
                                text = "Login / Sign-up",
                                onClick = {
                                    SoundManager.playClick()
                                    showLoginChooser = true
                                },
                                colors = listOf(RainbowBlue, RainbowIndigo)
                            )
                        }
                    } else {
                        // FIRST TIME USER
                        RainbowButton(
                            text = "Play as Guest",
                            onClick = {
                                SoundManager.playClick()
                                userPrefs.setGuestMode(true)
                                userPrefs.setSeenWelcome(true)
                                onNavigateToHome()
                            },
                            colors = listOf(RainbowOrange, RainbowYellow)
                        )

                        Spacer(Modifier.height(12.dp))

                        RainbowButton(
                            text = "Login / Sign-up",
                            onClick = {
                                SoundManager.playClick()
                                showLoginChooser = true
                            },
                            colors = listOf(RainbowBlue, RainbowIndigo)
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        // Settings Dialog
        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }

        // Replace the Login Chooser dialog section in WelcomeScreen with this:

// Login Chooser
        if (showLoginChooser) {
            AlertDialog(
                onDismissRequest = { showLoginChooser = false },
                title = { Text("Choose a login method", textAlign = TextAlign.Center) },
                text = {
                    Column {
                        SocialDialogButton(
                            text = "Continue with Google",
                            iconRes = R.drawable.ic_google,
                            onClick = {
                                showLoginChooser = false
                                isLoading = true
                                socialLoginManager.signInWithGoogle(
                                    launcher = googleSignInLauncher,
                                    onSuccess = { idToken ->
                                        scope.launch {
                                            val result = authRepository.loginWithSocial(idToken, "google")
                                            isLoading = false
                                            result.onSuccess {
                                                // ✅ NOTIFY VIEWMODEL TO REFRESH STATE (AWAIT IT)
                                                authViewModel.onLoginSuccess()

                                                userPrefs.setSeenWelcome(true)
                                                userPrefs.clearGuestMode()
                                                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()

                                                // Small delay to ensure state propagates
                                                kotlinx.coroutines.delay(200)
                                                onNavigateToHome()
                                            }.onFailure {
                                                Toast.makeText(context, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onFailure = {
                                        isLoading = false
                                        Toast.makeText(context, "Google sign-in failed: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        if (isFacebookAvailable) {
                            SocialDialogButton(
                                text = "Continue with Facebook",
                                iconRes = R.drawable.ic_facebook,
                                onClick = {
                                    showLoginChooser = false
                                    isLoading = true
                                    activity?.let {
                                        socialLoginManager.loginWithFacebook(
                                            activity = it,
                                            onSuccess = { accessToken ->
                                                scope.launch {
                                                    val result = authRepository.loginWithSocial(accessToken, "facebook")
                                                    isLoading = false
                                                    result.onSuccess {
                                                        // ✅ NOTIFY VIEWMODEL TO REFRESH STATE
                                                        authViewModel.onLoginSuccess()

                                                        userPrefs.setSeenWelcome(true)
                                                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                                        onNavigateToHome()
                                                    }.onFailure {
                                                        Toast.makeText(context, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            },
                                            onFailure = {
                                                isLoading = false
                                                Toast.makeText(context, "Facebook login failed: ${it.message}", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showLoginChooser = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

    }
}

@Composable
private fun SocialDialogButton(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp).padding(end = 12.dp)
            )
            Text(
                text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            )
        }
    }
}