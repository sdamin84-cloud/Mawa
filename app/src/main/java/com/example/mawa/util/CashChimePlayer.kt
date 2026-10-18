package com.example.mawa.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-fidelity pure synthesized cash register audio chime ("Cha-Ching / Bell Ting").
 * Uses AudioTrack with zero external assets, 100% offline and low latency.
 */
object CashChimePlayer {

    private val audioScope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays a crisp, satisfying mechanical cash-register chime (Ka-Ching!)
     */
    fun playCashChime(context: Context) {
        val prefs = context.getSharedPreferences("mawa_app_prefs", Context.MODE_PRIVATE)
        val isSoundEnabled = prefs.getBoolean("is_cash_sound_enabled", true)
        if (!isSoundEnabled) return

        audioScope.launch {
            try {
                val sampleRate = 44100
                val durationMs = 380
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val sample = if (t < 0.12) {
                        // First bell ding (approx 1760Hz & 2640Hz)
                        val decay = exp(-t * 22.0)
                        val w1 = sin(2.0 * PI * 1760.0 * t)
                        val w2 = 0.55 * sin(2.0 * PI * 2640.0 * t)
                        val w3 = 0.25 * sin(2.0 * PI * 3520.0 * t)
                        (w1 + w2 + w3) * decay
                    } else {
                        // Second crisp bright register chime (approx 2093Hz & 3136Hz)
                        val t2 = t - 0.12
                        val decay2 = exp(-t2 * 14.0)
                        val w1 = sin(2.0 * PI * 2093.0 * t2)
                        val w2 = 0.70 * sin(2.0 * PI * 3136.0 * t2)
                        val w3 = 0.35 * sin(2.0 * PI * 4186.0 * t2)
                        (w1 + w2 + w3) * decay2
                    }
                    val clamped = (sample * 0.75 * Short.MAX_VALUE).coerceIn(
                        Short.MIN_VALUE.toDouble(),
                        Short.MAX_VALUE.toDouble()
                    )
                    buffer[i] = clamped.toInt().toShort()
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                delay(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Throwable) {
                // Graceful fail-safe if device audio system is unavailable
            }
        }
    }
}
