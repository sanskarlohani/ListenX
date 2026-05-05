package com.sanskar.listenx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.sanskar.listenx.navigation.AppNavigation
import com.sanskar.listenx.ui.theme.ListenXTheme

/**
 * MainActivity - Entry point of the application
 * Sets up Jetpack Compose and Navigation
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListenXTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}

