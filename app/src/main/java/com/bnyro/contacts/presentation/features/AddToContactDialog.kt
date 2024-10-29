package com.bnyro.contacts.presentation.features

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.ValueWithType
import com.bnyro.contacts.presentation.components.ClickableText
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.editor.EditorScreen
import kotlinx.coroutines.launch

@Composable
fun AddToContactDialog(newNumber: String, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val contactsModel: ContactsModel = viewModel(factory = ContactsModel.Factory)

    var contactToEdit by remember {
        mutableStateOf<ContactData?>(null)
    }

    var isNewContact by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    if (contactToEdit == null) {
        var searchQuery by remember {
            mutableStateOf("")
        }

        AlertDialog(
            onDismissRequest = { onDismissRequest() },
            confirmButton = {
                DialogButton(text = stringResource(R.string.cancel)) {
                    onDismissRequest()
                }
            },
            dismissButton = {
                DialogButton(text = stringResource(R.string.new_contact)) {
                    isNewContact = true
                    contactToEdit = ContactData()
                }
            },
            title = {
                Text(text = stringResource(R.string.add_to_contact))
            },
            text = {
                Column {
                    OutlinedTextField(
                        modifier = Modifier.padding(bottom = 10.dp),
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = {
                            Text(stringResource(R.string.search))
                        }
                    )

                    LazyColumn(
                        modifier = Modifier.height(400.dp)
                    ) {
                        items(
                            contactsModel.contacts
                                .filter {
                                    it.displayName.orEmpty().lowercase().contains(
                                        searchQuery.lowercase()
                                    )
                                },
                            key = ContactData::contactId
                        ) {
                            ClickableText(text = it.displayName.orEmpty()) {
                                scope.launch {
                                    contactToEdit = contactsModel.loadAdvancedContactData(it)
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    contactToEdit?.let {
        it.numbers = it.numbers.toMutableList().apply {
            add(ValueWithType(newNumber, 0))
        }
        EditorScreen(
            contact = it,
            onClose = {
                contactToEdit = null
            }
        ) { contact ->
            if (isNewContact) {
                contactsModel.createContact(context, contact)
            } else {
                contactsModel.updateContact(context, contact)
            }
            contactToEdit = null
            onDismissRequest()
        }
    }
}
