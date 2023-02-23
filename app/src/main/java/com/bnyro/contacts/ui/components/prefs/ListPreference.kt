package com.bnyro.contacts.ui.components.prefs

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.ui.components.dialogs.DialogButton
import com.bnyro.contacts.util.Preferences

@Composable
fun ListPreference(
    preferenceKey: String,
    @StringRes title: Int,
    entries: List<String>,
    values: List<String>,
    defaultValue: String,
    onChange: (value: String) -> Unit = {}
) {
    var showDialog by remember {
        mutableStateOf(false)
    }
    var currentValue by remember {
        mutableStateOf(Preferences.getString(preferenceKey, defaultValue))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                showDialog = true
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleMedium
        )
        values.indexOfFirst { it == currentValue }.takeIf { it >= 0 }?.let {
            Text(text = entries[it])
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                DialogButton(text = stringResource(R.string.cancel)) {
                    showDialog = false
                }
            },
            text = {
                LazyColumn {
                    itemsIndexed(entries) { index, entry ->
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    val newValue = values[index]
                                    Preferences.edit { putString(preferenceKey, newValue) }
                                    currentValue = newValue
                                    onChange(newValue)
                                    showDialog = false
                                }
                                .padding(horizontal = 20.dp, vertical = 13.dp),
                            text = entry
                        )
                    }
                }
            }
        )
    }
}
