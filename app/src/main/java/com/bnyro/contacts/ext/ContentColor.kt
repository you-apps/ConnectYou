package com.bnyro.contacts.ext

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun Color.contentColor(): Color {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5
    return when {
        luminance().let { if (!isDarkTheme) it else 1 - it } > 0.5 -> {
            MaterialTheme.typography.bodyLarge.color
        }
        else -> MaterialTheme.colorScheme.background
    }
}
