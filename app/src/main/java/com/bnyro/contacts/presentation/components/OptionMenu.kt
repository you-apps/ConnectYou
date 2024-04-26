package com.bnyro.contacts.presentation.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun OptionMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    options: List<String>,
    onDismissRequest: () -> Unit,
    onSelect: (index: Int) -> Unit
) {
    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        options.forEachIndexed { index, title ->
            DropdownMenuItem(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    onSelect.invoke(index)
                },
                text = {
                    Text(title)
                }
            )
        }
    }
}
