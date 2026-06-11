package com.irremote.app.ir

interface IRTransmitter {
    fun transmit(hexCode: String)
    fun isAvailable(): Boolean
}
