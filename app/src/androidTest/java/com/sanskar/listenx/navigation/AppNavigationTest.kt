package com.sanskar.listenx.navigation

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sanskar.listenx.MainActivity
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_startsInSplashScreen() {
        // Splash screen has "FOURIO" text or similar logo
        composeTestRule.onNodeWithText("FOURIO").assertExists()
    }

    @Test
    fun splash_navigatesToHome() {
        // Splash screen usually has a delay or button for demo
        // If it's automatic, we just wait
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasText("Discover What's Playing")).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithText("Discover What's Playing").assertExists()
    }
}
