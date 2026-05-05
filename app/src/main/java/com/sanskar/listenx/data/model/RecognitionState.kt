package com.sanskar.listenx.data.model

/**
 * Sealed class representing the state of audio recognition
 */
sealed class RecognitionState {
    object Idle : RecognitionState()
    object Listening : RecognitionState()
    object Processing : RecognitionState()
    data class Success(val songs: List<SongRecognitionResponse>) : RecognitionState()
    data class Error(val message: String) : RecognitionState()
}

