package com.sanskar.listenx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.rememberNavController
import com.sanskar.listenx.navigation.AppNavigation
import com.sanskar.listenx.ui.theme.ListenXTheme
import com.sanskar.listenx.util.PermissionManager

/**
 * MainActivity - Entry point of the application
 * Sets up Jetpack Compose and Navigation
 * Handles runtime permission requests
 */
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions have been requested
        // The app will continue even if permissions are denied,
        // but recording will fail gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request permissions
        requestPermissions()

        setContent {
            ListenXTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = PermissionManager.requiredPermissions.filter { permission ->
            checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }
}

