package com.sanskar.listenx.data.api

import com.sanskar.listenx.data.model.FingerprintRequest
import com.sanskar.listenx.data.model.SongRecognitionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit API interface for song recognition
 */
interface SongRecognitionApi {

    /**
     * Send fingerprint to backend for song matching
     * Backend returns: [{"title": "...", "artist": "...", ...}, ...]
     */
    @POST("fourio/match")
    suspend fun matchFingerprint(
        @Body request: FingerprintRequest
    ): List<SongRecognitionResponse>

    @GET("songs/total")
    suspend fun getTotalSongs(): Map<String, Int>

    /**
     * Download and index a song from Spotify/YouTube URL
     * Supports both Spotify track URLs and YouTube video URLs
     * Backend handles downloading, fingerprinting, and database storage
     * @param url URL to a Spotify track or YouTube video
     * @return Response with status and song info
     */
    @POST("songs/download")
    suspend fun downloadSong(
        @Query("url") url: String
    ): Map<String, Any>

}

