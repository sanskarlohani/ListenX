package com.sanskar.listenx.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton for Retrofit client initialization
 */
object RetrofitClient {
    // Backend base URL - update this with your actual server
    private const val BASE_URL = "https://api.yourdomain.com/"

    private lateinit var retrofit: Retrofit

    fun getApi(): SongRecognitionApi {
        if (!::retrofit.isInitialized) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit.create(SongRecognitionApi::class.java)
    }
}

