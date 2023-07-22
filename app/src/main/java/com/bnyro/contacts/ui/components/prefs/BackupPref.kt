package com.bnyro.contacts.ui.components.prefs

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.BackupType
import com.bnyro.contacts.util.PickFolderContract
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.workers.BackupWorker

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

    SettingsCategory(title = stringResource(R.string.auto_backup))
    BlockPreference(
        preferenceKey = Preferences.backupTypeKey,
        entries = listOf(R.string.none, R.string.device, R.string.local, R.string.both).map {
            stringResource(it)
        }
    ) {
        backupType = BackupType.fromInt(it)
    }
    Spacer(modifier = Modifier.height(10.dp))
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
            modifier = Modifier.padding(top = 5.dp),
            onClick = {
                directoryPicker.launch(null)
            }
        ) {
            Text(stringResource(R.string.choose_dir))
        }
    }
}
