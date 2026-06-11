package com.irremote.app.model

data class ColorButton(
    val row: Int,
    val buttonName: String,
    val irHexCode: String,
    val rgbMixDescription: String,
    val hexColorValue: String
)

data class RemoteMetadata(
    val protocol: String,
    val commonChipset: String,
    val totalButtons: Int,
    val staticColorsSupported: Int,
    val inputCodeProvided: String
)

data class RemoteConfig(
    val remoteMetadata: RemoteMetadata,
    val supportedColors: List<ColorButton>,
    val hardwareMaxColorLimit: Map<String, Int>
)
