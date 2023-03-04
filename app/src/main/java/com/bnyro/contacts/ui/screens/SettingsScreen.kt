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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.ThemeMode
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.components.prefs.BackupPref
import com.bnyro.contacts.ui.components.prefs.BlockPreference
import com.bnyro.contacts.ui.components.prefs.CheckboxPref
import com.bnyro.contacts.ui.components.prefs.SettingsCategory
import com.bnyro.contacts.ui.components.prefs.SettingsContainer
import com.bnyro.contacts.ui.models.ThemeModel
import com.bnyro.contacts.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onDismissRequest: () -> Unit) {
    val themeModel: ThemeModel = viewModel()

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
                    SettingsCategory(title = stringResource(R.string.theme))
                    BlockPreference(
                        preferenceKey = Preferences.themeKey,
                        entries = listOf(R.string.system, R.string.light, R.string.dark).map {
                            stringResource(it)
                        }
                    ) {
                        themeModel.themeMode = ThemeMode.fromInt(it)
                    }
                }
                SettingsContainer {
                    SettingsCategory(title = stringResource(R.string.start_tab))
                    BlockPreference(
                        preferenceKey = Preferences.homeTabKey,
                        entries = listOf(R.string.device, R.string.local).map {
                            stringResource(it)
                        }
                    )
                }
                SettingsContainer {
                    BackupPref()
                }
                SettingsContainer {
                    SettingsCategory(title = stringResource(R.string.misc))
                    CheckboxPref(
                        prefKey = Preferences.collapseBottomBarKey,
                        title = stringResource(R.string.collapse_bottom_bar)
                    )
                    CheckboxPref(
                        prefKey = Preferences.colorfulContactIconsKey,
                        title = stringResource(R.string.colorful_contact_icons)
                    )
                }
            }
        }
    }
}
