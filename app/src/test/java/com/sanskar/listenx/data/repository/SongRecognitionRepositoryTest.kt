package com.sanskar.listenx.data.repository

import com.sanskar.listenx.data.api.SongRecognitionApi
import com.sanskar.listenx.data.model.SongRecognitionResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.SocketTimeoutException

class SongRecognitionRepositoryTest {

    private val api = mockk<SongRecognitionApi>()
    private val repository = SongRecognitionRepository(api)

    @Test
    fun `matchFingerprint returns list of songs on success`() = runTest {
        val mockSongs = listOf(
            SongRecognitionResponse(1L, "Song 1", "Artist 1", "yt1", 0, 90f)
        )
        coEvery { api.matchFingerprint(any()) } returns mockSongs

        val result = repository.matchFingerprint(mapOf("hash" to 100))

        assertEquals(mockSongs, result)
    }

    @Test(expected = Exception::class)
    fun `matchFingerprint throws exception on timeout`() = runTest {
        coEvery { api.matchFingerprint(any()) } throws SocketTimeoutException("timeout")

        repository.matchFingerprint(mapOf("hash" to 100))
    }

    @Test
    fun `getTotalSongs returns correct count`() = runTest {
        coEvery { api.getTotalSongs() } returns mapOf("total" to 42)

        val result = repository.getTotalSongs()

        assertEquals(42, result)
    }
}
