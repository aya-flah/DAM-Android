package com.pianokids.game.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pianokids.game.view.screens.AppPianoScreen
import com.pianokids.game.view.screens.HomeScreen
import com.pianokids.game.view.screens.LevelOneScreen
import com.pianokids.game.view.screens.ProfileScreen
import com.pianokids.game.view.screens.WelcomeScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Level1 : Screen("level1")
    object AppPiano : Screen("app_piano/{levelNumber}") {
        fun createRoute(levelNumber: Int) = "app_piano/$levelNumber"
    }
}

@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) }
            )
        }


        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToLevel1 = { navController.navigate(Screen.Level1.route) }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    // Optional: clear login state
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Level1.route) {
            LevelOneScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AppPiano.route) { backStackEntry ->
            val levelNumber = backStackEntry.arguments?.getString("levelNumber")?.toIntOrNull() ?: 1
            AppPianoScreen(
                levelNumber = levelNumber,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
