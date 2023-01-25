package com.bnyro.contacts.ui.components.base

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ClickableIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            tint = tint,
            contentDescription = null
        )
    }
}
