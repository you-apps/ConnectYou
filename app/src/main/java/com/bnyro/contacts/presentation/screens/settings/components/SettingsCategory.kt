package com.bnyro.contacts.presentation.screens.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsCategory(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp),
        text = title.uppercase(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary
    )
}
