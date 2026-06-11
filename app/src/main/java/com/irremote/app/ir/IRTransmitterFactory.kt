package com.irremote.app.ir

import android.content.Context

object IRTransmitterFactory {

    private var builtIn: BuiltInIRTransmitter? = null
    private var jack: JackIRTransmitter? = null

    fun getTransmitter(mode: IRMode, context: Context): IRTransmitter {
        return when (mode) {
            IRMode.BUILT_IN -> {
                if (builtIn == null) {
                    builtIn = BuiltInIRTransmitter(context.applicationContext)
                }
                builtIn!!
            }
            IRMode.JACK_3_5MM -> {
                if (jack == null) {
                    jack = JackIRTransmitter()
                }
                jack!!
            }
        }
    }
}
