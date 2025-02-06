package com.bnyro.contacts.presentation.screens.contacts.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.TranslatedType
import com.bnyro.contacts.domain.model.ValueWithType
import com.bnyro.contacts.presentation.features.DialogButton

const val CUSTOM_TYPE_ID = 0

@Composable
fun TypeSelectorDialog(
    currentValueAndType: ValueWithType,
    types: List<TranslatedType>,
    onTypeChange: (type: TranslatedType, label: String?) -> Unit,
    onDismissRequest: () -> Unit
) {
    var type by remember {
        mutableStateOf(types.find { it.id == currentValueAndType.type } ?: types.first())
    }
    var label by remember {
        mutableStateOf(currentValueAndType.label)
    }

    AlertDialog(
        dismissButton = {
            DialogButton(stringResource(R.string.cancel)) {
                onDismissRequest()
            }
        },
        confirmButton = {
            DialogButton(stringResource(R.string.okay)) {
                onTypeChange(type, label.takeIf { type.id == CUSTOM_TYPE_ID })
                onDismissRequest()
            }
        },
        onDismissRequest = onDismissRequest,
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(types) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == it,
                            onClick = { type = it }
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(it.title))
                    }
                }

                item {
                    if (type.id == CUSTOM_TYPE_ID) {
                        TextField(
                            value = label.orEmpty(),
                            onValueChange = { label = it },
                            label = {
                                Text(stringResource(R.string.type))
                            }
                        )
                    }
                }
            }
        }
    )
}