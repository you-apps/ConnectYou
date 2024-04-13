package com.bnyro.contacts.ui.components

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bnyro.contacts.R
import com.bnyro.contacts.ui.components.dialogs.ConfirmationDialog
import com.bnyro.contacts.ui.models.ContactsModel

@Composable
fun ConfirmImportContactsDialog(contactsModel: ContactsModel, contactsUri: Uri) {
    val context = LocalContext.current

    var showDialog by remember {
        mutableStateOf(true)
    }

    if (showDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDialog = false },
            title = stringResource(R.string.import_vcf),
            text = stringResource(R.string.import_confirm)
        ) {
            contactsModel.importVcf(context, contactsUri)
        }
    }
}