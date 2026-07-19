package com.sanskar.listenx.data.api

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton for Retrofit client initialization
 * Supports configurable backend URLs for different environments
 * Uses default OkHttp client with proper timeout configuration
 */
object RetrofitClient {
    private const val TAG = "RetrofitClient"

    // For Physical Device, use your computer's local IP address
    // Change this to your actual machine IP if needed
    private const val BASE_URL = "http://192.168.90.112:8000/"

    // Alternative URLs for quick testing:
    // - Local emulator: http://10.0.2.2:8000/
    // - Your IP: http://192.168.1.6:8000/
    // - Another machine: http://192.168.1.X:8000/

    // ============ TIMEOUT CONFIGURATION ============
    // Increase these values if backend is slow
    // Default Retrofit timeouts are quite short, so we rely on OkHttp defaults
    // For production, consider adding custom OkHttpClient
    // Current defaults: Connect=10s, Read=10s, Write=10s
    // If seeing timeouts, increase these in a custom OkHttpClient

    private var retrofit: Retrofit? = null

    fun getApi(): SongRecognitionApi {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            Log.i(TAG, "✅ Retrofit initialized successfully")
            Log.i(TAG, "   BASE_URL: $BASE_URL")
            Log.i(TAG, "   ⚠️ Using default OkHttp timeouts (10s each)")
            Log.i(TAG, "   💡 If seeing SocketTimeoutException, refer to DEBUGGING_GUIDE.md")
        }
        return retrofit!!.create(SongRecognitionApi::class.java)
    }

    /**
     * Reset client (useful for changing URLs at runtime)
     */
    fun resetClient() {
        retrofit = null
        Log.i(TAG, "🔄 Retrofit client reset")
    }

    /**
     * Get current BASE_URL (useful for debugging)
     */
    fun getBaseUrl(): String = BASE_URL
}

