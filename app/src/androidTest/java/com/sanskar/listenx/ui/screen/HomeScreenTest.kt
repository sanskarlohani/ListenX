package com.sanskar.listenx.ui.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sanskar.listenx.ui.theme.ListenXTheme
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysHeaderAndSectionTitles() {
        val viewModel = mockk<SongRecognitionViewModel>(relaxed = true)
        
        composeTestRule.setContent {
            ListenXTheme {
                HomeScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("FOURIO").assertExists()
        composeTestRule.onNodeWithText("Recently Played").assertExists()
        composeTestRule.onNodeWithText("Recommended For You").assertExists()
    }
}
