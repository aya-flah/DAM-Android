// MainActivity.kt
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
import com.pianokids.game.ui.theme.PianoKidsGameTheme
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private lateinit var socialLoginManager: SocialLoginManager
    private lateinit var userPrefs: UserPreferences
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        socialLoginManager = SocialLoginManager(this)
        userPrefs = UserPreferences(this)
        authViewModel = AuthViewModel(application)


        SoundManager.init(this)

        // Initialize Facebook SDK without client token
        try {
            // Initialize Facebook SDK with auto-log enabled (no client token needed)
            FacebookSdk.setAutoLogAppEventsEnabled(false) // Disable auto events for now
            FacebookSdk.sdkInitialize(applicationContext)
            FacebookSdk.setAdvertiserIDCollectionEnabled(false)

            // For debugging
            if (BuildConfig.DEBUG) {
                FacebookSdk.setIsDebugEnabled(true)
            }

            Log.d("Facebook", "Facebook SDK initialized successfully")
        } catch (e: Exception) {
            Log.e("Facebook", "Facebook SDK initialization failed", e)
            // Continue without Facebook if initialization fails
        }

        setContent {
            PianoKidsGameTheme {
                val navController = rememberNavController()

                // Background to avoid white flash
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // ── DETERMINE START DESTINATION ───────────────────────
                    val hasSeenWelcome = userPrefs.hasSeenWelcome()
                    val isLoggedIn = userPrefs.hasLocalToken()  // Fast local check

                    val startDestination = when {
                        !hasSeenWelcome -> "welcome"
                        isLoggedIn -> "home"
                        else -> "welcome"  // First time or guest → show Welcome
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("welcome") {
                            WelcomeScreen(
                                onNavigateToHome = {
                                    userPrefs.setSeenWelcome(true)
                                    userPrefs.clearGuestMode()  // Ensure clean state
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToAuth = { navController.navigate("welcome") }
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
        // Handle Facebook login result
        socialLoginManager.handleFacebookResult(requestCode, resultCode, data)
    }
}


