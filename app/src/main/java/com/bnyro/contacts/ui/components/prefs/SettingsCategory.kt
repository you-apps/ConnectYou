package com.bnyro.contacts.ui.components.prefs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun SettingsCategory(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        modifier = modifier,
        text = title.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        fontSize = 14.sp
    )
}
