package com.irremote.app.data

import android.content.Context
import com.irremote.app.model.ColorButton
import com.irremote.app.model.RemoteConfig
import com.irremote.app.model.RemoteMetadata
import org.json.JSONObject

object JsonLoader {

    fun loadRemoteConfig(context: Context): RemoteConfig {
        val jsonString = context.assets.open("switch.json")
            .bufferedReader()
            .use { it.readText() }

        val json = JSONObject(jsonString)
        val meta = json.getJSONObject("remote_metadata")
        val metadata = RemoteMetadata(
            protocol = meta.getString("protocol"),
            commonChipset = meta.getString("common_chipset"),
            totalButtons = meta.getInt("total_buttons"),
            staticColorsSupported = meta.getInt("static_colors_supported"),
            inputCodeProvided = meta.getString("input_code_provided")
        )

        val colorsArray = json.getJSONArray("supported_colors")
        val colors = mutableListOf<ColorButton>()
        for (i in 0 until colorsArray.length()) {
            val obj = colorsArray.getJSONObject(i)
            colors.add(
                ColorButton(
                    row = obj.getInt("row"),
                    buttonName = obj.getString("button_name"),
                    irHexCode = obj.getString("ir_hex_code"),
                    rgbMixDescription = obj.getString("rgb_mix_description"),
                    hexColorValue = obj.getString("hex_color_value")
                )
            )
        }

        val limits = json.getJSONObject("hardware_maximum_color_limit")
        val hardwareLimit = mapOf(
            "hardware_remote_preset_buttons" to limits.getInt("hardware_remote_preset_buttons"),
            "strip_hardware_capability_24bit_rgb" to limits.getInt("strip_hardware_capability_24bit_rgb")
        )

        return RemoteConfig(
            remoteMetadata = metadata,
            supportedColors = colors,
            hardwareMaxColorLimit = hardwareLimit
        )
    }
}
