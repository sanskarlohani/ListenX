package com.sanskar.listenx.fingerprint

import kotlin.math.*

/**
 * Audio fingerprinting engine using FFT and peak detection
 * Implements Shazam-like fingerprint generation
 *
 * Process:
 * 1. Downsample audio to 11025 Hz
 * 2. Apply FFT with 1024-sample window
 * 3. Generate spectrogram
 * 4. Detect peaks in frequency bands
 * 5. Generate anchor-target pairs and create hash
 */
class AudioFingerprinter {

    companion object {
        // Fingerprinting parameters
        const val TARGET_SAMPLE_RATE = 11025  // Downsampled rate
        const val FFT_WINDOW_SIZE = 1024
        const val OVERLAP_RATIO = 0.5f  // 50% overlap
        const val FREQUENCY_BANDS = 256

        // Peak detection parameters
        const val PEAK_NEIGHBORHOOD_SIZE = 10  // Frequency bins for local peak detection
        const val MIN_PEAK_AMPLITUDE = 10f  // Minimum amplitude for a peak

        // Hash parameters
        const val ANCHOR_TIME_RANGE = 70  // Time range for target pairs (in frames)
        const val HASH_MOD = 1000000007  // Large prime for hashing
    }

    /**
     * Generate fingerprint from raw audio samples
     * Returns map of hash -> timestamp pairs
     */
    fun generateFingerprint(audioSamples: ShortArray, originalSampleRate: Int): Map<Int, Int> {
        // Step 1: Downsample audio
        val downsampledAudio = downSampleAudio(audioSamples, originalSampleRate)

        // Step 2: Generate spectrogram using FFT
        val spectrogram = generateSpectrogram(downsampledAudio)

        // Step 3: Detect peaks in spectrogram
        val peakMap = detectPeaks(spectrogram)

        // Step 4: Generate hashes from anchor-target pairs
        val fingerprint = generateHashesFromPeaks(peakMap)

        return fingerprint
    }

    /**
     * Downsample audio from original sample rate to target rate
     */
    private fun downSampleAudio(audioSamples: ShortArray, originalSampleRate: Int): FloatArray {
        val downSamplingFactor = originalSampleRate / TARGET_SAMPLE_RATE
        val downsampledSize = audioSamples.size / downSamplingFactor
        val downsampledAudio = FloatArray(downsampledSize)

        for (i in downsampledAudio.indices) {
            val originalIndex = i * downSamplingFactor
            downsampledAudio[i] = audioSamples[originalIndex].toFloat() / 32768f
        }

        return downsampledAudio
    }

    /**
     * Generate spectrogram using FFT
     * Returns 2D array: rows = time frames, columns = frequency bins
     */
    private fun generateSpectrogram(audioSamples: FloatArray): Array<FloatArray> {
        val hopSize = (FFT_WINDOW_SIZE * (1 - OVERLAP_RATIO)).toInt()
        val numFrames = (audioSamples.size - FFT_WINDOW_SIZE) / hopSize + 1
        val spectrogram = Array(numFrames) { FloatArray(FFT_WINDOW_SIZE / 2) }

        for (frameIdx in 0 until numFrames) {
            val startIdx = frameIdx * hopSize
            val endIdx = minOf(startIdx + FFT_WINDOW_SIZE, audioSamples.size)

            // Extract frame
            val frame = FloatArray(FFT_WINDOW_SIZE)
            for (i in 0 until (endIdx - startIdx)) {
                frame[i] = audioSamples[startIdx + i]
            }

            // Apply Hann window
            applyHannWindow(frame)

            // Compute FFT
            val fftResult = computeFFT(frame)

            // Compute magnitude spectrum
            for (i in 0 until FFT_WINDOW_SIZE / 2) {
                val real = fftResult[2 * i]
                val imag = fftResult[2 * i + 1]
                spectrogram[frameIdx][i] = sqrt(real * real + imag * imag)
            }
        }

        return spectrogram
    }

    /**
     * Apply Hann window to audio frame
     */
    private fun applyHannWindow(frame: FloatArray) {
        for (i in frame.indices) {
            val hannValue = 0.5f * (1 - cos(2 * PI * i / (frame.size - 1))).toFloat()
            frame[i] *= hannValue
        }
    }

