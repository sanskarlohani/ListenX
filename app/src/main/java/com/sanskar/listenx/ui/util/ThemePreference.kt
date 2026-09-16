package com.sanskar.listenx.ui.util

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

/**
 * Manages user preference for application dark mode theme.
 */
object ThemePreference {
    private const val PREF_NAME = "listenx_preferences"
    private const val KEY_DARK_MODE = "dark_mode_enabled"

    // Nullable state: null means follow system theme until user toggles explicitly
    val darkModeState = mutableStateOf<Boolean?>(null)

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(KEY_DARK_MODE)) {
            darkModeState.value = prefs.getBoolean(KEY_DARK_MODE, false)
        } else {
            darkModeState.value = null
        }
    }

    @Composable
    fun isDarkTheme(): Boolean {
        val override = darkModeState.value
        return override ?: isSystemInDarkTheme()
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        darkModeState.value = enabled
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_DARK_MODE, enabled) }
    }
}
