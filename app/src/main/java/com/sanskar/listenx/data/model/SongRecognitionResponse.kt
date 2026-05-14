package com.sanskar.listenx.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for song recognition API
 */
data class SongRecognitionResponse(
    @SerializedName("title")
    val title: String,

    @SerializedName("artist")
    val artist: String,

    @SerializedName("youtubeId")
    val youtubeId: String,

    @SerializedName("score")
    val score: Float,

    @SerializedName("album")
    val album: String? = null
)

/**
 * Request model for song recognition
 */
data class FingerprintRequest(
    val fingerprint: Map<String, Int>  // hex hash -> timestamp
)
