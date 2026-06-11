package com.irremote.app.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log

class BuiltInIRTransmitter(context: Context) : IRTransmitter {

    private val irManager: ConsumerIrManager? =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    override fun transmit(hexCode: String) {
        val ir = irManager
        if (ir == null) {
            Log.w("BuiltInIR", "ConsumerIrManager not available")
            return
        }
        if (!ir.hasIrEmitter()) {
            Log.w("BuiltInIR", "No IR emitter")
            return
        }

        val pattern = necHexToPattern(hexCode)
        try {
            ir.transmit(38000, pattern)
            Log.d("BuiltInIR", "Transmitted: $hexCode -> ${pattern.contentToString()}")
        } catch (e: Exception) {
            Log.e("BuiltInIR", "Transmit failed: ${e.message}", e)
        }
    }

    override fun isAvailable(): Boolean {
        return irManager?.hasIrEmitter() == true
    }

    private fun necHexToPattern(hex: String): IntArray {
        val cleanHex = hex.replace("0x", "").replace(" ", "")
        val padded = cleanHex.padStart(8, '0')
        val code = padded.toLong(16)

        val bits = mutableListOf<Int>()

        bits.add(9000)
        bits.add(4500)

        for (i in 31 downTo 0) {
            val bit = ((code shr i) and 1L).toInt()
            bits.add(560)
            bits.add(if (bit == 1) 1690 else 560)
        }

        bits.add(560)

        return bits.toIntArray()
    }
}
