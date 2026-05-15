package com.sanskar.listenx.navigation

/**
 * Navigation routes as sealed class (type-safe navigation)
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Listening : Screen("listening")
    object Result : Screen("result")
    object Favorites : Screen("favorites")
    object Profile : Screen("profile")
    object Contribute : Screen("contribute")
}

