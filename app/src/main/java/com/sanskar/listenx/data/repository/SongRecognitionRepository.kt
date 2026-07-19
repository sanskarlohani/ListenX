package com.sanskar.listenx.data.repository

import android.util.Log
import com.google.gson.Gson
import com.sanskar.listenx.data.api.RetrofitClient
import com.sanskar.listenx.data.api.SongRecognitionApi
import com.sanskar.listenx.data.model.FingerprintRequest
import com.sanskar.listenx.data.model.SongRecognitionResponse
import com.sanskar.listenx.util.NetworkDebugUtil
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Repository for song recognition - handles all API calls
 * Following Repository pattern from MVVM
 * Includes comprehensive error handling and logging for debugging
 */
class SongRecognitionRepository(private val api: SongRecognitionApi = RetrofitClient.getApi()) {
    private val gson = Gson()
    private val TAG = "SongRecognitionRepo"

    /**
     * Send audio fingerprint to backend for song matching
     *
     * @param fingerprint Map of hash (decimal string) -> timestamp pairs
     * @return List of matched songs
     */
    suspend fun matchFingerprint(fingerprint: Map<String, Int>): List<SongRecognitionResponse> {
        return try {
            // Fingerprint is already in correct format: Map<String, Int> with decimal string keys
            val request = FingerprintRequest(fingerprint)

            // Log the JSON payload for debugging
            val jsonPayload = gson.toJson(request)
            Log.d(TAG, " Sending fingerprint request to fourio/match")
            Log.d(TAG, "   Hashes count: ${fingerprint.size}")
            Log.d(TAG, "   Sample hashes: ${fingerprint.keys.take(3)}")
            Log.d(TAG, "   Request payload size: ${jsonPayload.length} bytes")
            Log.v(TAG, "Full request JSON (first 500 chars): ${jsonPayload.take(500)}")

            val startTime = System.currentTimeMillis()
            val results = api.matchFingerprint(request)
            val elapsed = System.currentTimeMillis() - startTime

            // Backend returns array directly
            Log.d(TAG, "✅ Fingerprint match SUCCESS in ${elapsed}ms")
            Log.d(TAG, "   Returned ${results.size} song(s)")
            
            // Log raw score values from backend for debugging
            results.forEachIndexed { index, song ->
                Log.d(TAG, "   Song $index: ${song.title} - Raw Score: ${song.score}, Artist: ${song.artist}")
            }
            
            results

        } catch (e: SocketTimeoutException) {
            // TIMEOUT ERROR - most common issue
            val message = "⏱️ SocketTimeoutException: Request timeout"
            Log.e(TAG, message)
            logTimeoutDebugInfo(e)
            throw Exception("$message: ${e.message}")

        } catch (e: ConnectException) {
            // CONNECTION ERROR - backend not reachable
            val message = " ConnectException: Cannot connect to backend"
            Log.e(TAG, message)
            Log.e(TAG, "   Possible causes:")
            Log.e(TAG, "   1. Backend server is not running")
            Log.e(TAG, "   2. Wrong IP address or port")
            Log.e(TAG, "   3. Network is down or isolated")
            Log.e(TAG, "   4. Firewall blocking connection")
            Log.e(TAG, "   Error: ${e.message}")
            throw Exception("$message: ${e.message}")

        } catch (e: UnknownHostException) {
            // DNS ERROR - cannot resolve hostname
            val message = " UnknownHostException: Cannot resolve backend address"
            Log.e(TAG, message)
            Log.e(TAG, "   Backend URL: ${RetrofitClient.getBaseUrl()}")
            Log.e(TAG, "   Check: Is DNS working? Is hostname correct?")
            Log.e(TAG, "   Error: ${e.message}")
            throw Exception("$message: ${e.message}")

        } catch (e: HttpException) {
            // HTTP ERROR (4xx, 5xx)
            val errorBody = try {
                e.response()?.errorBody()?.string() ?: "No error body"
            } catch (ex: Exception) {
                "Could not read error body: ${ex.message}"
            }

            val message = " HTTP Error ${e.code()}"
            Log.e(TAG, message)
            Log.e(TAG, "   Message: ${e.message()}")
            Log.e(TAG, "   Error Body: $errorBody")
            Log.e(TAG, "   Endpoint: ${RetrofitClient.getBaseUrl()}fourio/match")

            when (e.code()) {
                400 -> Log.e(TAG, "   → Bad Request: Invalid fingerprint format or data")
                401 -> Log.e(TAG, "   → Unauthorized: Missing authentication")
                403 -> Log.e(TAG, "   → Forbidden: Access denied")
                404 -> Log.e(TAG, "   → Not Found: Endpoint /fourio/match doesn't exist at backend")
                500 -> Log.e(TAG, "   → Internal Server Error: Backend crashed or endpoint error")
                502 -> Log.e(TAG, "   → Bad Gateway: Reverse proxy/load balancer issue")
                503 -> Log.e(TAG, "   → Service Unavailable: Backend temporarily down")
            }

            throw Exception("$message: ${e.message()}. Body: $errorBody")

        } catch (e: Exception) {
            // UNKNOWN ERROR
            val message = "❌ Unexpected Error"
            Log.e(TAG, message)
            Log.e(TAG, "   Type: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Message: ${e.message}")
            Log.e(TAG, "   Stack trace:", e)
            throw Exception("$message: ${e.message}")
        }
    }

