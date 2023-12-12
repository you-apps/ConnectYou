package com.bnyro.contacts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R

@Composable
fun DialerButton(
    isEnabled: Boolean,
    icon: ImageVector,
    hint: String,
    onClick: () -> Unit
) {
    val tonalColor = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)

    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (isEnabled) tonalColor else MaterialTheme.colorScheme.background,
            contentColor = if (isEnabled) MaterialTheme.colorScheme.contentColorFor(tonalColor) else MaterialTheme.colorScheme.onBackground
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MicOff,
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = stringResource(R.string.mute))
        }
    }
}