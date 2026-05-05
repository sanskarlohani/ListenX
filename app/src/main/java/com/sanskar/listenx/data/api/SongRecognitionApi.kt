package com.sanskar.listenx.data.api

import com.sanskar.listenx.data.model.FingerprintRequest
import com.sanskar.listenx.data.model.SongRecognitionResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for song recognition
 */
interface SongRecognitionApi {

    @POST("fourio/match")
    suspend fun matchFingerprint(
        @Body request: FingerprintRequest
    ): List<SongRecognitionResponse>
}

