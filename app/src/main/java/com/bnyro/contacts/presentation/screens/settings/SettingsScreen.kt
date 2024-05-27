package com.bnyro.contacts.presentation.screens.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.ThemeMode
import com.bnyro.contacts.presentation.components.ClickableIcon
import com.bnyro.contacts.presentation.screens.settings.components.AutoBackupPref
import com.bnyro.contacts.presentation.screens.settings.components.BlockPreference
import com.bnyro.contacts.presentation.screens.settings.components.EncryptBackupsPref
import com.bnyro.contacts.presentation.screens.settings.components.SettingsCategory
import com.bnyro.contacts.presentation.screens.settings.components.SwitchPref
import com.bnyro.contacts.presentation.screens.settings.components.SwitchPrefBase
import com.bnyro.contacts.presentation.screens.settings.model.ThemeModel
import com.bnyro.contacts.presentation.screens.sms.model.SmsModel
import com.bnyro.contacts.util.BiometricAuthUtil
import com.bnyro.contacts.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(themeModel: ThemeModel, smsModel: SmsModel, onBackPress: () -> Unit) {
    val context = LocalContext.current

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
                        onBackPress.invoke()
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
                entries = listOf(
                    R.string.system,
                    R.string.light,
                    R.string.dark,
                    R.string.amoled
                ).map {
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
            HorizontalDivider(
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
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingsCategory(title = stringResource(R.string.behavior))
            Text(stringResource(R.string.start_tab))
            BlockPreference(
                preferenceKey = Preferences.homeTabKey,
                entries = listOf(R.string.dial, R.string.contacts, R.string.messages).map {
                    stringResource(it)
                }
            )
            SwitchPref(
                prefKey = Preferences.collapseBottomBarKey,
                title = stringResource(R.string.collapse_bottom_bar)
            ) {
                themeModel.collapsableBottomBar = it
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                SettingsCategory(title = stringResource(R.string.security))

                var biometricAuthEnabled by remember {
                    mutableStateOf(Preferences.getBoolean(Preferences.biometricAuthKey, false))
                }

                SwitchPrefBase(
                    title = stringResource(R.string.biometric_authentication),
                    summary = stringResource(R.string.biometric_authentication_summary),
                    checked = biometricAuthEnabled
                ) { newValue ->
                    BiometricAuthUtil.requestAuth(context) { authSuccess ->
                        if (authSuccess) {
                            Preferences.edit { putBoolean(Preferences.biometricAuthKey, newValue) }
                            biometricAuthEnabled = newValue
                        }
                    }
                }
            }
            AutoBackupPref()
            EncryptBackupsPref()
        }
    }
}
