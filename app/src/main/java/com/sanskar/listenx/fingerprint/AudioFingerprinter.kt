package com.sanskar.listenx.fingerprint

import android.util.Log
import kotlin.math.*

/**
 * Audio fingerprinting engine aligned with Python backend logic.
 */
class AudioFingerprinter {

    companion object {
        private const val TAG = "AudioFingerprinter"
        // DSP Constants from Python backend
        const val DSP_RATIO = 4
        const val FREQ_BIN_SIZE = 1024
        const val MAX_FREQ_CUTOFF = 5000.0
        const val HOP_SIZE = FREQ_BIN_SIZE / 32 // 32

        // Fingerprint Constants
        const val ANCHOR_FREQ_SHIFT = 23
        const val TARGET_FREQ_SHIFT = 14
        const val TARGET_ZONE_SIZE = 5

        // Frequency bands for peak extraction
        val BANDS = listOf(
            0 to 10, 10 to 20, 20 to 40, 40 to 80,
            80 to 160, 160 to 512
        )
    }

    data class Peak(val time: Double, val freqIdx: Int, val complexValue: Pair<Float, Float>)

    /**
     * Generate fingerprint from raw audio samples (ShortArray)
     * Returns Map with decimal string keys (e.g., "2147483648") and timestamp values
     */
    fun generateFingerprint(audioSamples: ShortArray, originalSampleRate: Int): Map<String, Int> {
        Log.d(TAG, "Starting fingerprinting. Input size: ${audioSamples.size} samples.")
        // 1. Convert to float samples [-1.0, 1.0] (WavBytesToSamples equivalent)
        val samples = FloatArray(audioSamples.size) { audioSamples[it].toFloat() / 32768.0f }
        Log.d(TAG, "Converted to normalized float samples.")

        // 2. Low Pass Filter
        val filteredSamples = lowPassFilter(MAX_FREQ_CUTOFF, originalSampleRate.toDouble(), samples)
        Log.d(TAG, "Low-pass filter applied.")

        // 3. Downsample
        val targetSampleRate = originalSampleRate / DSP_RATIO
        val downsampledSamples = downsample(filteredSamples, originalSampleRate, targetSampleRate)
        Log.d(TAG, "Downsampled to $targetSampleRate Hz. Output size: ${downsampledSamples.size} samples.")

        // 4. Spectrogram (STFT)
        val spectrogram = generateSpectrogram(downsampledSamples)
        Log.d(TAG, "Spectrogram generated: ${spectrogram.size} windows.")

        // 5. Extract Peaks
        val audioDuration = audioSamples.size.toDouble() / originalSampleRate
        val peaks = extractPeaks(spectrogram, audioDuration)
        Log.d(TAG, "Peaks extracted: ${peaks.size} peaks found.")

        // 6. Fingerprint (Hashing)
        val fingerprint = generateHashesFromPeaks(peaks)
        Log.d(TAG, "Fingerprint generated: ${fingerprint.size} hashes.")
        
        return fingerprint
    }

    private fun lowPassFilter(cutoff: Double, sampleRate: Double, input: FloatArray): FloatArray {
        val rc = 1.0 / (2.0 * PI * cutoff)
        val dt = 1.0 / sampleRate
        val alpha = (dt / (rc + dt)).toFloat()

        val output = FloatArray(input.size)
        var prevOutput = 0.0f
        for (i in input.indices) {
            if (i == 0) {
                output[i] = input[i] * alpha
            } else {
                output[i] = alpha * input[i] + (1.0f - alpha) * prevOutput
            }
            prevOutput = output[i]
        }
        return output
    }

    private fun downsample(input: FloatArray, originalRate: Int, targetRate: Int): FloatArray {
        val ratio = originalRate / targetRate
        val outputSize = input.size / ratio
        val output = FloatArray(outputSize)

        for (i in 0 until outputSize) {
            var sum = 0.0f
            for (j in 0 until ratio) {
                sum += input[i * ratio + j]
            }
            output[i] = sum / ratio
        }
        return output
    }

    private fun generateSpectrogram(samples: FloatArray): List<Array<Pair<Float, Float>>> {
        val numWindows = (samples.size - FREQ_BIN_SIZE) / HOP_SIZE + 1
        if (numWindows <= 0) return emptyList()

        val spectrogram = mutableListOf<Array<Pair<Float, Float>>>()

        // Hamming window: 0.54 - 0.46 * cos(2 * PI * n / (N - 1))
        val windowFunc = FloatArray(FREQ_BIN_SIZE) { i ->
            (0.54 - 0.46 * cos(2.0 * PI * i / (FREQ_BIN_SIZE - 1))).toFloat()
        }

        for (i in 0 until numWindows) {
            val start = i * HOP_SIZE
            val frame = FloatArray(FREQ_BIN_SIZE) { j ->
                samples[start + j] * windowFunc[j]
            }

            val fftResult = computeFFT(frame)
            val complexBins = Array(FREQ_BIN_SIZE) { j ->
                fftResult[2 * j] to fftResult[2 * j + 1]
            }
            spectrogram.add(complexBins)
        }

        return spectrogram
    }

