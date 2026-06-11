package com.irremote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.irremote.app.data.JsonLoader
import com.irremote.app.model.RemoteConfig
import com.irremote.app.ui.screen.MainScreen
import com.irremote.app.ui.theme.IRRemoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IRRemoteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    IRRemoteApp()
                }
            }
        }
    }
}

@Composable
fun IRRemoteApp() {
    var remoteConfig by remember { mutableStateOf<RemoteConfig?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        remoteConfig = JsonLoader.loadRemoteConfig(context)
    }

    MainScreen(remoteConfig = remoteConfig)
}
