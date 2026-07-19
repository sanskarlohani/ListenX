package com.sanskar.listenx.fingerprint

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioFingerprinterTest {

    private val fingerprinter = AudioFingerprinter()
    private val sampleRate = 44100

    @Test
    fun `generateFingerprint should return same hashes for same audio`() {
        // Create a simple sine wave or random noise
        val audioData = ShortArray(44100 * 2) { (Math.sin(it.toDouble() * 0.1) * 32767).toInt().toShort() }

        val fingerprint1 = fingerprinter.generateFingerprint(audioData, sampleRate)
        val fingerprint2 = fingerprinter.generateFingerprint(audioData, sampleRate)

        assertEquals("Fingerprints should be identical for same input", fingerprint1, fingerprint2)
    }

    @Test
    fun `generateFingerprint should return different hashes for different audio`() {
        val audioData1 = ShortArray(44100) { (Math.sin(it.toDouble() * 0.1) * 32767).toInt().toShort() }
        val audioData2 = ShortArray(44100) { (Math.sin(it.toDouble() * 0.2) * 32767).toInt().toShort() }

        val fingerprint1 = fingerprinter.generateFingerprint(audioData1, sampleRate)
        val fingerprint2 = fingerprinter.generateFingerprint(audioData2, sampleRate)

        assertNotEquals("Fingerprints should be different for different frequencies", fingerprint1, fingerprint2)
    }

    @Test
    fun `generateFingerprint should handle empty or silent audio`() {
        val silentAudio = ShortArray(44100) { 0 }
        val fingerprint = fingerprinter.generateFingerprint(silentAudio, sampleRate)

        // Silent audio should result in no peaks and thus no hashes
        assertTrue("Silent audio should produce empty fingerprint", fingerprint.isEmpty())
    }

    @Test
    fun `generateFingerprint should produce hashes within expected range`() {
        // Generate some non-trivial audio to ensure hashes are created
        val audioData = ShortArray(44100 * 3) { (Math.random() * 32767).toInt().toShort() }
        val fingerprint = fingerprinter.generateFingerprint(audioData, sampleRate)

        assertTrue("Should generate at least some hashes for random noise", fingerprint.isNotEmpty())
        
        // Verify hash format (should be decimal strings)
        fingerprint.keys.forEach { hash ->
            assertTrue("Hash should be a valid long decimal string: $hash", hash.toLongOrNull() != null)
        }
    }
}
