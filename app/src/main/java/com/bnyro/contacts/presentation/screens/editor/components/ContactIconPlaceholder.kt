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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.presentation.screens.settings.model.ThemeModel
import com.bnyro.contacts.util.ColorUtils
import com.bnyro.contacts.util.extension.contentColor

@Composable
fun ContactIconPlaceholder(
    themeModel: ThemeModel,
    firstChar: Char?
) {
    val backgroundColor = if (themeModel.colorfulIcons) {
        remember { Color(ColorUtils.getRandomColor()) }
    } else {
        MaterialTheme.colorScheme.primary
    }
    val contentColor = when {
        !themeModel.colorfulIcons -> MaterialTheme.colorScheme.onPrimary
        else -> backgroundColor.contentColor()
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                shape = CircleShape,
                color = backgroundColor
            )
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = firstChar?.toString() ?: "?",
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}