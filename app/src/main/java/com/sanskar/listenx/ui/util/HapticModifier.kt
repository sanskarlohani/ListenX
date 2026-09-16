package com.sanskar.listenx.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.core.content.edit

/**
 * Manages user preference for enabling or disabling haptic feedback across the application.
 */
object HapticPreference {
    private const val PREF_NAME = "listenx_preferences"
    private const val KEY_HAPTIC_ENABLED = "haptic_enabled"

    @Volatile
    private var cachedEnabled: Boolean? = null

    fun isHapticEnabled(context: Context): Boolean {
        cachedEnabled?.let { return it }
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        cachedEnabled = enabled
        return enabled
    }

    fun setHapticEnabled(context: Context, enabled: Boolean) {
        cachedEnabled = enabled
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_HAPTIC_ENABLED, enabled) }
    }
}

/**
 * Triggers a clear, soft touch haptic vibration using the system Vibrator hardware if haptics are enabled.
 */
fun triggerSoftHaptic(context: Context, view: View? = null) {
    if (!HapticPreference.isHapticEnabled(context)) return

    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(30, 80))
            }
            return
        }
    } catch (_: Exception) {
        // Fallback to View haptic feedback
    }

    view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
}

/**
 * Fallback extension for HapticFeedback.
 */
fun HapticFeedback.performSoftHaptic(context: Context? = null, view: View? = null) {
    if (context != null) {
        triggerSoftHaptic(context, view)
    } else {
        this.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

/**
 * Helper function to obtain a soft haptic trigger inside a Composable.
 */
@Composable
fun rememberSoftHaptic(): () -> Unit {
    val context = LocalContext.current
    val view = LocalView.current
    return remember(context, view) {
        { triggerSoftHaptic(context, view) }
    }
}

/**
 * A clickable modifier that triggers soft haptic vibration on click.
 */
fun Modifier.softHapticClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onClick: () -> Unit
): Modifier = composed {
    val context = LocalContext.current
    val view = LocalView.current
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    this.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        interactionSource = actualInteractionSource,
        indication = indication
    ) {
        triggerSoftHaptic(context, view)
        onClick()
    }
}
