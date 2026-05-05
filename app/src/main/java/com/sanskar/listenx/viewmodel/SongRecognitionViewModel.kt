package com.sanskar.listenx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.listenx.data.model.FavoriteSong
import com.sanskar.listenx.data.model.RecognitionState
import com.sanskar.listenx.data.repository.SongRecognitionRepository
import com.sanskar.listenx.fingerprint.AudioFingerprinter
import com.sanskar.listenx.util.AudioRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for song recognition
 * Manages recording, fingerprinting, API calls, and UI state
 */
class SongRecognitionViewModel : ViewModel() {

    // Dependencies
    private val repository = SongRecognitionRepository()
    private val audioRecorder = AudioRecorder()
    private val audioFingerprinter = AudioFingerprinter()

    // State Management using StateFlow
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()

    private val _recordingProgress = MutableStateFlow(0)
    val recordingProgress: StateFlow<Int> = _recordingProgress.asStateFlow()

    private val _favoritesList = MutableStateFlow<List<FavoriteSong>>(emptyList())
    val favoritesList: StateFlow<List<FavoriteSong>> = _favoritesList.asStateFlow()

    // In-memory favorites storage (in real app, use Room database)
    private val favoritesMemory = mutableListOf<FavoriteSong>()

    /**
     * Start audio recording and recognition process
     */
    fun startRecognition() {
        viewModelScope.launch {
            try {
                // Update state to listening
                _recognitionState.value = RecognitionState.Listening
                _recordingProgress.value = 0

                // Start recording
                val recordingStarted = audioRecorder.startRecording()
                if (!recordingStarted) {
                    _recognitionState.value = RecognitionState.Error("Failed to start recording")
                    return@launch
                }

                // Simulate progress tracking
                val recordingDuration = 10000  // 10 seconds
                val progressInterval = 100  // Update every 100ms
                val maxProgress = recordingDuration / progressInterval

                for (progress in 0..maxProgress) {
                    _recordingProgress.value = (progress * 100) / maxProgress
                    kotlinx.coroutines.delay(progressInterval.toLong())
                    if (progress >= maxProgress) break
                }

                // Stop recording and get audio data
                val audioData = audioRecorder.stopRecording()
                if (audioData == null) {
                    _recognitionState.value = RecognitionState.Error("Failed to record audio")
                    return@launch
                }

                // Update state to processing
                _recognitionState.value = RecognitionState.Processing

                // Generate fingerprint
                val fingerprint = audioFingerprinter.generateFingerprint(
                    audioData,
                    AudioRecorder.SAMPLE_RATE
                )

                if (fingerprint.isEmpty()) {
                    _recognitionState.value = RecognitionState.Error("Failed to generate fingerprint")
                    return@launch
                }

                // Send fingerprint to backend for matching
                val results = repository.matchFingerprint(fingerprint)

                // Update state with results
                _recognitionState.value = RecognitionState.Success(results)

            } catch (e: Exception) {
                _recognitionState.value = RecognitionState.Error(
                    e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Cancel ongoing recognition
     */
    fun cancelRecognition() {
        if (audioRecorder.isRecording()) {
            audioRecorder.stopRecording()
        }
        _recognitionState.value = RecognitionState.Idle
        _recordingProgress.value = 0
    }

    /**
     * Save a song to favorites
     */
    fun addToFavorites(song: FavoriteSong) {
        favoritesMemory.add(song)
        _favoritesList.value = favoritesMemory.toList()
    }

    /**
     * Remove a song from favorites
     */
    fun removeFromFavorites(songId: String) {
        favoritesMemory.removeAll { it.id == songId }
        _favoritesList.value = favoritesMemory.toList()
    }

    /**
     * Check if a song is in favorites
     */
    fun isFavorite(songId: String): Boolean {
        return favoritesMemory.any { it.id == songId }
    }

    /**
     * Reset recognition state
     */
    fun resetState() {
        _recognitionState.value = RecognitionState.Idle
        _recordingProgress.value = 0
    }

    /**
     * Load initial favorites (from database in real app)
     */
    fun loadFavorites() {
        // In a real app, load from Room database
        _favoritesList.value = favoritesMemory.toList()
    }
}

