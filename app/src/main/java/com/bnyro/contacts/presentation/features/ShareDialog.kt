package com.bnyro.contacts.presentation.features

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.ListAttribute
import com.bnyro.contacts.domain.enums.StringAttribute
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.presentation.components.ShareOption
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.ContactsHelper

@Composable
fun ShareDialog(
    contact: ContactData,
    onDismissRequest: () -> Unit
) {
    val contactsModel: ContactsModel = viewModel(factory = ContactsModel.Factory)
    val context = LocalContext.current

    val baseOptions = remember {
        listOf(R.string.name, R.string.photo)
    }

    val options = remember {
         baseOptions + ContactsHelper.contactAttributesTypes.map { it.stringRes }
    }

    val selected = remember {
        SnapshotStateList<Boolean>().apply {
            for (i in options.indices) add(true)
        }
    }

    fun getContactData(): ContactData {
        val data = ContactData()

        if (selected[0]) {
            data.displayName = contact.displayName
            data.alternativeName = contact.alternativeName
            data.firstName = contact.firstName
            data.surName = contact.surName
        }
        if (selected[1]) {
            data.photo = contact.photo
        }

        ContactsHelper.contactAttributesTypes.forEachIndexed { index, contactAttribute ->
            if (selected[index + baseOptions.size]) {
                if (contactAttribute is StringAttribute) {
                    val value = contactAttribute.get(contact)
                    contactAttribute.set(data, value)
                } else if (contactAttribute is ListAttribute) {
                    val value = contactAttribute.get(contact)
                    contactAttribute.set(data, value)
                }
            }
        }

        return data
    }

    val openFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BackupHelper.vCardMimeTypes.first())
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        val editedContact = getContactData()
        contactsModel.exportVcf(context, uri, listOf(editedContact))
        onDismissRequest.invoke()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.share)) },
        text = {
            LazyColumn {
                itemsIndexed(options) { index, it ->
                    ShareOption(title = it, isChecked = selected[index]) {
                        selected[index] = it
                    }
                }
            }
        },
        dismissButton = {
            DialogButton(stringResource(R.string.cancel)) {
                onDismissRequest.invoke()
            }
        },
        confirmButton = {
            Row {
                DialogButton(stringResource(R.string.save)) {
                    openFilePicker.launch("${contact.displayName}.vcf")
                }
                DialogButton(stringResource(R.string.share)) {
                    val editedContact = getContactData()
                    contactsModel.shareTempContacts(context, listOf(editedContact))
                    onDismissRequest.invoke()
                }
            }
        }
    )
}
