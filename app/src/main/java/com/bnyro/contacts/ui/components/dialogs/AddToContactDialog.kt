package com.bnyro.contacts.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ValueWithType
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.screens.EditorScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToContactDialog(
    newNumber: String
) {
    val context = LocalContext.current
    val contactsModel: ContactsModel = viewModel()

    var showDialog by remember {
        mutableStateOf(true)
    }

    var contactToEdit by remember {
        mutableStateOf<ContactData?>(null)
    }

    val scope = rememberCoroutineScope()

    if (showDialog) {
        var searchQuery by remember {
            mutableStateOf("")
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                DialogButton(text = stringResource(R.string.cancel)) {
                    showDialog = false
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
                            contactsModel.contacts.orEmpty()
                                .filter {
                                    it.displayName.orEmpty().lowercase().contains(
                                        searchQuery.lowercase()
                                    )
                                }
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        scope.launch {
                                            contactToEdit = contactsModel.loadAdvancedContactData(
                                                it
                                            ).apply {
                                                it.numbers = it.numbers.toMutableList().apply {
                                                    add(ValueWithType(newNumber, 0))
                                                }
                                            }
                                        }
                                        showDialog = false
                                    }
                                    .padding(vertical = 15.dp, horizontal = 20.dp),
                                text = it.displayName.orEmpty()
                            )
                        }
                    }
                }
            }
        )
    }

    contactToEdit?.let {
        EditorScreen(
            contact = it,
            onClose = { contactToEdit = null }
        ) { contact ->
            contactsModel.updateContact(context, contact)
            contactToEdit = null
        }
    }
}
