package com.bnyro.contacts.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.bnyro.contacts.R
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.OptionMenu
import com.bnyro.contacts.ui.screens.AboutScreen
import com.bnyro.contacts.ui.screens.SettingsScreen

@Composable
fun TopBarMoreMenu(
    extraOptions: List<String> = emptyList(),
    onExtraOptionClick: (Int) -> Unit = {}
) {
    var showSettings by remember {
        mutableStateOf(false)
    }

    var showAbout by remember {
        mutableStateOf(false)
    }

    var expandedOptions by remember {
        mutableStateOf(false)
    }

    ClickableIcon(
        icon = Icons.Default.MoreVert,
        contentDescription = R.string.more
    ) {
        expandedOptions = !expandedOptions
    }

    val options = extraOptions + listOf(
        stringResource(R.string.settings),
        stringResource(R.string.about)
    )
    OptionMenu(
        expanded = expandedOptions,
        options = options,
        onDismissRequest = {
            expandedOptions = false
        },
        onSelect = {
            when (it) {
                options.size - 2 -> showSettings = true
                options.size - 1 -> showAbout = true
                else -> onExtraOptionClick(it)
            }
            expandedOptions = false
        }
    )

    if (showSettings) {
        SettingsScreen {
            showSettings = false
        }
    }

    if (showAbout) {
        AboutScreen {
            showAbout = false
        }
    }
}