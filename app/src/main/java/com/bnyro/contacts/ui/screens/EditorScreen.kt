package com.bnyro.contacts.ui.screens

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.components.dialogs.ContactEditor

@Composable
fun EditorScreen(
    contact: ContactData? = null,
    onClose: () -> Unit,
    onSave: (ContactData) -> Unit
) {
    val context = LocalContext.current

    FullScreenDialog(onClose = onClose) {
        ContactEditor(
            contact = contact,
            onSave = {
                if (it.displayName.orEmpty().isBlank()) {
                    Toast.makeText(context, R.string.empty_name, Toast.LENGTH_SHORT).show()
                    return@ContactEditor
                }
                onSave.invoke(it)
                onClose.invoke()
            }
        )
    }
}
