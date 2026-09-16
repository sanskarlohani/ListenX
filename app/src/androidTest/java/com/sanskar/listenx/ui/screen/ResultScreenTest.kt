package com.sanskar.listenx.ui.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sanskar.listenx.data.model.RecognitionState
import com.sanskar.listenx.data.model.SongRecognitionResponse
import com.sanskar.listenx.ui.theme.ListenXTheme
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class ResultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun resultScreen_displaysSuccessMatches() {
        val viewModel = mockk<SongRecognitionViewModel>(relaxed = true)
        val mockSongs = listOf(
            SongRecognitionResponse(1L, "Sample Match Song", "Sample Match Artist", "yt123", 0, 40000f)
        )
        every { viewModel.recognitionState } returns MutableStateFlow(RecognitionState.Success(mockSongs))

        composeTestRule.setContent {
            ListenXTheme {
                ResultScreen(
                    viewModel = viewModel,
                    onNavigateBack = {},
                    onNavigateToHome = {}
                )
            }
        }

        composeTestRule.onNodeWithText("✅ Found 1 Match").assertExists()
        composeTestRule.onNodeWithText("Sample Match Song").assertExists()
        composeTestRule.onNodeWithText("Sample Match Artist").assertExists()
        composeTestRule.onNodeWithText("Search Again").assertExists()
    }

    @Test
    fun resultScreen_displaysErrorMessageOnFailure() {
        val viewModel = mockk<SongRecognitionViewModel>(relaxed = true)
        every { viewModel.recognitionState } returns MutableStateFlow(RecognitionState.Error("No music detected."))

        composeTestRule.setContent {
            ListenXTheme {
                ResultScreen(
                    viewModel = viewModel,
                    onNavigateBack = {},
                    onNavigateToHome = {}
                )
            }
        }

        composeTestRule.onNodeWithText("❌ Error").assertExists()
        composeTestRule.onNodeWithText("No music detected.").assertExists()
        composeTestRule.onNodeWithText("Try Again").assertExists()
    }
}
