package com.sanskar.listenx.viewmodel

import app.cash.turbine.test
import com.sanskar.listenx.data.model.FavoriteSong
import com.sanskar.listenx.data.model.RecognitionState
import com.sanskar.listenx.data.model.SongRecognitionResponse
import com.sanskar.listenx.data.repository.SongRecognitionRepository
import com.sanskar.listenx.fingerprint.AudioFingerprinter
import com.sanskar.listenx.util.AudioRecorder
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongRecognitionViewModelTest {

    private val repository = mockk<SongRecognitionRepository>()
    private val recorder = mockk<AudioRecorder>(relaxed = true)
    private val fingerprinter = mockk<AudioFingerprinter>()
    
    private lateinit var viewModel: SongRecognitionViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SongRecognitionViewModel(repository, recorder, fingerprinter)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() = runTest {
        assertEquals(RecognitionState.Idle, viewModel.recognitionState.value)
    }

    @Test
    fun `startRecognition transitions states correctly on success`() = runTest {
        val mockAudio = ShortArray(100)
        val mockFingerprint = mapOf("hash" to 1)
        val mockResults = listOf(SongRecognitionResponse(1L, "Title", "Artist", "ytid", 0, 100f))

        every { recorder.startRecording() } returns true
        every { recorder.stopRecording() } returns mockAudio
        every { fingerprinter.generateFingerprint(any(), any()) } returns mockFingerprint
        coEvery { repository.matchFingerprint(any()) } returns mockResults

        viewModel.recognitionState.test {
            assertEquals(RecognitionState.Idle, awaitItem())
            
            viewModel.startRecognition()
            
            // Should be Listening
            assertEquals(RecognitionState.Listening, awaitItem())
            
            // Advance time to skip simulation of recording progress (20s)
            testDispatcher.scheduler.advanceTimeBy(21000)
            
            // Should be Processing
            assertEquals(RecognitionState.Processing, awaitItem())
            
            // Should be Success
            val finalState = awaitItem()
            assertTrue(finalState is RecognitionState.Success)
            assertEquals(mockResults, (finalState as RecognitionState.Success).songs)
        }
    }

    @Test
    fun `startRecognition transitions to Error when recorder fails`() = runTest {
        every { recorder.startRecording() } returns false

        viewModel.recognitionState.test {
            assertEquals(RecognitionState.Idle, awaitItem())
            
            viewModel.startRecognition()
            
            assertEquals(RecognitionState.Listening, awaitItem())
            
            val errorState = awaitItem()
            assertTrue(errorState is RecognitionState.Error)
        }
    }

    @Test
    fun `cancelRecognition resets state to Idle when listening`() = runTest {
        every { recorder.startRecording() } returns true

        viewModel.recognitionState.test {
            assertEquals(RecognitionState.Idle, awaitItem())

            viewModel.startRecognition()
            assertEquals(RecognitionState.Listening, awaitItem())

            viewModel.cancelRecognition()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(RecognitionState.Idle, awaitItem())
        }
    }

    @Test
    fun `favorites management works correctly`() {
        val song = FavoriteSong("1", "Title", "Artist", "yt123", 100f, "Album")

        viewModel.addToFavorites(song)
        assertTrue(viewModel.isFavorite("1"))
        assertTrue(viewModel.isFavorite("yt123"))
        assertEquals(1, viewModel.favoritesList.value.size)

        viewModel.removeFromFavorites("yt123")
        assertTrue(!viewModel.isFavorite("1"))
        assertTrue(!viewModel.isFavorite("yt123"))
        assertEquals(0, viewModel.favoritesList.value.size)
    }

    @Test
    fun `resetState resets state to Idle and progress to 0`() {
        viewModel.resetState()

        assertEquals(RecognitionState.Idle, viewModel.recognitionState.value)
        assertEquals(0, viewModel.recordingProgress.value)
    }

    @Test
    fun `startRecognition sets Error state when fingerprint is empty`() = runTest {
        val mockAudio = ShortArray(100)
        every { recorder.startRecording() } returns true
        every { recorder.stopRecording() } returns mockAudio
        every { fingerprinter.generateFingerprint(any(), any()) } returns emptyMap()

        viewModel.recognitionState.test {
            assertEquals(RecognitionState.Idle, awaitItem())

            viewModel.startRecognition()
            assertEquals(RecognitionState.Listening, awaitItem())

            testDispatcher.scheduler.advanceTimeBy(21000)

            assertEquals(RecognitionState.Processing, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is RecognitionState.Error)
            assertEquals("No music detected. Try moving closer to the source.", (errorState as RecognitionState.Error).message)
        }
    }
}
