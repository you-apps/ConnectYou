package com.bnyro.contacts.ui.components.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ValueWithType
import com.bnyro.contacts.ui.components.EditorEntry
import com.bnyro.contacts.ui.components.base.LabeledTextField
import com.bnyro.contacts.util.ContactsHelper

@Composable
fun ContactEditor(
    contact: ContactData? = null,
    onSave: (contact: ContactData) -> Unit
) {
    fun List<ValueWithType>?.fill(): List<ValueWithType> {
        return if (this.isNullOrEmpty()) {
            listOf(ValueWithType("", 0))
        } else {
            this
        }
    }

    fun List<MutableState<ValueWithType>>.clean(): List<ValueWithType> {
        return this.filter { it.value.value.isNotBlank() }.map { it.value }
    }

    val firstName = remember {
        mutableStateOf(contact?.displayName.orEmpty())
    }

    val surName = remember {
        mutableStateOf(contact?.displayName.orEmpty())
    }

    val phoneNumber = remember {
        contact?.phoneNumber.fill().map { mutableStateOf(it) }
    }

    val emails = remember {
        contact?.emails.fill().map { mutableStateOf(it) }
    }

    val addresses = remember {
        contact?.addresses.fill().map { mutableStateOf(it) }
    }

    val events = remember {
        contact?.events.fill().map { mutableStateOf(it) }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                LabeledTextField(
                    label = R.string.first_name,
                    state = firstName
                )
            }

            item {
                LabeledTextField(
                    label = R.string.surname,
                    state = surName
                )
            }

            items(phoneNumber) {
                EditorEntry(R.string.phone, it, ContactsHelper.phoneNumberTypes)
            }

            items(emails) {
                EditorEntry(R.string.email, it, ContactsHelper.emailTypes)
            }

            items(addresses) {
                EditorEntry(R.string.address, it, ContactsHelper.addressTypes)
            }

            items(events) {
                EditorEntry(R.string.event, it, ContactsHelper.eventTypes)
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = {
                val newContact = ContactData(
                    firstName = firstName.value.trim(),
                    surName = surName.value.trim(),
                    displayName = "${firstName.value.trim()} ${surName.value.trim()}",
                    phoneNumber = phoneNumber.clean(),
                    emails = emails.clean(),
                    addresses = addresses.clean(),
                    events = events.clean()
                )
                onSave.invoke(newContact)
            }
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null
            )
        }
    }
}
