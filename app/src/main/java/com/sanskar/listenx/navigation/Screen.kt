package com.sanskar.listenx.navigation

/**
 * Navigation routes as sealed class (type-safe navigation)
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Recognition : Screen("recognition")
    object Favorites : Screen("favorites")
    object SongDetail : Screen("songDetail/{songId}") {
        fun createRoute(songId: String) = "songDetail/$songId"
    }
}

