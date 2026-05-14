package com.sanskar.listenx.util

import android.Manifest
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
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
    @Volatile
    private var isRecording = false
    private var recordingThread: Thread? = null
    private var audioData: ShortArray? = null

    /**
     * Start recording audio
     * Returns immediately; recording happens in background thread
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Synchronized
    fun startRecording(): Boolean {
        // Stop any existing recording first
        if (isRecording) {
            stopRecording()
        }

        return try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )

            if (minBufferSize <= 0) {
                return false
            }

            val bufferSize = minBufferSize * BUFFER_SIZE_MULTIPLIER

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return false
            }

            // Initialize audio data array
            val totalSamples = (SAMPLE_RATE.toLong() * RECORDING_DURATION_MS / 1000).toInt()
            audioData = ShortArray(totalSamples)

            isRecording = true
            audioRecord?.startRecording()

            // Start recording in background thread
            recordingThread = Thread({
                try {
                    recordAudioData()
                } catch (e: Exception) {
                    isRecording = false
                }
            }, "AudioRecordingThread")
            recordingThread?.start()

            true
        } catch (e: Exception) {
            isRecording = false
            audioRecord?.release()
            audioRecord = null
            false
        }
    }

    /**
     * Stop recording and return audio data
     */
    @Synchronized
    fun stopRecording(): ShortArray? {
        if (!isRecording && audioRecord == null) return audioData
        
        isRecording = false
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            // Ignore stop errors
        }
        
        // Don't join from the main thread if we can avoid it, 
        // but here we need to ensure the thread is done before releasing resources
        recordingThread?.join(500) // Timeout join to prevent UI hang
        
        audioRecord?.release()
        audioRecord = null
        recordingThread = null

        return audioData
    }

    /**
     * Internal method to read audio data from AudioRecord
     */
    private fun recordAudioData() {
        val record = audioRecord ?: return
        val data = audioData ?: return
        
        val minBufSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )
        
        if (minBufSize <= 0) return
        
        val buffer = ShortArray(minBufSize)

        var totalSamplesRead = 0
        val maxSamples = data.size

        while (isRecording && totalSamplesRead < maxSamples) {
            val toRead = minOf(buffer.size, maxSamples - totalSamplesRead)
            val samplesRead = record.read(buffer, 0, toRead)

            if (samplesRead > 0) {
                buffer.copyInto(data, totalSamplesRead, 0, samplesRead)
                totalSamplesRead += samplesRead
            } else if (samplesRead < 0) {
                // Error occurred while reading
                break
            }
        }
    }

    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean = isRecording
}


