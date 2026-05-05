package com.sanskar.listenx.util

import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.min

/**
 * Utility class for audio recording
 * Handles 10-second audio recording with PCM output
 */
class AudioRecorder {

    companion object {
        // Audio recording parameters
        const val SAMPLE_RATE = 44100  // 44.1 kHz
        const val CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT
        const val RECORDING_DURATION_MS = 10000  // 10 seconds
        const val BUFFER_SIZE_MULTIPLIER = 2
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    private var audioData: ShortArray? = null

    /**
     * Start recording audio
     * Returns immediately; recording happens in background thread
     */
    fun startRecording(): Boolean {
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return false
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize * BUFFER_SIZE_MULTIPLIER
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            return false
        }

        // Initialize audio data array
        val totalSamples = (SAMPLE_RATE * RECORDING_DURATION_MS) / 1000
        audioData = ShortArray(totalSamples)

        isRecording = true
        audioRecord?.startRecording()

        // Start recording in background thread
        recordingThread = Thread {
            recordAudioData()
        }
        recordingThread?.start()

        return true
    }

    /**
     * Stop recording and return audio data
     */
    fun stopRecording(): ShortArray? {
        isRecording = false
        audioRecord?.stop()
        recordingThread?.join()
        audioRecord?.release()
        audioRecord = null

        return audioData
    }

    /**
     * Internal method to read audio data from AudioRecord
     */
    private fun recordAudioData() {
        val record = audioRecord ?: return
        val data = audioData ?: return
        val bufSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )
        val buffer = ShortArray(bufSize)

        var totalSamplesRead = 0
        val maxSamples = data.size

        while (isRecording && totalSamplesRead < maxSamples) {
            val samplesRead = record.read(buffer, 0, min(buffer.size, maxSamples - totalSamplesRead))

            if (samplesRead > 0) {
                buffer.copyInto(data, totalSamplesRead, 0, samplesRead)
                totalSamplesRead += samplesRead
            }
        }
    }

    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean = isRecording
}


