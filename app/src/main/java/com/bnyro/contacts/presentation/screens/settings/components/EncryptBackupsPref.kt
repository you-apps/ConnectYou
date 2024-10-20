package com.bnyro.contacts.presentation.screens.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.bnyro.contacts.R
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.PasswordUtils
import com.bnyro.contacts.util.Preferences

@Composable
fun EncryptBackupsPref() {
    var encryptBackups by remember { mutableStateOf(BackupHelper.encryptBackups) }

    SwitchPref(
        prefKey = Preferences.encryptBackupsKey,
        title = stringResource(R.string.encrypt_backups)
    ) {
        encryptBackups = it
        if (it) {
            Preferences.edit {
                putString(Preferences.encryptBackupPasswordKey, PasswordUtils.randomString(12))
            }
        }
    }

    AnimatedVisibility(encryptBackups) {
        EditTextPreference(
            preferenceKey = Preferences.encryptBackupPasswordKey,
            title = R.string.backup_password,
            isPassword = true,
            defaultValue = "",
        )
    }
}
