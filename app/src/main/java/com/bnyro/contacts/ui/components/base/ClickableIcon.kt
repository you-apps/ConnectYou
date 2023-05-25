package com.bnyro.contacts.ui.components.base

import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    @StringRes contentDescription: Int? = null,
    tint: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    if (contentDescription != null) {
        PlainTooltipBox(tooltip = { Text(stringResource(contentDescription)) }) {
            IconButton(
                modifier = modifier.tooltipTrigger(),
                onClick = onClick
            ) {
                Icon(
                    imageVector = icon,
                    tint = tint,
                    contentDescription = stringResource(contentDescription)
                )
            }
        }
    } else {
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
}
