package com.irremote.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irremote.app.model.ColorButton
import com.irremote.app.ui.components.RemoteButton

@Composable
fun MobileRemoteScreen(
    buttons: List<ColorButton>,
    onButtonClick: (ColorButton) -> Unit,
    modifier: Modifier = Modifier
) {
    if (buttons.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No buttons configured")
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        items(buttons, key = { it.irHexCode }) { button ->
            RemoteButton(
                label = button.buttonName,
                colorValue = button.hexColorValue,
                onClick = { onButtonClick(button) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
