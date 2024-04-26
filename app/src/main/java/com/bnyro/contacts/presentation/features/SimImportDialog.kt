package com.bnyro.contacts.presentation.features

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.util.SimContactsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SimImportDialog(
    onDismissRequest: () -> Unit
) {
    val simContacts = remember { mutableStateListOf<ContactData>() }
    val selectedContacts = remember { mutableStateListOf<ContactData>() }
    var isLoading by remember {
        mutableStateOf(true)
    }
    val context = LocalContext.current
    val contactsModel: ContactsModel = viewModel(factory = ContactsModel.Factory)

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val contacts = SimContactsHelper.getSimContacts(context)
                simContacts.addAll(contacts)
                selectedContacts.addAll(contacts)
            } catch (e: Exception) {
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
            }
            isLoading = false
        }
    }

    AlertDialog(
        title = { Text(stringResource(R.string.import_sim)) },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            DialogButton(stringResource(R.string.cancel)) {
                onDismissRequest.invoke()
            }
        },
        confirmButton = {
            DialogButton(text = stringResource(R.string.okay)) {
                selectedContacts.forEach { contact ->
                    contactsModel.createContact(context, contact)
                }
                onDismissRequest.invoke()
            }
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(simContacts) { contact ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedContacts.contains(contact),
                                onCheckedChange = {
                                    if (it) {
                                        selectedContacts.add(contact)
                                    } else {
                                        selectedContacts.remove(contact)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = contact.displayName.orEmpty())
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(text = contact.numbers.firstOrNull()?.value.orEmpty())
                            }
                        }
                    }
                }
            }
        }
    )
}
