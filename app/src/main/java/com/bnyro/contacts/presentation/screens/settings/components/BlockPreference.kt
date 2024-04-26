package com.bnyro.contacts.presentation.screens.settings.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.util.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockPreference(
    preferenceKey: String,
    entries: List<String>,
    onSelectionChange: (Int) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(scrollState)
    ) {
        var selectedItem by rememberPreference(key = preferenceKey, defaultValue = 0)

        entries.forEachIndexed { index, entry ->
            SegmentedButton(
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    onSelectionChange(index)
                },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = entries.size)
            ) {
                Text(entry)
            }
        }
    }
}
