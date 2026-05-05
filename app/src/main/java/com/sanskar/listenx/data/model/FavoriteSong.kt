package com.sanskar.listenx.data.model

/**
 * Model for saved favorite songs
 */
data class FavoriteSong(
    val id: String,
    val title: String,
    val artist: String,
    val youtubeId: String,
    val score: Float,
    val album: String? = null,
    val savedAt: Long = System.currentTimeMillis()
)

