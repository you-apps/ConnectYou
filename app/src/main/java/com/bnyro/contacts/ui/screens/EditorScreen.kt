package com.bnyro.contacts.ui.screens

import androidx.compose.runtime.Composable
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.components.dialogs.ContactEditor

@Composable
fun EditorScreen(onClose: () -> Unit, onSave: (ContactData) -> Unit) {
    FullScreenDialog(onClose = onClose) {
        ContactEditor(onSave = {
            onSave.invoke(it)
            onClose.invoke()
        })
    }
}
