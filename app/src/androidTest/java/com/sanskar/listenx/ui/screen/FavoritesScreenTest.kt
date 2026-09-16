package com.sanskar.listenx.ui.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sanskar.listenx.data.model.FavoriteSong
import com.sanskar.listenx.ui.theme.ListenXTheme
import com.sanskar.listenx.viewmodel.SongRecognitionViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun favoritesScreen_displaysEmptyStateWhenNoFavorites() {
        val viewModel = mockk<SongRecognitionViewModel>(relaxed = true)
        every { viewModel.favoritesList } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            ListenXTheme {
                FavoritesScreen(viewModel = viewModel, onNavigateBack = {})
            }
        }

        composeTestRule.onNodeWithText("My Favorites").assertExists()
        composeTestRule.onNodeWithText("No Favorites Yet").assertExists()
        composeTestRule.onNodeWithText("Add songs to your favorites to see them here").assertExists()
    }

    @Test
    fun favoritesScreen_displaysFavoriteSongItem() {
        val viewModel = mockk<SongRecognitionViewModel>(relaxed = true)
        val sampleFavorite = FavoriteSong("1", "Test Song Title", "Test Artist", "yt123", 95f, "Test Album")
        every { viewModel.favoritesList } returns MutableStateFlow(listOf(sampleFavorite))

        composeTestRule.setContent {
            ListenXTheme {
                FavoritesScreen(viewModel = viewModel, onNavigateBack = {})
            }
        }

        composeTestRule.onNodeWithText("My Favorites").assertExists()
        composeTestRule.onNodeWithText("Test Song Title").assertExists()
        composeTestRule.onNodeWithText("Test Artist").assertExists()
    }
}
