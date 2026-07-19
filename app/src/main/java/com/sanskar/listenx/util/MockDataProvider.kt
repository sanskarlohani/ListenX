package com.sanskar.listenx.util

import com.sanskar.listenx.data.model.SongRecognitionResponse

/**
 * Mock data provider for testing without backend
 * Provides sample song recognition results
 */
object MockDataProvider {

    fun getMockSongs(): List<SongRecognitionResponse> {
        return listOf(
            SongRecognitionResponse(
                songId = 1L,
                title = "Blinding Lights",
                artist = "The Weeknd",
                youtubeId = "4NRXx6U8ABQ",
                timestamp = 45000,
                score = 95.2f
            ),
            SongRecognitionResponse(
                songId = 2L,
                title = "Levitating",
                artist = "Dua Lipa",
                youtubeId = "TUVcZfQe-Kw",
                timestamp = 32000,
                score = 88.5f
            ),
            SongRecognitionResponse(
                songId = 3L,
                title = "Good 4 U",
                artist = "Olivia Rodrigo",
                youtubeId = "-mKlSaQ1F1I",
                timestamp = 28000,
                score = 82.3f
            ),
            SongRecognitionResponse(
                songId = 4L,
                title = "Anti-Hero",
                artist = "Taylor Swift",
                youtubeId = "klRtG9m-OfI",
                timestamp = 50000,
                score = 91.7f
            ),
            SongRecognitionResponse(
                songId = 5L,
                title = "As It Was",
                artist = "Harry Styles",
                youtubeId = "H5v3kku4y5E",
                timestamp = 35000,
                score = 87.4f
            )
        )
    }
}

