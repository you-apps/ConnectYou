package com.bnyro.contacts.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.obj.TranslatedType
import com.bnyro.contacts.obj.ValueWithType

@Composable
fun ContactEntryGroup(
    label: String,
    entries: List<ValueWithType>,
    types: List<TranslatedType> = listOf(),
    onClick: (ValueWithType) -> Unit = {}
) {
    if (entries.isNotEmpty()) {
        Column {
            Text(
                modifier = Modifier
                    .padding(start = 20.dp, top = 10.dp)
                    .align(Alignment.Start),
                text = label,
                style = MaterialTheme.typography.titleSmall
            )
            entries.forEach { entry ->
                val type = types.firstOrNull { type -> entry.type == type.id }?.title
                ContactEntry(content = entry.value, type = type, onClick = {
                    onClick.invoke(entry)
                })
            }
        }
    }
}

@Composable
fun ContactEntryTextGroup(
    label: String,
    entries: List<String>,
    types: List<TranslatedType> = listOf(),
    onClick: (ValueWithType) -> Unit = {}
) {
    ContactEntryGroup(
        label,
        entries.map { ValueWithType(it, null) },
        types,
        onClick
    )
}
