package com.bnyro.contacts.presentation.screens.editor

import android.widget.Toast
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.presentation.components.FullScreenDialog
import com.bnyro.contacts.presentation.screens.editor.components.ContactEditor
import com.bnyro.contacts.util.ContactsHelper

@Composable
fun EditorScreen(
    contact: ContactData? = null,
    onClose: () -> Unit,
    isDeviceContact: Boolean = false,
    onSave: (ContactData) -> Unit
) {
    val context = LocalContext.current

    FullScreenDialog(onClose = onClose) {
        ContactEditor(
            modifier = Modifier
                .imePadding(),
            contact = contact?.copy(),
            isDeviceContact = isDeviceContact,
            onSave = {
                if (ContactsHelper.isContactEmpty(it)) {
                    Toast.makeText(context, R.string.empty_name, Toast.LENGTH_SHORT).show()
                    return@ContactEditor
                }
                onSave.invoke(it)
                onClose.invoke()
            }
        )
    }
}
