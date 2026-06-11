package com.irremote.app.ir

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JackIRTransmitter : IRTransmitter {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val carrierFreq = 38000
    private val sampleRate = 192000
    private val halfCycleSamples = sampleRate / carrierFreq / 2

    override fun transmit(hexCode: String) {
        scope.launch {
            val pcm = generatePcm(hexCode)
            playPcm(pcm)
        }
    }

    override fun isAvailable(): Boolean = true

    private fun generatePcm(hex: String): ShortArray {
        val cleanHex = hex.replace("0x", "").replace(" ", "")
        val fullHex = if (cleanHex.length == 6) {
            val addr = cleanHex.substring(0, 2)
            val cmd = cleanHex.substring(2, 4)
            val cmdInv = cleanHex.substring(4, 6)
            val addrInv = String.format("%02X", addr.toInt(16) xor 0xFF)
            addr + addrInv + cmd + cmdInv
        } else {
            cleanHex
        }
        val code = fullHex.toLong(16)
        val pcm = mutableListOf<Short>()

        fun carrier(durationUs: Int): List<Short> {
            val count = (sampleRate * durationUs / 1_000_000L).toInt()
            val buffer = mutableListOf<Short>()
            var phase = 0
            for (i in 0 until count) {
                val value = if (phase < halfCycleSamples) Short.MAX_VALUE else Short.MIN_VALUE
                buffer.add(value)
                phase++
                if (phase >= halfCycleSamples * 2) phase = 0
            }
            return buffer
        }

        fun space(durationUs: Int): List<Short> {
            val count = (sampleRate * durationUs / 1_000_000L).toInt()
            return List(count) { 0 }
        }

        pcm.addAll(carrier(9000))
        pcm.addAll(space(4500))

        for (i in 0 until 32) {
            val bit = ((code shr i) and 1L).toInt()
            pcm.addAll(carrier(562))
            pcm.addAll(space(if (bit == 1) 1687 else 562))
        }

        pcm.addAll(carrier(562))

        return pcm.toShortArray()
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
            Thread.sleep(durationMs + 50)
            track.stop()
        } finally {
            track.release()
        }
    }
}
