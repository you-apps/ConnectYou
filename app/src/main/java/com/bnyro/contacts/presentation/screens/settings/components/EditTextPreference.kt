package com.bnyro.contacts.presentation.screens.settings.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.R
import com.bnyro.contacts.presentation.components.ClickableIcon
import com.bnyro.contacts.presentation.features.DialogButton
import com.bnyro.contacts.util.Preferences

@Composable
fun EditTextPreference(
    preferenceKey: String,
    @StringRes title: Int,
    defaultValue: String,
    isPassword: Boolean = false,
    supportingHint: String? = null,
    onChange: (value: String) -> Unit = {}
) {
    var showDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var preferenceValue by remember {
        mutableStateOf(Preferences.getString(preferenceKey, defaultValue) ?: defaultValue)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        if (!isPassword) Text(text = stringResource(title), fontSize = 16.sp)
        Button(
            onClick = { showDialog = true }, modifier = Modifier.padding(vertical = 0.dp)
        ) {
            if (!isPassword) Text(preferenceValue.ifEmpty { defaultValue }) else Text(stringResource(title))
        }
    }

    if (showDialog) {
        var passwordVisible by remember { mutableStateOf(false) }
        var inputValue by remember {
            mutableStateOf(preferenceValue)
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                DialogButton(text = stringResource(R.string.okay)) {
                    Preferences.edit { putString(preferenceKey, inputValue) }
                    preferenceValue = inputValue
                    showDialog = false
                }
            }, dismissButton = {
                DialogButton(text = stringResource(R.string.cancel)) {
                    showDialog = false
                }
            }, title = {
                Text(stringResource(title))
            }, text = {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    visualTransformation = if (passwordVisible || !isPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    supportingText = {
                        if (supportingHint != null) Text(supportingHint)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text),
                    trailingIcon = {
                        if (isPassword) {
                            val image = if (passwordVisible) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            }
                            ClickableIcon(icon = image,
                                onClick = { passwordVisible = !passwordVisible })
                        }
                    }
                )
            }
        )
    }
}