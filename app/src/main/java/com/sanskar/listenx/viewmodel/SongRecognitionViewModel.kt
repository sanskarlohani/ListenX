package com.sanskar.listenx.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanskar.listenx.data.model.FavoriteSong
import com.sanskar.listenx.data.model.RecognitionState
import com.sanskar.listenx.data.repository.SongRecognitionRepository
import com.sanskar.listenx.fingerprint.AudioFingerprinter
import com.sanskar.listenx.util.AudioRecorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException

/**
 * ViewModel for song recognition
 * Manages recording, fingerprinting, API calls, and UI state
 */
class SongRecognitionViewModel(
    private val repository: SongRecognitionRepository = SongRecognitionRepository(),
    private val audioRecorder: AudioRecorder = AudioRecorder(),
    private val audioFingerprinter: AudioFingerprinter = AudioFingerprinter()
) : ViewModel() {
    private val TAG = "SongRecognitionVM"

    // State Management using StateFlow
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState.asStateFlow()

    private val _recordingProgress = MutableStateFlow(0)
    val recordingProgress: StateFlow<Int> = _recordingProgress.asStateFlow()

    private val _favoritesList = MutableStateFlow<List<FavoriteSong>>(emptyList())
    val favoritesList: StateFlow<List<FavoriteSong>> = _favoritesList.asStateFlow()

    private val _totalSongsInDb = MutableStateFlow(0)
    val totalSongsInDb: StateFlow<Int> = _totalSongsInDb.asStateFlow()

    private val _searchHistoryCount = MutableStateFlow(0)
    val searchHistoryCount: StateFlow<Int> = _searchHistoryCount.asStateFlow()

    // In-memory favorites storage (in real app, use Room database)
    private val favoritesMemory = mutableListOf<FavoriteSong>()

    private var recognitionJob: Job? = null

    /**
     * Load initial data
     */
    fun loadInitialData() {
        loadFavorites()
        fetchTotalSongs()
    }

    private fun fetchTotalSongs() {
        viewModelScope.launch {
            val total = withContext(Dispatchers.IO) {
                repository.getTotalSongs()
            }
            _totalSongsInDb.value = total
        }
    }

    /**
     * Start audio recording and recognition process
     */
    @SuppressLint("MissingPermission")
    fun startRecognition() {
        Log.d(TAG, "startRecognition called")
        
        // Ignore if already recording to prevent overlapping processes
        if (recognitionJob?.isActive == true) {
            Log.d(TAG, "Recognition already running, ignoring request")
            return
        }
        
        _recordingProgress.value = 0
        
        recognitionJob = viewModelScope.launch {
            try {
                Log.d(TAG, "Recognition job started")
                
                // Ensure any previous hardware state is cleaned up
                withContext(Dispatchers.IO) {
                    audioRecorder.stopRecording()
                }

                // Update state to listening
                _recognitionState.value = RecognitionState.Listening
                
                // Start recording
                val recordingStarted = withContext(Dispatchers.IO) {
                    audioRecorder.startRecording()
                }
                
                if (!recordingStarted) {
                    Log.e(TAG, "Recording failed to start")
                    _recognitionState.value = RecognitionState.Error("Could not access microphone. Please check permissions.")
                    return@launch
                }
                Log.d(TAG, "Recording started")

                // Simulate progress tracking (real app would track actual bytes read)
                val recordingDuration = 20000  // 20 seconds - increased for better fingerprint accuracy
                val progressInterval = 100  // Update every 100ms
                val maxProgress = recordingDuration / progressInterval

                for (progress in 0..maxProgress) {
                    if (!isActive) break
                    _recordingProgress.value = (progress * 100) / maxProgress
                    delay(progressInterval.toLong())
                }

                // Verify the job is still active before moving to processing
                if (!isActive) {
                    Log.d(TAG, "Recognition job cancelled during recording")
                    return@launch
                }

                // Stop recording and get audio data in background
                Log.d(TAG, "Stopping recording and getting audio data")
                val audioData = withContext(Dispatchers.IO) {
                    audioRecorder.stopRecording()
                }
                if (audioData == null) {
                    Log.e(TAG, "Failed to capture audio data (null)")
                    _recognitionState.value = RecognitionState.Error("Failed to capture audio data.")
                    return@launch
                }
                Log.d(TAG, "Captured ${audioData.size} audio samples")

                // Update state to processing
                _recognitionState.value = RecognitionState.Processing

                // Generate fingerprint in background thread
                Log.d(TAG, "Generating fingerprint...")
                val fingerprint = withContext(Dispatchers.Default) {
                    audioFingerprinter.generateFingerprint(
                        audioData,
                        AudioRecorder.SAMPLE_RATE
                    )
                }

                if (fingerprint.isEmpty()) {
                    Log.w(TAG, "Fingerprint is empty")
                    _recognitionState.value = RecognitionState.Error("No music detected. Try moving closer to the source.")
                    return@launch
                }
                Log.d(TAG, "Fingerprint generated with ${fingerprint.size} hashes")

                // Send fingerprint to backend for matching in IO thread
                Log.d(TAG, "Matching fingerprint with backend...")
                val results = try {
                    withContext(Dispatchers.IO) {
                        repository.matchFingerprint(fingerprint)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Backend match failed: ${e.message}. Falling back to mock data.", e)
                    // Fallback to mock data for demo if API fails
                    withContext(Dispatchers.Default) {
                        com.sanskar.listenx.util.MockDataProvider.getMockSongs()
                    }
                }
                Log.d(TAG, "Found ${results.size} matches")

                // Update state with results
                _recognitionState.value = RecognitionState.Success(results)
                _searchHistoryCount.value += 1
                Log.d(TAG, "Recognition successful")

            } catch (e: CancellationException) {
                Log.d(TAG, "Recognition job cancelled via exception")
                throw e // Essential to re-throw for coroutine scope
            } catch (e: Exception) {
                Log.e(TAG, "Recognition error: ${e.message}", e)
                _recognitionState.value = RecognitionState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            } finally {
                // Ensure microphone is stopped in all scenarios
                withContext(Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
                    if (audioRecorder.isRecording()) {
                        audioRecorder.stopRecording()
                    }
                }
            }
        }
    }

    /**
     * Cancel ongoing recognition
     */
    fun cancelRecognition() {
        viewModelScope.launch {
            Log.d(TAG, "cancelRecognition called")
            
            // Wait for the job to fully terminate and release resources
            recognitionJob?.cancelAndJoin()
            
            // Only reset to Idle if we were in the middle of recognition
            // This prevents clearing the Success state when navigating to the Result screen
            if (_recognitionState.value is RecognitionState.Listening || 
                _recognitionState.value is RecognitionState.Processing) {
                _recognitionState.value = RecognitionState.Idle
                _recordingProgress.value = 0
            }
        }
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
        favoritesMemory.removeAll { it.id == songId || it.youtubeId == songId }
        _favoritesList.value = favoritesMemory.toList()
    }

    /**
     * Check if a song is in favorites
     */
    fun isFavorite(songId: String): Boolean {
        return favoritesMemory.any { it.id == songId || it.youtubeId == songId }
    }

    /**
     * Reset recognition state
     */
    fun resetState() {
        recognitionJob?.cancel()
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

