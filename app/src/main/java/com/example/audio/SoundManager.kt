package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var ambientJob: Job? = null
    private var isPlayingAmbient = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Plays a pleasant harmonic Tibetan-style or Zen chime sequence on session completion
     */
    fun playCompletionChime() {
        scope.launch {
            try {
                // Play harmonic bell frequencies (528Hz Love/Transformation frequency, 660Hz, 880Hz)
                val sampleRate = 44100
                val durationSeconds = 1.6
                val numSamples = (durationSeconds * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                val freqs = listOf(528.0, 660.0, 792.0, 1056.0)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Exponential decay envelope
                    val envelope = Math.exp(-2.2 * t)
                    var sampleVal = 0.0
                    for (f in freqs) {
                        sampleVal += sin(2.0 * Math.PI * f * t) * (1.0 / freqs.size)
                    }
                    samples[i] = (sampleVal * envelope * Short.MAX_VALUE * 0.7).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()

                // Release after playback
                delay((durationSeconds * 1000).toLong() + 200)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Fallback to ToneGenerator
                try {
                    val toneGen = ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 80)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Subtle soft click for timer start / tap
     */
    fun playClickSound() {
        scope.launch {
            try {
                val sampleRate = 22050
                val duration = 0.04
                val numSamples = (duration * sampleRate).toInt()
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val envelope = Math.exp(-60.0 * t)
                    val v = sin(2.0 * Math.PI * 880.0 * t) * envelope
                    samples[i] = (v * Short.MAX_VALUE * 0.4).toInt().toShort()
                }
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                track.play()
                delay(100)
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    /**
     * Subtle soft mechanical clock tick
     */
    fun playTickSound() {
        scope.launch {
            try {
                val sampleRate = 22050
                val duration = 0.02
                val numSamples = (duration * sampleRate).toInt()
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = Math.exp(-120.0 * t)
                    val v = sin(2.0 * Math.PI * 1200.0 * t) * env
                    samples[i] = (v * Short.MAX_VALUE * 0.25).toInt().toShort()
                }
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                track.play()
                delay(60)
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    fun vibrateSessionComplete() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 200, 150, 300, 150, 400)
                val amplitudes = intArrayOf(0, 180, 0, 220, 0, 255)
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (_: Exception) {}
    }

    fun vibrateClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (_: Exception) {}
    }

    fun startAmbient(type: String) {
        stopAmbient()
        if (type == "NONE") return

        isPlayingAmbient = true
        ambientJob = scope.launch {
            try {
                val sampleRate = 22050
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ) * 2

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                val chunk = ShortArray(1024)
                var phase = 0.0
                var brownNoise = 0.0

                while (isActive && isPlayingAmbient) {
                    when (type) {
                        "RAIN" -> {
                            // Pink/brown filtered noise with randomized drops
                            for (i in chunk.indices) {
                                val white = (Math.random() * 2.0 - 1.0)
                                brownNoise = (brownNoise + (0.02 * white)) / 1.02
                                val drop = if (Math.random() < 0.003) Math.sin(Math.random() * 10) * 0.4 else 0.0
                                val out = (brownNoise * 0.3 + drop) * Short.MAX_VALUE * 0.35
                                chunk[i] = out.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                            }
                        }
                        "WHITE_NOISE" -> {
                            // Soft soothing pink/brown noise
                            for (i in chunk.indices) {
                                val white = (Math.random() * 2.0 - 1.0)
                                brownNoise = (brownNoise + (0.05 * white)) / 1.05
                                val out = brownNoise * Short.MAX_VALUE * 0.3
                                chunk[i] = out.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                            }
                        }
                        "ZEN_BELLS" -> {
                            // Subtle ambient hum at 432 Hz
                            val freq = 432.0
                            val delta = 2.0 * Math.PI * freq / sampleRate
                            for (i in chunk.indices) {
                                phase += delta
                                val v = sin(phase) * 0.15 + sin(phase * 0.5) * 0.08
                                chunk[i] = (v * Short.MAX_VALUE).toInt().toShort()
                            }
                        }
                        else -> break
                    }
                    track.write(chunk, 0, chunk.size)
                }

                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    fun stopAmbient() {
        isPlayingAmbient = false
        ambientJob?.cancel()
        ambientJob = null
    }
}
