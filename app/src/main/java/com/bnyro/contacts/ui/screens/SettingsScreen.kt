package com.bnyro.contacts.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.ThemeMode
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.components.prefs.AutoBackupPref
import com.bnyro.contacts.ui.components.prefs.BlockPreference
import com.bnyro.contacts.ui.components.prefs.EncryptBackupsPref
import com.bnyro.contacts.ui.components.prefs.SettingsCategory
import com.bnyro.contacts.ui.components.prefs.SwitchPref
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.ui.models.ThemeModel
import com.bnyro.contacts.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onDismissRequest: () -> Unit) {
    val themeModel: ThemeModel = viewModel()
    val smsModel: SmsModel = viewModel()

    FullScreenDialog(onClose = onDismissRequest) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState()
        )

        Scaffold(
            modifier = Modifier.nestedScroll((scrollBehavior.nestedScrollConnection)),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(stringResource(R.string.settings))
                    },
                    navigationIcon = {
                        ClickableIcon(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = R.string.okay
                        ) {
                            onDismissRequest.invoke()
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { pV ->
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .padding(pV)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                SettingsCategory(title = stringResource(R.string.appearance))
                Text(stringResource(R.string.theme))
                BlockPreference(
                    preferenceKey = Preferences.themeKey,
                    entries = listOf(R.string.system, R.string.light, R.string.dark, R.string.amoled).map {
                        stringResource(it)
                    }
                ) {
                    themeModel.themeMode = ThemeMode.fromInt(it)
                }
                SwitchPref(
                    prefKey = Preferences.colorfulContactIconsKey,
                    title = stringResource(R.string.colorful_contact_icons)
                ) {
                    themeModel.colorfulIcons = it
                }
                Divider(
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                SettingsCategory(title = stringResource(R.string.messages))
                SwitchPref(
                    prefKey = Preferences.storeSmsLocallyKey,
                    title = stringResource(R.string.private_sms_database),
                    summary = stringResource(R.string.private_sms_database_desc)
                ) {
                    smsModel.refreshLocalSmsPreference()
                }
                Divider(
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                SettingsCategory(title = stringResource(R.string.behavior))
                Text(stringResource(R.string.start_tab))
                BlockPreference(
                    preferenceKey = Preferences.homeTabKey,
                    entries = listOf(R.string.contacts, R.string.messages).map {
                        stringResource(it)
                    }
                )
                SwitchPref(
                    prefKey = Preferences.collapseBottomBarKey,
                    title = stringResource(R.string.collapse_bottom_bar)
                ) {
                    themeModel.collapsableBottomBar = it
                }
                Divider(
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                AutoBackupPref()
                EncryptBackupsPref()
            }
        }
    }
}
