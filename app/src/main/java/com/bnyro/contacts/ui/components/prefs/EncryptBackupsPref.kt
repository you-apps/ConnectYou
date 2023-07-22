package com.bnyro.contacts.ui.components.prefs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.dialogs.DialogButton
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.PasswordUtils
import com.bnyro.contacts.util.Preferences

@Composable
fun EncryptBackupsPref() {
    var encryptBackups by remember { mutableStateOf(BackupHelper.encryptBackups) }
    var showBackupPasswordDialog by remember { mutableStateOf(false) }

    SettingsCategory(title = stringResource(R.string.backup))
    CheckboxPref(
        prefKey = Preferences.encryptBackupsKey,
        title = stringResource(R.string.encrypt_backups)
    ) {
        encryptBackups = it
        if (it) Preferences.edit {
            putString(Preferences.encryptBackupPasswordKey, PasswordUtils.randomString(12))
        }
    }
    AnimatedVisibility(encryptBackups) {
        Text(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable {
                    showBackupPasswordDialog = true
                },
            text = stringResource(R.string.backup_password)
        )
    }

    if (showBackupPasswordDialog) {
        var password by remember {
            mutableStateOf(
                Preferences.getString(Preferences.encryptBackupPasswordKey, "").orEmpty()
            )
        }
        var passwordVisible by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showBackupPasswordDialog = false },
            confirmButton = {
                DialogButton(text = stringResource(R.string.okay)) {
                    Preferences.edit { putString(Preferences.encryptBackupPasswordKey, password) }
                    showBackupPasswordDialog = false
                }
            },
            dismissButton = {
                DialogButton(text = stringResource(R.string.cancel)) {
                    showBackupPasswordDialog = false
                }
            },
            title = {
                Text(stringResource(R.string.backup_password))
            },
            text = {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        ClickableIcon(
                            icon = image,
                            onClick = { passwordVisible = !passwordVisible }
                        )
                    }
                )
            }
        )
    }
}
