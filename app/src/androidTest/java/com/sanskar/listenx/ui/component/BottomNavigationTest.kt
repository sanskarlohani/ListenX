package com.sanskar.listenx.ui.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.sanskar.listenx.ui.theme.ListenXTheme
import org.junit.Rule
import org.junit.Test

class BottomNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomNavigationBar_rendersFloatingMicButton() {
        composeTestRule.setContent {
            ListenXTheme {
                BottomNavigationBar(
                    currentRoute = "home",
                    onNavigate = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Listen").assertExists()
    }
}
