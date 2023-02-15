package com.bnyro.contacts.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bnyro.contacts.R
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.components.prefs.BlockPreference
import com.bnyro.contacts.ui.components.prefs.SettingsCategory
import com.bnyro.contacts.ui.components.prefs.SettingsContainer
import com.bnyro.contacts.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onDismissRequest: () -> Unit) {
    FullScreenDialog(onClose = onDismissRequest) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.settings))
                    },
                    navigationIcon = {
                        ClickableIcon(icon = Icons.Default.ArrowBack) {
                            onDismissRequest.invoke()
                        }
                    }
                )
            }
        ) { pV ->
            Column(
                modifier = Modifier
                    .padding(pV)
            ) {
                SettingsContainer {
                    SettingsCategory(title = stringResource(R.string.start_tab))
                    BlockPreference(
                        preferenceKey = Preferences.homeTabKey,
                        entries = listOf(R.string.device, R.string.local).map {
                            stringResource(it)
                        }
                    )
                }
            }
        }
    }
}