    /**
     * Simple FFT implementation (Cooley-Tukey)
     * Input: real signal alternating with zeros for imaginary
     * Output: complex spectrum [real0, imag0, real1, imag1, ...]
     */
    private fun computeFFT(frame: FloatArray): FloatArray {
        val n = frame.size
        val fft = FloatArray(n * 2)  // Real and imaginary parts

        // Initialize with input (imaginary part is 0)
        for (i in 0 until n) {
            fft[2 * i] = frame[i]
            fft[2 * i + 1] = 0f
        }

        // Bit reversal
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                // Swap real and imaginary parts
                fft.swap(2 * i, 2 * j)
                fft.swap(2 * i + 1, 2 * j + 1)
            }
            var k = n / 2
            while (k <= j) {
                j -= k
                k /= 2
            }
            j += k
        }

        // FFT computation
        var length = 2
        while (length <= n) {
            val angle = -2 * PI / length
            val wReal = cos(angle).toFloat()
            val wImag = sin(angle).toFloat()

            var i = 0
            while (i < n) {
                var wRe = 1f
                var wIm = 0f

                for (j in 0 until length / 2) {
                    val tReal = wRe * fft[2 * (i + j + length / 2)] - wIm * fft[2 * (i + j + length / 2) + 1]
                    val tImag = wRe * fft[2 * (i + j + length / 2) + 1] + wIm * fft[2 * (i + j + length / 2)]

                    fft[2 * (i + j + length / 2)] = fft[2 * (i + j)] - tReal
                    fft[2 * (i + j + length / 2) + 1] = fft[2 * (i + j) + 1] - tImag
                    fft[2 * (i + j)] += tReal
                    fft[2 * (i + j) + 1] += tImag

                    val wReTemp = wRe * wReal - wIm * wImag
                    wIm = wRe * wImag + wIm * wReal
                    wRe = wReTemp
                }

                i += length
            }
            length *= 2
        }

        return fft
    }

    /**
     * Helper function to swap array elements
     */
    private fun FloatArray.swap(i: Int, j: Int) {
        val temp = this[i]
        this[i] = this[j]
        this[j] = temp
    }

    /**
     * Detect peaks in spectrogram
     * Returns map of timeFrame -> list of (frequencyBin, amplitude) pairs
     */
    private fun detectPeaks(spectrogram: Array<FloatArray>): Map<Int, List<Pair<Int, Float>>> {
        val peakMap = mutableMapOf<Int, MutableList<Pair<Int, Float>>>()

        for (timeFrame in spectrogram.indices) {
            val frequencies = spectrogram[timeFrame]
            for (freqBin in PEAK_NEIGHBORHOOD_SIZE until frequencies.size - PEAK_NEIGHBORHOOD_SIZE) {
                val centerAmplitude = frequencies[freqBin]

                // Check if local maximum
                if (centerAmplitude > MIN_PEAK_AMPLITUDE) {
                    var isLocalMax = true
                    for (offset in -PEAK_NEIGHBORHOOD_SIZE..PEAK_NEIGHBORHOOD_SIZE) {
                        if (offset != 0 && frequencies[freqBin + offset] > centerAmplitude) {
                            isLocalMax = false
                            break
                        }
                    }

                    if (isLocalMax) {
                        peakMap.getOrPut(timeFrame) { mutableListOf() }
                            .add(Pair(freqBin, centerAmplitude))
                    }
                }
            }
        }

        return peakMap
    }

    /**
     * Generate hashes from detected peaks
     * Uses anchor-target pair approach
     */
    private fun generateHashesFromPeaks(peakMap: Map<Int, List<Pair<Int, Float>>>): Map<Int, Int> {
        val fingerprint = mutableMapOf<Int, Int>()

        for ((timeFrame, peaks) in peakMap) {
            for (anchor in peaks) {
                // Look for target peaks within time range
                for (targetTime in (timeFrame + 1)..minOf(timeFrame + ANCHOR_TIME_RANGE,
                    peakMap.keys.maxOrNull() ?: timeFrame)) {
                    peakMap[targetTime]?.forEach { target ->
                        // Create hash from anchor-target pair
                        val hash = createHash(
                            anchor.first,  // anchor frequency
                            target.first,  // target frequency
                            targetTime - timeFrame  // time difference
                        )

                        // Store hash with timestamp
                        fingerprint[hash] = timeFrame
                    }
                }
            }
        }

        return fingerprint
    }

    /**
     * Create hash from anchor, target, and time difference
     */
    private fun createHash(anchorFreq: Int, targetFreq: Int, timeDiff: Int): Int {
        // Simple hash combining the components
        val hash = (anchorFreq.toLong() * 1000L +
                   targetFreq.toLong() * 10L +
                   timeDiff.toLong()).toInt()
        return hash.rem(HASH_MOD)
    }
}

