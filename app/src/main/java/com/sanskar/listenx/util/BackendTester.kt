package com.sanskar.listenx.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Backend endpoint tester
 * Use this to test connectivity before running actual recognition
 */
object BackendTester {
    private const val TAG = "BackendTester"

    interface TestApi {
        @GET("health")
        suspend fun health(): Map<String, Any>

        @GET("match")
        suspend fun match(): Map<String, Any>
    }

    /**
     * Test if backend health endpoint is reachable
     */
    suspend fun testHealthEndpoint(baseUrl: String): TestResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔍 Testing health endpoint: $baseUrl/health")
                val startTime = System.currentTimeMillis()

                val api = createTestApi(baseUrl)
                val response = api.health()

                val elapsed = System.currentTimeMillis() - startTime
                Log.d(TAG, "✅ Health endpoint SUCCESS in ${elapsed}ms")
                Log.d(TAG, "   Response: $response")

                TestResult(
                    success = true,
                    message = "Health check passed",
                    responseTime = elapsed,
                    statusCode = 200,
                    details = response.toString()
                )
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis()
                Log.e(TAG, "❌ Health endpoint FAILED: ${e.message}")
                analysisAndLogError(e, "health")

                TestResult(
                    success = false,
                    message = "Health check failed: ${e.message}",
                    responseTime = elapsed,
                    statusCode = -1,
                    details = e.stackTraceToString()
                )
            }
        }
    }

    /**
     * Test if backend match endpoint is callable
     */
    suspend fun testMatchEndpoint(baseUrl: String): TestResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔍 Testing match endpoint: $baseUrl/match")
                val startTime = System.currentTimeMillis()

                val conn = URL("$baseUrl/match").openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                // Send empty fingerprint request
                val requestBody = """{"fingerprint":{}}"""
                val output = conn.outputStream
                output.write(requestBody.toByteArray())
                output.flush()
                output.close()

                val responseCode = conn.responseCode
                val elapsed = System.currentTimeMillis() - startTime

                val success = responseCode in 200..299
                if (success) {
                    Log.d(TAG, "✅ Match endpoint reachable (HTTP $responseCode in ${elapsed}ms)")
                } else {
                    Log.w(TAG, "⚠️ Match endpoint returned HTTP $responseCode in ${elapsed}ms")
                }

                conn.disconnect()

                TestResult(
                    success = success,
                    message = "Match endpoint returned HTTP $responseCode",
                    responseTime = elapsed,
                    statusCode = responseCode,
                    details = "Endpoint is reachable, but returned $responseCode (expected 200-299)"
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Match endpoint test FAILED: ${e.message}")
                analysisAndLogError(e, "match")

                TestResult(
                    success = false,
                    message = "Match endpoint test failed: ${e.message}",
                    responseTime = -1,
                    statusCode = -1,
                    details = e.stackTraceToString()
                )
            }
        }
    }

    /**
     * Run comprehensive backend test
     */
    suspend fun runFullBackendTest(baseUrl: String): List<TestResult> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<TestResult>()

            Log.d(TAG, """
                🧪 FULL BACKEND CONNECTIVITY TEST
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Backend URL: $baseUrl
                
            """.trimIndent())

            // Test health
            val healthResult = testHealthEndpoint(baseUrl)
            results.add(healthResult)
            Log.d(TAG, "1. Health Check: ${if (healthResult.success) "✅ PASS" else "❌ FAIL"}")

            // Test match
            val matchResult = testMatchEndpoint(baseUrl)
            results.add(matchResult)
            Log.d(TAG, "2. Match Check: ${if (matchResult.success) "✅ PASS" else "❌ FAIL"}")

            Log.d(TAG, "\n📊 Test Summary: ${results.count { it.success }}/${results.size} tests passed")

            results
        }
    }

    /**
     * Create test API without custom HTTP client
     */
    private fun createTestApi(baseUrl: String): TestApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TestApi::class.java)
    }

    /**
     * Analyze error and log recommendations
     */
    private fun analysisAndLogError(e: Exception, endpoint: String) {
        when (e) {
            is java.net.SocketTimeoutException -> {
                Log.e(TAG, """
                    ⏱️ TIMEOUT ERROR
                    Backend took too long to respond. Possible reasons:
                    • Backend is not running or crashed
                    • Backend is processing slowly (CPU/memory issue)
                    • Network congestion or high latency
                    
                    Action: Check backend logs and system resources
                """.trimIndent())
            }
            is java.net.ConnectException -> {
                Log.e(TAG, """
                    🔌 CONNECTION REFUSED
                    Cannot connect to backend. Possible reasons:
                    • Backend service is not running
                    • Wrong IP address or port
                    • Firewall blocking connection
                    • Network is down
                    
                    Action: Start backend, verify IP:port, check firewall
                """.trimIndent())
            }
            is java.net.UnknownHostException -> {
                Log.e(TAG, """
                    📡 DNS RESOLUTION FAILED
                    Cannot resolve backend address. Possible reasons:
                    • Wrong hostname in URL
                    • DNS server not responding
                    • Network is down
                    
                    Action: Check backend URL, verify DNS works
                """.trimIndent())
            }
            else -> {
                Log.e(TAG, """
                    ❌ UNKNOWN ERROR
                    Type: ${e.javaClass.simpleName}
                    Message: ${e.message}
                """.trimIndent())
            }
        }
    }
}

/**
 * Result of a single backend test
 */
data class TestResult(
    val success: Boolean,
    val message: String,
    val responseTime: Long,  // milliseconds, -1 if error
    val statusCode: Int,      // -1 if error
    val details: String
) {
    fun prettyPrint(): String {
        return """
            Status: ${if (success) "✅ PASS" else "❌ FAIL"}
            Message: $message
            Response Time: ${if (responseTime >= 0) "${responseTime}ms" else "N/A"}
            Status Code: ${if (statusCode >= 0) statusCode else "N/A"}
            Details: $details
        """.trimIndent()
    }
}




