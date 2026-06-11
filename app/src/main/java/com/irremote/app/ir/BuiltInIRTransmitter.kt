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
        val bits = mutableListOf<Int>()

        bits.addAll(listOf(9000, 4500))

        for (i in 0 until 32) {
            val bit = ((code shr i) and 1L).toInt()
            bits.add(562)
            bits.add(if (bit == 1) 1687 else 562)
        }

        bits.add(562)

        return bits.toIntArray()
    }
}
