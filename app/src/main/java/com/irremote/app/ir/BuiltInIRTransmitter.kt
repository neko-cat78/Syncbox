package com.irremote.app.ir

import android.content.Context
import android.hardware.ConsumerIrManager

class BuiltInIRTransmitter(context: Context) : IRTransmitter {

    private val irManager: ConsumerIrManager? =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    override fun transmit(hexCode: String) {
        val ir = irManager ?: return
        if (!ir.hasIrEmitter()) return

        val freq = 38000
        val pattern = necHexToPattern(hexCode)
        ir.transmit(freq, pattern)
    }

    override fun isAvailable(): Boolean {
        return irManager?.hasIrEmitter() == true
    }

    private fun necHexToPattern(hex: String): IntArray {
        val cleanHex = hex.replace("0x", "").replace(" ", "")
        val code = cleanHex.toLong(16)
        val bits = mutableListOf<Int>()

        // NEC carrier frequency = 38kHz, period ≈ 26.3µs
        // Pulse = 562.5µs → ~21 pulses of 26.3µs → 21 * 26.3 ≈ 562.5µs
        // But ConsumerIrManager uses microsecond timing

        // NEC leader: 9000µs burst, 4500µs space
        bits.addAll(listOf(9000, 4500))

        // 32 bits: address (16), command (16)
        // NEC sends LSB first
        for (i in 0 until 32) {
            val bit = ((code shr i) and 1L).toInt()
            bits.add(562) // carrier burst
            bits.add(if (bit == 1) 1687 else 562) // space
        }

        // End burst
        bits.add(562)

        return bits.toIntArray()
    }
}
