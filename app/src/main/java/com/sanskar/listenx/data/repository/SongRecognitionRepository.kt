package com.sanskar.listenx.data.repository

import com.sanskar.listenx.data.api.RetrofitClient
import com.sanskar.listenx.data.model.FingerprintRequest
import com.sanskar.listenx.data.model.SongRecognitionResponse

/**
 * Repository for song recognition - handles all API calls
 * Following Repository pattern from MVVM
 */
class SongRecognitionRepository {
    private val api = RetrofitClient.getApi()

    /**
     * Send audio fingerprint to backend for song matching
     *
     * @param fingerprint Map of hash -> timestamp pairs
     * @return List of matched songs
     */
    suspend fun matchFingerprint(fingerprint: Map<Int, Int>): List<SongRecognitionResponse> {
        return try {
            val request = FingerprintRequest(fingerprint)
            api.matchFingerprint(request)
        } catch (e: Exception) {
            throw Exception("Failed to match fingerprint: ${e.message}")
        }
    }
}

