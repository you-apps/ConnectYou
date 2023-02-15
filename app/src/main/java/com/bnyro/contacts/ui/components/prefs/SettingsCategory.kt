package com.bnyro.contacts.ui.components.prefs

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsCategory(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        modifier = modifier.padding(10.dp),
        text = title.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp
    )
}
