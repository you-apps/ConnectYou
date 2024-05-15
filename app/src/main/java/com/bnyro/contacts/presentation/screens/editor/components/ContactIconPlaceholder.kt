package com.bnyro.contacts.presentation.screens.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.presentation.screens.settings.model.ThemeModel
import com.bnyro.contacts.util.ColorUtils

@Composable
fun ContactIconPlaceholder(
    themeModel: ThemeModel,
    firstChar: Char?
) {
    val container = MaterialTheme.colorScheme.secondaryContainer
    val onContainer = MaterialTheme.colorScheme.onSecondaryContainer
    val color = when (themeModel.colorfulIcons) {
        true -> remember { ColorUtils.getRandomMaterialColorPair(container, onContainer) }
        false -> container to onContainer
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                shape = CircleShape,
                color = color.first
            )
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = firstChar?.toString() ?: "?",
            color = color.second,
            fontWeight = FontWeight.Bold
        )
    }
}