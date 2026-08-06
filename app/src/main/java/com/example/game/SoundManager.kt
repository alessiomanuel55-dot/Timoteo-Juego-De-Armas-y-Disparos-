package com.example.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

class SoundManager {
    var isMuted: Boolean = false
    private val scope = CoroutineScope(Dispatchers.Default)
    private val random = Random()

    fun playLaserSound() {
        if (isMuted) return
        scope.launch {
            try {
                val sampleRate = 22050
                val duration = 0.10 // seconds
                val numSamples = (duration * sampleRate).toInt()
                val buffer = ShortArray(numSamples)

                var startFreq = 1400.0
                var endFreq = 250.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * t
                    val sampleValue = sin(2.0 * Math.PI * currentFreq * (i.toDouble() / sampleRate))
                    val envelope = 1.0 - t // linear decay
                    buffer[i] = (sampleValue * envelope * Short.MAX_VALUE * 0.4).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playExplosionSound() {
        if (isMuted) return
        scope.launch {
            try {
                val sampleRate = 22050
                val duration = 0.22 // seconds
                val numSamples = (duration * sampleRate).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val noise = (random.nextDouble() * 2.0 - 1.0)
                    val envelope = Math.pow(1.0 - t, 2.5) // exponential decay
                    buffer[i] = (noise * envelope * Short.MAX_VALUE * 0.5).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playLifeLostSound() {
        if (isMuted) return
        scope.launch {
            try {
                val sampleRate = 22050
                val duration = 0.35
                val numSamples = (duration * sampleRate).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val freq = 180.0 - (t * 110.0)
                    val squareWave = if (sin(2.0 * Math.PI * freq * (i.toDouble() / sampleRate)) > 0) 0.6 else -0.6
                    val envelope = 1.0 - t
                    buffer[i] = (squareWave * envelope * Short.MAX_VALUE * 0.5).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playComboSound(combo: Int) {
        if (isMuted) return
        scope.launch {
            try {
                val sampleRate = 22050
                val duration = 0.15
                val numSamples = (duration * sampleRate).toInt()
                val buffer = ShortArray(numSamples)

                val baseFreq = 800.0 + (combo * 150.0).coerceAtMost(1000.0)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val sampleValue = sin(2.0 * Math.PI * baseFreq * (i.toDouble() / sampleRate)) +
                            0.5 * sin(2.0 * Math.PI * (baseFreq * 1.5) * (i.toDouble() / sampleRate))
                    val envelope = 1.0 - t
                    buffer[i] = (sampleValue * envelope * Short.MAX_VALUE * 0.3).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    fun playGameOverSound() {
        if (isMuted) return
        scope.launch {
            try {
                val sampleRate = 22050
                val freqs = doubleArrayOf(523.25, 466.16, 392.00, 311.13) // C5, Bb4, G4, Eb4
                val durationPerNote = 0.12
                val totalSamples = (freqs.size * durationPerNote * sampleRate).toInt()
                val buffer = ShortArray(totalSamples)

                for (noteIdx in freqs.indices) {
                    val f = freqs[noteIdx]
                    val startSample = (noteIdx * durationPerNote * sampleRate).toInt()
                    val noteSamples = (durationPerNote * sampleRate).toInt()
                    for (i in 0 until noteSamples) {
                        val globalIdx = startSample + i
                        if (globalIdx < buffer.size) {
                            val t = i.toDouble() / noteSamples
                            val sample = sin(2.0 * Math.PI * f * (i.toDouble() / sampleRate))
                            val envelope = 1.0 - t
                            buffer[globalIdx] = (sample * envelope * Short.MAX_VALUE * 0.4).toInt().toShort()
                        }
                    }
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
    }
}
