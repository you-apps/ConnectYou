package com.bnyro.contacts.presentation.screens.settings.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.BackupType
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.PickFolderContract
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.util.workers.BackupWorker

@Composable
fun AutoBackupPref() {
    val context = LocalContext.current
    val directoryPicker = rememberLauncherForActivityResult(PickFolderContract()) {
        it ?: return@rememberLauncherForActivityResult
        Preferences.edit {
            putString(Preferences.backupDirKey, it.toString())
        }
        context.contentResolver.takePersistableUriPermission(
            it,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }
    var backupType by remember {
        mutableStateOf(Preferences.getBackupType())
    }
    SettingsCategory(title = stringResource(R.string.backup))
    Text(stringResource(R.string.auto_backup))
    BlockPreference(
        preferenceKey = Preferences.backupTypeKey,
        entries = listOf(R.string.none, R.string.device, R.string.local, R.string.both).map {
            stringResource(it)
        }
    ) {
        backupType = BackupType.fromInt(it)
    }

    EditTextPreference(
        preferenceKey = Preferences.backupNamingSchemeKey,
        title = R.string.backup_naming_scheme,
        supportingHint = stringResource(R.string.backup_naming_scheme_hint),
        defaultValue = BackupHelper.defaultBackupNamingScheme
    )

    val backupIntervals = listOf(1, 2, 4, 6, 12, 24, 48)
    ListPreference(
        preferenceKey = Preferences.backupIntervalKey,
        title = R.string.backup_interval,
        entries = backupIntervals.map { "${it}h" },
        values = backupIntervals.map { it.toString() },
        defaultValue = "12"
    ) {
        BackupWorker.enqueue(context, true)
    }
    val backupAmounts = listOf(1, 2, 3, 5, 10, 20)
    ListPreference(
        preferenceKey = Preferences.maxBackupAmountKey,
        title = R.string.max_backup_amount,
        entries = backupAmounts.map { it.toString() },
        values = backupAmounts.map { it.toString() },
        defaultValue = "5"
    )
    AnimatedVisibility(visible = backupType != BackupType.NONE) {
        Button(
            onClick = {
                directoryPicker.launch(null)
            }
        ) {
            Text(stringResource(R.string.choose_dir))
        }
    }
}
