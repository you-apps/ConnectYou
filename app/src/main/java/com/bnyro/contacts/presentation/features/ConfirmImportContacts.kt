package com.bnyro.contacts.presentation.features

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bnyro.contacts.R
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel

@Composable
fun ConfirmImportContactsDialog(
    contactsModel: ContactsModel,
    contactsUri: Uri,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.import_vcf),
        text = stringResource(R.string.import_confirm)
    ) {
        contactsModel.importVcf(context, contactsUri)
    }
}