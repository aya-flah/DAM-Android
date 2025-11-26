package com.pianokids.game

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.media3.common.BuildConfig
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.facebook.FacebookSdk
import com.pianokids.game.view.screens.HomeScreen
import com.pianokids.game.view.screens.ProfileScreen
import com.pianokids.game.view.screens.WelcomeScreen
import com.pianokids.game.view.screens.MusicRecognitionScreen
import com.pianokids.game.ui.theme.PianoKidsGameTheme
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.viewmodel.AuthViewModel
import com.pianokids.game.data.repository.AuthRepository
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.pianokids.game.view.screens.levels.LevelScreen
import com.pianokids.game.utils.PianoSoundManager


class MainActivity : ComponentActivity() {

    private lateinit var socialLoginManager: SocialLoginManager
    private lateinit var userPrefs: UserPreferences
    private lateinit var authViewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        socialLoginManager = SocialLoginManager(this)
        userPrefs = UserPreferences(this)
        authRepository = AuthRepository(this)
        authViewModel = AuthViewModel(application)

        // Initialize Piano Sound Manager
        PianoSoundManager.init(this)

        SoundManager.init(this)

        // Initialize Facebook SDK without client token
        try {
            FacebookSdk.setAutoLogAppEventsEnabled(false)
            FacebookSdk.sdkInitialize(applicationContext)
            FacebookSdk.setAdvertiserIDCollectionEnabled(false)

            if (BuildConfig.DEBUG) {
                FacebookSdk.setIsDebugEnabled(true)
            }

            Log.d("Facebook", "Facebook SDK initialized successfully")
        } catch (e: Exception) {
            Log.e("Facebook", "Facebook SDK initialization failed", e)
        }

        setContent {
            PianoKidsGameTheme {
                val navController = rememberNavController()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val hasSeenWelcome = userPrefs.hasSeenWelcome()
                    var startDestination by remember { mutableStateOf("welcome") }

                    LaunchedEffect(Unit) {
                        // Debug: Check all stored preferences
                        Log.d("MainActivity", "=== STARTUP DEBUG ===")
                        Log.d("MainActivity", "hasSeenWelcome: $hasSeenWelcome")
                        Log.d("MainActivity", "authToken: ${userPrefs.getAuthToken()?.take(10)}...")
                        Log.d("MainActivity", "providerId: ${userPrefs.getProviderId()}")
                        Log.d("MainActivity", "isGuestMode: ${userPrefs.isGuestMode()}")
                        Log.d("MainActivity", "userData: ${userPrefs.getUser()}")
                        
                        // Check if user is logged in
                        val hasToken = userPrefs.hasLocalToken()
                        val isLoggedIn = if (hasToken) {
                            val verified = authRepository.verifyToken()
                            Log.d("MainActivity", "Token verification result: $verified")
                            // If token is invalid, clear it
                            if (!verified) {
                                Log.d("MainActivity", "Token invalid, clearing auth")
                                userPrefs.clearAuth()
                            }
                            verified
                        } else {
                            false
                        }

                        val isGuest = userPrefs.isGuestMode()

                        // Debug logging
                        Log.d("MainActivity", "Has token: $hasToken")
                        Log.d("MainActivity", "Is logged in: $isLoggedIn")
                        Log.d("MainActivity", "Is guest mode: $isGuest")

                        // Navigation logic: only go to home if explicitly logged in or guest
                        startDestination = when {
                            isLoggedIn -> {
                                // User has valid auth token
                                Log.d("MainActivity", "Navigation: Going to home (logged in)")
                                userPrefs.clearGuestMode()
                                "home"
                            }
                            isGuest -> {
                                // User is in guest mode
                                Log.d("MainActivity", "Navigation: Going to home (guest mode)")
                                "home"
                            }
                            else -> {
                                // Not logged in and not guest - show welcome
                                Log.d("MainActivity", "Navigation: Going to welcome")
                                "welcome"
                            }
                        }
                        
                        Log.d("MainActivity", "Final destination: $startDestination")

                        navController.navigate(startDestination) {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("welcome") {
                            WelcomeScreen(
                                onNavigateToHome = {
                                    userPrefs.setSeenWelcome(true)
                                    userPrefs.clearGuestMode()
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToAuth = { navController.navigate("welcome") },
                                onNavigateBack = { 
                                    // Don't do anything - HomeScreen is a root destination
                                    // User should use device back button to exit app
                                },
                                onNavigateToLevel = { levelId, userId ->
                                    navController.navigate("level/$levelId/$userId")
                                },
                                onNavigateToMusic = { navController.navigate("music") }
                            )
                        }

                        composable("music") {
                            MusicRecognitionScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onLogout = {
                                    userPrefs.clearAuth()
                                    userPrefs.clearGuestMode()
                                    navController.navigate("welcome") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("level/{levelId}/{userId}") { backStackEntry ->
                            val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
                            val userId = backStackEntry.arguments?.getString("userId") ?: "guest"

                            LevelScreen(
                                userId = userId,
                                levelId = levelId,
                                onExit = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }

        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true)
            FacebookSdk.addLoggingBehavior(com.facebook.LoggingBehavior.INCLUDE_ACCESS_TOKENS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        socialLoginManager.handleFacebookResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release Piano Sound Manager resources
        PianoSoundManager.release()
    }
}