package com.bnyro.contacts.ui.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ValueWithType
import com.bnyro.contacts.ui.components.base.LabeledTextField
import com.bnyro.contacts.ui.components.editor.DatePickerEditor
import com.bnyro.contacts.ui.components.editor.TextFieldEditor
import com.bnyro.contacts.util.ContactsHelper

@Composable
fun ContactEditor(
    contact: ContactData? = null,
    onSave: (contact: ContactData) -> Unit
) {
    fun List<ValueWithType>?.fillIfEmpty(): List<ValueWithType> {
        return if (this.isNullOrEmpty()) {
            listOf(ValueWithType("", 0))
        } else {
            this
        }
    }

    fun List<MutableState<ValueWithType>>.clean(): List<ValueWithType> {
        return this.filter { it.value.value.isNotBlank() }.map { it.value }
    }

    fun emptyMutable() = mutableStateOf(ValueWithType("", 0))

    val firstName = remember {
        mutableStateOf(contact?.firstName.orEmpty())
    }

    val surName = remember {
        mutableStateOf(contact?.surName.orEmpty())
    }

    var phoneNumber by remember {
        mutableStateOf(
            contact?.numbers.fillIfEmpty().map { mutableStateOf(it) }
        )
    }

    var emails by remember {
        mutableStateOf(
            contact?.emails.fillIfEmpty().map { mutableStateOf(it) }
        )
    }

    var addresses by remember {
        mutableStateOf(
            contact?.addresses.fillIfEmpty().map { mutableStateOf(it) }
        )
    }

    var events by remember {
        mutableStateOf(
            contact?.events.fillIfEmpty().map { mutableStateOf(it) }
        )
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
                TextFieldEditor(R.string.phone, it, ContactsHelper.phoneNumberTypes) {
                    phoneNumber = phoneNumber + emptyMutable()
                }
            }

            items(emails) {
                TextFieldEditor(R.string.email, it, ContactsHelper.emailTypes) {
                    emails = emails + emptyMutable()
                }
            }

            items(addresses) {
                TextFieldEditor(R.string.address, it, ContactsHelper.addressTypes) {
                    addresses = addresses + emptyMutable()
                }
            }

            items(events) {
                DatePickerEditor(R.string.event, it, ContactsHelper.eventTypes) {
                    events = events + emptyMutable()
                }
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
                    numbers = phoneNumber.clean(),
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
