package com.bnyro.contacts.ui.components.prefs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bnyro.contacts.util.rememberPreference

@Composable
fun SwitchPref(
    prefKey: String,
    title: String,
    summary: String? = null,
    defaultValue: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    var checked by rememberPreference(key = prefKey, defaultValue = defaultValue)
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                checked = !checked
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(title)
                if (summary != null) {
                    Text(summary)
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange.invoke(it)
            }
        )
    }
}
