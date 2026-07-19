package com.sanskar.listenx.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response wrapper for fingerprint matching
 * Wraps the list of matched songs returned from backend
 */
data class MatchResponse(
    @SerializedName("results")
    val results: List<SongRecognitionResponse> = emptyList(),

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null
)

