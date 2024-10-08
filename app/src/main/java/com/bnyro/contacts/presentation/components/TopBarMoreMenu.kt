package com.bnyro.contacts.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.bnyro.contacts.R

@Composable
fun TopBarMoreMenu(
    options: List<String> = emptyList(),
    onOptionClick: (Int) -> Unit = {}
) {
    var expandedOptions by rememberSaveable {
        mutableStateOf(false)
    }

    ClickableIcon(
        icon = Icons.Default.MoreVert,
        contentDescription = R.string.more
    ) {
        expandedOptions = !expandedOptions
    }

    OptionMenu(
        expanded = expandedOptions,
        options = options,
        onDismissRequest = {
            expandedOptions = false
        },
        onSelect = {
            onOptionClick(it)
            expandedOptions = false
        }
    )
}