    /**
     * Test backend connectivity
     * @return true if backend is reachable, false otherwise
     */
    suspend fun testBackendConnectivity(): Boolean {
        return try {
            Log.d(TAG, " Testing backend connectivity...")
            val response = api.getTotalSongs()
            val isReachable = response.isNotEmpty()
            if (isReachable) {
                Log.d(TAG, "✅ Backend is reachable")
            } else {
                Log.w(TAG, "⚠️ Backend responded but with empty data")
            }
            isReachable
        } catch (e: Exception) {
            Log.e(TAG, "❌ Backend is NOT reachable: ${e.message}")
            false
        }
    }

    /**
     * Get total number of songs in the database
     */
    suspend fun getTotalSongs(): Int {
        return try {
            val response = api.getTotalSongs()
            response["total"] ?: response["count"] ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total songs: ${e.message}")
            0
        }
    }

    /**
     * Download and index a song from Spotify/YouTube URL
     * @param url Spotify track URL or YouTube video URL
     * @return Song metadata including title, artist, youtube_id
     */
    suspend fun downloadSong(url: String): Map<String, Any> {
        return try {
            Log.d(TAG, " Downloading song from URL: $url")
            val response = api.downloadSong(url)
            Log.d(TAG, "✅ Song downloaded successfully")
            Log.d(TAG, "   Response: $response")
            response
        } catch (e: HttpException) {
            val errorBody = try {
                e.response()?.errorBody()?.string() ?: "No error body"
            } catch (ex: Exception) {
                "Could not read error body"
            }
            Log.e(TAG, "❌ HTTP Error ${e.code()}: $errorBody")
            when (e.code()) {
                400 -> throw Exception("Invalid URL format. Use Spotify track or YouTube video URL")
                404 -> throw Exception("URL not found or invalid")
                429 -> throw Exception("Too many requests. Please wait before trying again")
                500 -> throw Exception("Backend server error. Please try again later")
                else -> throw Exception("Download failed: ${e.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error downloading song: ${e.message}")
            throw Exception("Failed to download song: ${e.message}")
        }
    }

    /**
     * Log detailed timeout debugging information
     */
    private fun logTimeoutDebugInfo(exception: SocketTimeoutException) {
        Log.e(TAG, """
            ⏱️ TIMEOUT ERROR DETAILS
            ━━━━━━━━━━━━━━━━━━━━━━
            Message: ${exception.message}

            This means:
            • Request was sent to backend
            • Backend didn't respond within timeout period

            Possible causes:
            1. ️ Backend not running or crashed
            2. ⏳ Backend is too slow to process
            3.  Network latency/congestion
            4.  Firewall blocking connection
            5.  Wrong IP/port (connection hangs instead of refusing)
            6.  Broken network connection

            How to debug:
            1. Check if backend process is alive
            2. Test endpoint from another device
            3. Check backend CPU/memory usage
            4. Review backend logs for errors
            5. Test ping to backend IP
            6. Check network for packet loss

            Backend URL: ${RetrofitClient.getBaseUrl()}
        """.trimIndent())
     }
 }
