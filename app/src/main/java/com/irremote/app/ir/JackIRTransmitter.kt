package com.irremote.app.ir

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class JackIRTransmitter : IRTransmitter {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val sampleRate = 48000

    override fun transmit(hexCode: String) {
        scope.launch {
            val pcm = generatePcm(hexCode)
            playPcm(pcm)
        }
    }

    override fun isAvailable(): Boolean = true

    private fun generatePcm(hex: String): ShortArray {
        val cleanHex = hex.replace("0x", "").replace(" ", "")
        val padded = cleanHex.padStart(8, '0')
        val code = padded.toLong(16)

        // Build NEC pattern (microsecond timings) MSB-first
        val patternUs = mutableListOf<Int>()
        patternUs.add(9000)
        patternUs.add(4500)
        for (i in 31 downTo 0) {
            val bit = ((code shr i) and 1L).toInt()
            patternUs.add(560)
            patternUs.add(if (bit == 1) 1690 else 560)
        }
        patternUs.add(560)

        // Convert to PCM: sine wave at carrierHz/2 during ON periods, silence during OFF
        val carrierHz = 38000
        val toneHz = carrierHz / 2
        val phaseInc = (toneHz.toDouble() * 2.0 * PI) / sampleRate.toDouble()

        var totalFrames = 0L
        for (d in patternUs) {
            totalFrames += (d.toLong() * sampleRate) / 1_000_000L
        }
        val totalSamples = totalFrames.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val pcm = ShortArray(totalSamples)

        var idx = 0
        var on = true
        for (d in patternUs) {
            val frames = (d.toLong() * sampleRate / 1_000_000L).toInt()
            if (frames <= 0) {
                on = !on
                continue
            }
            if (on) {
                var phase = 0.0
                for (i in 0 until frames) {
                    if (idx >= totalSamples) break
                    pcm[idx++] = (sin(phase) * 32000.0).toInt().toShort()
                    phase += phaseInc
                    if (phase >= 2.0 * PI) phase -= 2.0 * PI
                }
            } else {
                for (i in 0 until frames) {
                    if (idx >= totalSamples) break
                    pcm[idx++] = 0
                }
            }
            on = !on
        }

        return pcm
    }

    private fun playPcm(pcm: ShortArray) {
        val bufferSize = maxOf(
            AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT),
            pcm.size * 2
        )

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(pcm, 0, pcm.size)
            track.play()

            val durationMs = (pcm.size.toLong() * 1000) / sampleRate
            Thread.sleep(durationMs + 200)
            track.stop()
        } finally {
            track.release()
        }
    }
}
