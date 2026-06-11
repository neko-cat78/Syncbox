package com.irremote.app.ir

import android.content.Context
import android.hardware.ConsumerIrManager

class BuiltInIRTransmitter(context: Context) : IRTransmitter {

    private val irManager: ConsumerIrManager? =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    override fun transmit(hexCode: String) {
        val ir = irManager ?: return
        if (!ir.hasIrEmitter()) return

        val pattern = necHexToPattern(hexCode)
        ir.transmit(38000, pattern)
    }

    override fun isAvailable(): Boolean {
        return irManager?.hasIrEmitter() == true
    }

    private fun necHexToPattern(hex: String): IntArray {
        val cleanHex = hex.replace("0x", "").replace(" ", "")
        val padded = cleanHex.padStart(8, '0')
        val code = padded.toLong(16)

        val bits = mutableListOf<Int>()

        // NEC leader: 9000µs burst, 4500µs space
        bits.add(9000)
        bits.add(4500)

        // 32 bits MSB-first
        for (i in 31 downTo 0) {
            val bit = ((code shr i) and 1L).toInt()
            bits.add(560)
            bits.add(if (bit == 1) 1690 else 560)
        }

        // End burst
        bits.add(560)

        return bits.toIntArray()
    }
}
