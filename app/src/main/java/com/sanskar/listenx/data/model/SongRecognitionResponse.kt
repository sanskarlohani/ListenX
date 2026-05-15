package com.sanskar.listenx.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for song recognition API
 * Maps backend JSON response to Kotlin data class
 * Backend JSON:
 * {
 *   "SongID": 601126672,
 *   "SongTitle": "The Way I Am",
 *   "SongArtist": "Charlie Puth",
 *   "YouTubeID": "EQnk-h-LCpQ",
 *   "Timestamp": 45000,
 *   "Score": 95.5
 * }
 */
data class SongRecognitionResponse(
    @SerializedName("SongID")
    val songId: Long,

    @SerializedName("SongTitle")
    val title: String,

    @SerializedName("SongArtist")
    val artist: String,

    @SerializedName("YouTubeID")
    val youtubeId: String,

    @SerializedName("Timestamp")
    val timestamp: Int,

    @SerializedName("Score")
    val score: Float
)

/**
 * Request model for song recognition
 */
data class FingerprintRequest(
    val fingerprint: Map<String, Int>  // hex hash -> timestamp
)
