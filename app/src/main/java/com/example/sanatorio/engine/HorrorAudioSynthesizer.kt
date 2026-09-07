package com.example.sanatorio.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

class HorrorAudioSynthesizer {
    private val sampleRate = 22050
    private var isPlaying = false
    private var loopJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val random = Random()

    var isMuted: Boolean = false
    var isFlickeringHum: Boolean = false
    var isWalking: Boolean = false

    private var audioTrack: AudioTrack? = null

    fun start() {
        if (isPlaying) return
        isPlaying = true

        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
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
                .setBufferSizeInBytes(minBufSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            loopJob = scope.launch {
                val chunkSize = 1024
                val buffer = ShortArray(chunkSize)
                var phaseDrone = 0.0
                var phaseHum = 0.0
                var stepTimer = 0

                while (isActive && isPlaying) {
                    if (isMuted) {
                        buffer.fill(0)
                    } else {
                        val droneFreq = 48.0 // 48 Hz ominous sub-bass
                        val humFreq = 120.0  // 120 Hz electrical buzz harmonic

                        for (i in 0 until chunkSize) {
                            phaseDrone += 2.0 * Math.PI * droneFreq / sampleRate
                            phaseHum += 2.0 * Math.PI * humFreq / sampleRate

                            val droneSample = sin(phaseDrone) * 3500.0

                            // Electrical buzz / spark jitter
                            val humSample = if (isFlickeringHum) {
                                (sin(phaseHum) * 2000.0) + (random.nextGaussian() * 1200.0)
                            } else {
                                0.0
                            }

                            val mixed = (droneSample + humSample).toInt().coerceIn(-32767, 32767).toShort()
                            buffer[i] = mixed
                        }

                        // Footstep tick generator
                        if (isWalking) {
                            stepTimer += chunkSize
                            if (stepTimer >= sampleRate * 0.45) { // every 450ms
                                stepTimer = 0
                                // Add step thud
                                for (k in 0 until 400) {
                                    val damp = (1.0 - (k / 400.0))
                                    buffer[k] = ((buffer[k] + (random.nextGaussian() * 8000.0 * damp))).toInt()
                                        .coerceIn(-32767, 32767).toShort()
                                }
                            }
                        }
                    }

                    audioTrack?.write(buffer, 0, chunkSize)
                }
            }
        } catch (_: Exception) {
            // AudioTrack fallback gracefully if device restricts audio
        }
    }

    fun playFlashlightClick() {
        if (isMuted) return
        scope.launch {
            try {
                // Short crisp dual-click impulse
                val clickTrack = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(2048)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                val numSamples = 600
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val env = (1.0 - (i.toDouble() / numSamples))
                    val freq = if (i < 200) 1800.0 else 900.0
                    val wave = sin(2.0 * Math.PI * freq * (i.toDouble() / sampleRate))
                    samples[i] = (wave * 22000.0 * env).toInt().toShort()
                }

                clickTrack.write(samples, 0, numSamples)
                clickTrack.play()
                delay(100)
                clickTrack.release()
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        isPlaying = false
        loopJob?.cancel()
        loopJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {}
    }
}
