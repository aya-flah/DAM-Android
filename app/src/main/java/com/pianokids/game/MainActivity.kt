package com.pianokids.game

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pianokids.game.screens.*
import com.pianokids.game.ui.theme.PianoKidsGameTheme
import com.pianokids.game.utils.UserPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PianoKidsGameTheme {
                PianoKidsApp()
            }
        }
    }
}

@Composable
fun PianoKidsApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    // Has the user already seen the welcome screen?
    val hasSeenWelcome = remember {
        context.getSharedPreferences("piano_kids_prefs", Context.MODE_PRIVATE)
            .getBoolean("has_seen_welcome", false)
    }

    val startDestination = if (hasSeenWelcome) "home" else "welcome"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            WelcomeScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToAuth = { navController.navigate("auth") }
            )
        }

        composable("auth") {
            AuthScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateBack = { navController.navigate("welcome") }
            )
        }

        composable("home") {
            val isLoggedIn = userPrefs.isLoggedIn()
            HomeScreen(
                onNavigateToProfile = if (isLoggedIn) {
                    { navController.navigate("profile") }
                } else {
                    { } // Empty lambda for guest mode
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    // clear login flag
                    userPrefs.logout()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

