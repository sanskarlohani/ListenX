package com.sanskar.listenx.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

/**
 * Network debugging utility for diagnosing connectivity issues
 * Helps identify: wrong URL, network down, backend not running, firewall issues, etc.
 */
object NetworkDebugUtil {
    private const val TAG = "NetworkDebug"

    /**
     * Check if device has internet connection
     */
    @SuppressLint("MissingPermission")
    fun isInternetAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            activeNetwork != null
        } catch (e: Exception) {
            Log.w(TAG, "Error checking internet availability: ${e.message}")
            false
        }
    }

    /**
     * Get current network type (WiFi/Mobile/etc)
     */
    @SuppressLint("MissingPermission")
    fun getNetworkType(context: Context): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            when {
                capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
                capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
                capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown (${e.message})"
        }
    }

    /**
     * Test basic DNS resolution (can resolve domain names)
     */
    suspend fun testDNS(hostname: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val address = InetAddress.getByName(hostname)
                Log.d(TAG, "✅ DNS Resolution SUCCESS: $hostname → ${address.hostAddress}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "❌ DNS Resolution FAILED for $hostname: ${e.message}")
                false
            }
        }
    }

    /**
     * Test if URL is reachable with TCP ping (socket connection)
     */
    suspend fun testTcpConnection(url: String, timeoutMs: Int = 5000): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val urlObj = URL(url)
                val start = System.currentTimeMillis()
                val conn = urlObj.openConnection() as HttpURLConnection
                conn.connectTimeout = timeoutMs
                conn.readTimeout = timeoutMs
                conn.requestMethod = "GET"
                conn.connect()
                val responseCode = conn.responseCode
                val elapsed = System.currentTimeMillis() - start
                conn.disconnect()

                Log.d(TAG, "✅ TCP Connection SUCCESS to $url (Response: $responseCode, Time: ${elapsed}ms)")
                true
            } catch (e: Exception) {
                Log.e(TAG, "❌ TCP Connection FAILED to $url: ${e.message}")
                false
            }
        }
    }

    /**
     * Test specific endpoint with HEAD request
     */
    suspend fun testEndpoint(url: String, timeoutMs: Int = 5000): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val start = System.currentTimeMillis()
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = timeoutMs
                conn.readTimeout = timeoutMs
                conn.requestMethod = "HEAD"
                conn.connect()
                val responseCode = conn.responseCode
                val elapsed = System.currentTimeMillis() - start
                conn.disconnect()

                val message = "✅ Endpoint reachable (HTTP $responseCode, ${elapsed}ms)"
                Log.d(TAG, message)
                true to message
            } catch (e: Exception) {
                val message = "❌ Endpoint unreachable: ${e.message}"
                Log.e(TAG, message)
                false to message
            }
        }
    }

    /**
     * Run comprehensive network diagnosis
     */
    @SuppressLint("MissingPermission")
    suspend fun runFullDiagnosis(context: Context, backendUrl: String): Map<String, String> {
        val results = mutableMapOf<String, String>()

        Log.d(TAG, "🔍 Starting comprehensive network diagnosis...\n")

        // 1. Check internet availability
        val hasInternet = isInternetAvailable(context)
        results["Internet Available"] = if (hasInternet) "✅ Yes" else "❌ No"
        Log.d(TAG, "1️⃣ Internet: ${results["Internet Available"]}")

        if (!hasInternet) {
            Log.e(TAG, "⚠️ Device has no internet connection. Check WiFi/cellular.")
            return results
        }

        // 2. Get network type
        val networkType = getNetworkType(context)
        results["Network Type"] = networkType
        Log.d(TAG, "2️⃣ Network Type: $networkType")

        // 3. Extract hostname from URL
        val urlObj = URL(backendUrl)
        val hostname = urlObj.host
        results["Backend Host"] = hostname
        Log.d(TAG, "3️⃣ Backend Host: $hostname")

        // 4. Test DNS resolution
        val dnsSuc = testDNS(hostname)
        results["DNS Resolution"] = if (dnsSuc) "✅ Yes" else "❌ No"
        Log.d(TAG, "4️⃣ DNS: ${results["DNS Resolution"]}")

        // 5. Test TCP connection to host
        val tcpSuc = testTcpConnection(backendUrl, 5000)
        results["TCP Connection"] = if (tcpSuc) "✅ Yes" else "❌ No"
        Log.d(TAG, "5️⃣ TCP Connection: ${results["TCP Connection"]}")

        // 6. Test actual endpoint
        val (endpointSuc, endpointMsg) = testEndpoint("$backendUrl/health", 5000)
        results["Endpoint /health"] = endpointMsg
        Log.d(TAG, "6️⃣ Endpoint: $endpointMsg")

        Log.d(TAG, "\n📊 Diagnosis Summary: ${results.values.count { it.startsWith("✅") }}/${results.size} checks passed")

        return results
    }

    /**
     * Log detailed error information for debugging
     */
    fun logTimeoutError(
        url: String,
        elapsedMs: Long,
        timeoutMs: Long,
        endpoint: String = "/match"
    ) {
        Log.e(TAG, """
            ⏱️ TIMEOUT ERROR ANALYSIS
            ━━━━━━━━━━━━━━━━━━━━━━
            ❌ Request to: $url$endpoint
            ⏱️ Elapsed: ${elapsedMs}ms
            ⏳ Timeout: ${timeoutMs}ms
            
            Possible causes:
            1. Backend not running or crashed
            2. Backend processing taking too long
            3. Network latency or packet loss
            4. Firewall/proxy blocking connection
            5. Wrong IP address or port
            6. Endpoint doesn't exist
            
            Debug steps:
            1. Check backend is running: curl -v http://${URL(url).host}:${URL(url).port}/health
            2. Check network: ping ${URL(url).host}
            3. Check all devices on same network
            4. Test from another device
            5. Review backend logs for errors
        """.trimIndent())
    }
}



