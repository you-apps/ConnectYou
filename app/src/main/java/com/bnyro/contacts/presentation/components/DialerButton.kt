package com.bnyro.contacts.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DialerButton(
    isEnabled: Boolean,
    icon: ImageVector,
    hint: String,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconToggleButton(
            checked = isEnabled,
            onCheckedChange = { onClick.invoke() },
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(imageVector = icon, contentDescription = hint)
        }
        Text(text = hint)
    }
}

@Preview
@Composable
private fun DialerButtonPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        DialerButton(isEnabled = true, icon = Icons.Rounded.MicOff, hint = "Mute") {
        }
        DialerButton(isEnabled = false, icon = Icons.Rounded.Mic, hint = "Mute") {
        }
    }
}
