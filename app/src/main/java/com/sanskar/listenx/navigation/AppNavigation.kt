package com.sanskar.listenx.navigation

import com.sanskar.listenx.ui.component.BottomNavigationBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sanskar.listenx.ui.screen.FavoritesScreen
import com.sanskar.listenx.ui.screen.HomeScreen
import com.sanskar.listenx.ui.screen.ListeningScreen
import com.sanskar.listenx.ui.screen.ProfileScreen
import com.sanskar.listenx.ui.screen.ResultScreen
import com.sanskar.listenx.ui.screen.SplashScreen
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel

/**
 * Main app navigation with bottom navigation bar
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: SongRecognitionViewModel = viewModel()
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route ?: Screen.Splash.route

    // Determine if bottom nav should be shown
    val showBottomNav = currentRoute in listOf(
        Screen.Home.route,
        Screen.Favorites.route,
        Screen.Profile.route,
        Screen.Result.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == Screen.Listening.route) {
                            navController.navigate(route) {
                                popUpTo(route) { inclusive = true }
                                launchSingleTop = true
                                restoreState = false
                            }
                        } else {
                            if (route == Screen.Result.route) {
                                viewModel.resetState()
                            }
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                HomeScreen(viewModel = viewModel)
            }

            // Listening Screen
            composable(Screen.Listening.route) {
                ListeningScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onNavigateToResult = {
                        navController.navigate(Screen.Result.route)
                    }
                )
            }

            // Result Screen
            composable(Screen.Result.route) {
                ResultScreen(
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

            // Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onSettingsClick = {
                        // Navigate to settings if needed
                    },
                    onLogoutClick = {
                        navController.navigate(Screen.Splash.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
