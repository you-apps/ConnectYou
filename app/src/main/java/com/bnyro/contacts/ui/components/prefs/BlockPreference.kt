package com.bnyro.contacts.ui.components.prefs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bnyro.contacts.util.rememberPreference

@Composable
fun BlockPreference(
    preferenceKey: String,
    entries: List<String>,
    onSelectionChange: (Int) -> Unit = {}
) {
    val state = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(state)
    ) {
        val cornerRadius = 20.dp

        var selectedItem by rememberPreference(key = preferenceKey, defaultValue = 0)

        entries.forEachIndexed { index, entry ->
            val startRadius = if (index != 0) 0.dp else cornerRadius
            val endRadius = if (index == entries.size - 1) cornerRadius else 0.dp

            OutlinedButton(
                onClick = {
                    selectedItem = index
                    onSelectionChange.invoke(index)
                },
                modifier = Modifier
                    .offset(if (index == 0) 0.dp else (-1 * index).dp, 0.dp)
                    .zIndex(if (selectedItem == index) 1f else 0f),
                shape = RoundedCornerShape(
                    topStart = startRadius,
                    topEnd = endRadius,
                    bottomStart = startRadius,
                    bottomEnd = endRadius
                ),
                border = BorderStroke(
                    1.dp,
                    if (selectedItem == index) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                    }
                ),
                colors = if (selectedItem == index) {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                } else {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            ) {
                Text(entry)
            }
        }
    }
}
