package com.bnyro.contacts.presentation.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.util.rememberPreference

@Composable
fun SwitchPrefBase(
    title: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onCheckedChange(!checked)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(fontSize = 16.sp, text = title)
            if (summary != null) {
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = summary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Switch(
            checked = checked,
            onCheckedChange = {
                onCheckedChange.invoke(it)
            }
        )
    }
}

@Composable
fun SwitchPref(
    prefKey: String,
    title: String,
    summary: String? = null,
    defaultValue: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    var checked by rememberPreference(key = prefKey, defaultValue = defaultValue)

    SwitchPrefBase(title = title, summary = summary, checked = checked) {
        checked = it
        onCheckedChange.invoke(it)
    }
}
