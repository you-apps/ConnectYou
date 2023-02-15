package com.bnyro.contacts.ui.components.prefs

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.ui.components.BlockButton
import com.bnyro.contacts.util.Preferences

@Composable
fun BlockPreference(
    preferenceKey: String,
    entries: List<String>,
    onSelectionChange: (Int) -> Unit = {}
) {
    var selected by remember {
        mutableStateOf(Preferences.getInt(preferenceKey, 0))
    }

    LazyRow(
        modifier = Modifier.padding(horizontal = 5.dp)
    ) {
        itemsIndexed(entries) { index, it ->
            BlockButton(
                modifier = Modifier.padding(2.dp, 0.dp),
                text = it,
                selected = index != selected
            ) {
                Preferences.edit { putInt(preferenceKey, index) }
                selected = index
                onSelectionChange.invoke(selected)
            }
        }
    }
}
