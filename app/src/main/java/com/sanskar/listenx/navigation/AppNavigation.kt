package com.sanskar.listenx.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sanskar.listenx.ui.screen.FavoritesScreen
import com.sanskar.listenx.ui.screen.HomeScreen
import com.sanskar.listenx.ui.screen.SongRecognitionScreen
import com.sanskar.listenx.ui.screen.SplashScreen
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel

/**
 * Navigation host composable that defines all app routes
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: SongRecognitionViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToRecognition = {
                    navController.navigate(Screen.Recognition.route)
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                }
            )
        }

        // Song Recognition Screen
        composable(Screen.Recognition.route) {
            SongRecognitionScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Favorites Screen
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}

