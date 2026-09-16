package com.sanskar.listenx.ui.util

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ThemePreferenceTest {

    private val context = mockk<Context>()
    private val prefs = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)

    @Before
    fun setup() {
        every { context.getSharedPreferences("listenx_preferences", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
    }

    @Test
    fun `init sets darkModeState to null when key is absent`() {
        every { prefs.contains("dark_mode_enabled") } returns false

        ThemePreference.init(context)

        assertNull(ThemePreference.darkModeState.value)
    }

    @Test
    fun `init sets darkModeState from SharedPreferences when key exists`() {
        every { prefs.contains("dark_mode_enabled") } returns true
        every { prefs.getBoolean("dark_mode_enabled", false) } returns true

        ThemePreference.init(context)

        assertEquals(true, ThemePreference.darkModeState.value)
    }

    @Test
    fun `setDarkMode updates darkModeState and SharedPreferences`() {
        ThemePreference.setDarkMode(context, true)

        assertEquals(true, ThemePreference.darkModeState.value)
        verify { editor.putBoolean("dark_mode_enabled", true) }
    }
}