    private fun extractPeaks(spectrogram: List<Array<Pair<Float, Float>>>, audioDuration: Double): List<Peak> {
        if (spectrogram.isEmpty()) return emptyList()

        val peaks = mutableListOf<Peak>()
        val binDuration = audioDuration / spectrogram.size

        for (binIdx in spectrogram.indices) {
            val binData = spectrogram[binIdx]
            val binBandMaxies = mutableListOf<Triple<Float, Pair<Float, Float>, Int>>() // mag, complex, idx

            for (band in BANDS) {
                var maxMag = -1.0f
                var maxIdx = -1
                for (fIdx in band.first until minOf(band.second, binData.size)) {
                    val complex = binData[fIdx]
                    val mag = sqrt(complex.first * complex.first + complex.second * complex.second)
                    if (mag > maxMag) {
                        maxMag = mag
                        maxIdx = fIdx
                    }
                }

                if (maxIdx != -1 && maxMag > 0.0f) {
                    binBandMaxies.add(Triple(maxMag, binData[maxIdx], maxIdx))
                }
            }

            if (binBandMaxies.isEmpty()) continue

            val avgMagnitude = binBandMaxies.map { it.first }.average().toFloat()

            for (info in binBandMaxies) {
                if (info.first > avgMagnitude) {
                    val peakTimeInBin = info.third.toDouble() * binDuration / FREQ_BIN_SIZE
                    val peakTime = binIdx.toDouble() * binDuration + peakTimeInBin
                    peaks.add(Peak(peakTime, info.third, info.second))
                }
            }
        }
        return peaks
    }

    private fun generateHashesFromPeaks(peaks: List<Peak>): Map<String, Int> {
        val fingerprints = mutableMapOf<String, Int>()

        for (i in peaks.indices) {
            val anchor = peaks[i]
            val endIndex = minOf(peaks.size, i + TARGET_ZONE_SIZE + 1)

            for (j in i + 1 until endIndex) {
                val target = peaks[j]
                val address = createAddress(anchor, target)
                val anchorTimeMs = (anchor.time * 1000).toInt()
                // Convert address to decimal string
                fingerprints[address.toString()] = anchorTimeMs
            }
        }
        return fingerprints
    }

    private fun createAddress(anchor: Peak, target: Peak): Int {
        val anchorFreq = anchor.freqIdx
        val targetFreq = target.freqIdx
        val deltaMs = ((target.time - anchor.time) * 1000).toInt()

        // Mask deltaMs to 14 bits to be safe, although it should be small
        val maskedDelta = deltaMs and 0x3FFF

        return (anchorFreq shl ANCHOR_FREQ_SHIFT) or (targetFreq shl TARGET_FREQ_SHIFT) or maskedDelta
    }

    /**
     * Simple FFT implementation (Cooley-Tukey)
     */
    private fun computeFFT(frame: FloatArray): FloatArray {
        val n = frame.size
        val fft = FloatArray(n * 2)

        for (i in 0 until n) {
            fft[2 * i] = frame[i]
            fft[2 * i + 1] = 0f
        }

        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                val tempR = fft[2 * i]
                val tempI = fft[2 * i + 1]
                fft[2 * i] = fft[2 * j]
                fft[2 * i + 1] = fft[2 * j + 1]
                fft[2 * j] = tempR
                fft[2 * j + 1] = tempI
            }
            var k = n / 2
            while (k <= j) {
                j -= k
                k /= 2
            }
            j += k
        }

        var length = 2
        while (length <= n) {
            val angle = -2 * PI / length
            val wReal = cos(angle).toFloat()
            val wImag = sin(angle).toFloat()

            var i = 0
            while (i < n) {
                var wRe = 1f
                var wIm = 0f

                for (idx in 0 until length / 2) {
                    val evenIdx = 2 * (i + idx)
                    val oddIdx = 2 * (i + idx + length / 2)

                    val tReal = wRe * fft[oddIdx] - wIm * fft[oddIdx + 1]
                    val tImag = wRe * fft[oddIdx + 1] + wIm * fft[oddIdx]

                    fft[oddIdx] = fft[evenIdx] - tReal
                    fft[oddIdx + 1] = fft[evenIdx + 1] - tImag
                    fft[evenIdx] += tReal
                    fft[evenIdx + 1] += tImag

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
}
