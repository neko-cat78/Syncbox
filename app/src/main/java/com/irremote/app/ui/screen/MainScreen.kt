package com.irremote.app

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.irremote.app.ir.IRMode
import com.irremote.app.ir.IRTransmitterFactory
import com.irremote.app.model.ColorButton
import com.irremote.app.model.RemoteConfig
import com.irremote.app.ui.screen.MobileRemoteScreen
import com.irremote.app.ui.screen.TvRemoteScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    remoteConfig: RemoteConfig?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(IRMode.JACK_3_5MM) }

    val isTv = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SettingsRemote, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("IR Remote")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IRMode.entries.forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        label = { Text(mode.displayName) }
                    )
                }
            }

            val buttons = remoteConfig?.supportedColors ?: emptyList()

            if (isTv) {
                TvRemoteScreen(
                    buttons = buttons,
                    onButtonClick = { button -> transmitCode(context, selectedMode, button) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                MobileRemoteScreen(
                    buttons = buttons,
                    onButtonClick = { button -> transmitCode(context, selectedMode, button) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (remoteConfig != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Protocol: ${remoteConfig.remoteMetadata.protocol}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            "Chipset: ${remoteConfig.remoteMetadata.commonChipset}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            "Buttons: ${remoteConfig.remoteMetadata.totalButtons}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

private fun transmitCode(context: android.content.Context, mode: IRMode, button: ColorButton) {
    val transmitter = IRTransmitterFactory.getTransmitter(mode, context)
    transmitter.transmit(button.irHexCode)
}
