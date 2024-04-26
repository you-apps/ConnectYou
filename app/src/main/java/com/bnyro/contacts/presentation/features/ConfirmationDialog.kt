package com.bnyro.contacts.presentation.features

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(text = "Ok") {
                onConfirm.invoke()
                onDismissRequest.invoke()
            }
        },
        dismissButton = {
            DialogButton(text = "Cancel") {
                onDismissRequest.invoke()
            }
        },
        title = {
            Text(title)
        },
        text = {
            Text(text)
        }
    )
}
