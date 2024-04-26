package com.bnyro.contacts.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
        TooltipBox(
            tooltip = {
                PlainTooltip {
                    Text(stringResource(contentDescription))
                }
            },
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            state = rememberTooltipState(),
        ) {
            IconButton(
                modifier = modifier,
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
