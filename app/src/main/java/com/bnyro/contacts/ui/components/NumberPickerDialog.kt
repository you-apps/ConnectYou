package com.bnyro.contacts.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.SortOrder
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.dialogs.DialogButton
import com.bnyro.contacts.ui.models.ContactsModel

@Composable
fun NumberPickerDialog(
    onDismissRequest: () -> Unit,
    onNumberSelect: (number: String) -> Unit
) {
    val contactsModel: ContactsModel = viewModel()

    var numbersToSelectFrom by remember {
        mutableStateOf(listOf<String>())
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(text = stringResource(R.string.cancel)) {
                onDismissRequest.invoke()
            }
        },
        title = {
            Text(stringResource(R.string.pick_contact))
        },
        text = {
            LazyColumn {
                item {
                    var numberInput by remember {
                        mutableStateOf("")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = numberInput,
                            onValueChange = { numberInput = it },
                            label = {
                                Text(stringResource(R.string.phone_number))
                            }
                        )
                        ClickableIcon(
                            modifier = Modifier.padding(top = 3.dp),
                            icon = Icons.Default.Send
                        ) {
                            onNumberSelect.invoke(numberInput)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                items(contactsModel.contacts.filter { it.numbers.isNotEmpty() }) {
                    ContactItem(
                        contact = it,
                        sortOrder = SortOrder.FIRSTNAME,
                        selected = false,
                        onSinglePress = {
                            if (it.numbers.size > 1) {
                                numbersToSelectFrom = it.numbers.map { num -> num.value }
                                return@ContactItem true
                            }

                            onNumberSelect.invoke(it.numbers.first().value)
                            onDismissRequest.invoke()
                            true
                        },
                        onLongPress = {}
                    )
                }
            }
        }
    )

    if (numbersToSelectFrom.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { numbersToSelectFrom = emptyList() },
            text = {
                LazyColumn {
                    items(numbersToSelectFrom) {
                        ClickableText(text = it) {
                            onNumberSelect.invoke(it)
                            numbersToSelectFrom = emptyList()
                            onDismissRequest.invoke()
                        }
                    }
                }
            },
            confirmButton = {
                DialogButton(stringResource(R.string.cancel)) {
                    numbersToSelectFrom = emptyList()
                }
            }
        )
    }
}