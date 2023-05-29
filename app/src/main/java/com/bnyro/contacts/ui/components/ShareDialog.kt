package com.bnyro.contacts.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.dialogs.DialogButton
import com.bnyro.contacts.ui.models.ContactsModel

@Composable
fun ShareDialog(
    contact: ContactData,
    onDismissRequest: () -> Unit
) {
    val contactsModel: ContactsModel = viewModel()
    val context = LocalContext.current

    val shareName = remember { mutableStateOf(true) }
    val sharePhoto = remember { mutableStateOf(true) }
    val shareNickName = remember { mutableStateOf(true) }
    val shareOrganization = remember { mutableStateOf(true) }
    val shareWebsite = remember { mutableStateOf(true) }
    val sharePhone = remember { mutableStateOf(true) }
    val shareEmail = remember { mutableStateOf(true) }
    val shareAddress = remember { mutableStateOf(true) }
    val shareNote = remember { mutableStateOf(true) }

    val options = listOf(
        R.string.name to shareName,
        R.string.photo to sharePhoto,
        R.string.nick_name to shareNickName,
        R.string.organization to shareOrganization,
        R.string.website to shareWebsite,
        R.string.phone to sharePhone,
        R.string.email to shareEmail,
        R.string.address to shareAddress,
        R.string.note to shareNote
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.share)) },
        text = {
            LazyColumn {
                items(options) {
                    ShareOption(title = it.first, isChecked = it.second)
                }
            }
        },
        dismissButton = {
            DialogButton(stringResource(R.string.cancel)) {
                onDismissRequest.invoke()
            }
        },
        confirmButton = {
            DialogButton(stringResource(R.string.share)) {
                val editedContact = contact.copy().apply {
                    if (!shareName.value) {
                        displayName = null
                        alternativeName = null
                        firstName = null
                        surName = null
                    }
                    if (!sharePhoto.value) photo = null
                    if (!shareNickName.value) nickName = null
                    if (!shareOrganization.value) organization = null
                    if (!shareAddress.value) addresses = emptyList()
                    if (!shareEmail.value) emails = emptyList()
                    if (!shareNote.value) notes = emptyList()
                    if (!sharePhone.value) numbers = emptyList()
                    if (!shareWebsite.value) websites = emptyList()
                }
                contactsModel.exportSingleVcf(context, editedContact)
            }
        }
    )
}
