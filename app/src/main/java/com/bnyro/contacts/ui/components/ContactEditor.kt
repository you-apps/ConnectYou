package com.bnyro.contacts.ui.components.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.base.LabeledTextField

@Composable
fun ContactEditor(
    contact: ContactData? = null,
    onSave: (contact: ContactData) -> Unit
) {
    val firstName = remember {
        mutableStateOf(contact?.displayName.orEmpty())
    }

    val surName = remember {
        mutableStateOf(contact?.displayName.orEmpty())
    }

    val phoneNumber = remember {
        mutableStateOf(contact?.phoneNumber?.firstOrNull().orEmpty())
    }

    val email = remember {
        mutableStateOf(contact?.emails?.firstOrNull().orEmpty())
    }

    val address = remember {
        mutableStateOf(contact?.addresses?.firstOrNull().orEmpty())
    }

    val event = remember {
        mutableStateOf(contact?.events?.firstOrNull().orEmpty())
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
                LabeledTextField(
                    label = R.string.surname,
                    state = surName
                )
                LabeledTextField(
                    label = R.string.phone,
                    state = phoneNumber
                )
                LabeledTextField(
                    label = R.string.email,
                    state = email
                )
                LabeledTextField(
                    label = R.string.address,
                    state = address
                )
                LabeledTextField(
                    label = R.string.event,
                    state = event
                )
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
                    phoneNumber = listOf(phoneNumber.value.trim()),
                    emails = listOf(email.value.trim()),
                    addresses = listOf(address.value.trim()),
                    events = listOf(event.value.trim())
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
