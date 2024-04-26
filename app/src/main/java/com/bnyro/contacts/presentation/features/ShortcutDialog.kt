package com.bnyro.contacts.presentation.features

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.IntentActionType
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.presentation.components.ClickableText
import com.bnyro.contacts.util.ShortcutHelper

@Composable
fun ShortcutDialog(
    contact: ContactData,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(text = stringResource(R.string.cancel)) {
                onDismissRequest.invoke()
            }
        },
        title = {
            Text(text = stringResource(R.string.create_shortcut))
        },
        text = {
            LazyColumn {
                items(ShortcutHelper.actionTypes) { actionType ->
                    var showInfoSelectionDialog by remember {
                        mutableStateOf(false)
                    }
                    val possibleData = when (actionType.first) {
                        IntentActionType.DIAL, IntentActionType.SMS -> contact.numbers.map { it.value }
                        IntentActionType.EMAIL -> contact.emails.map { it.value }
                        IntentActionType.CONTACT -> listOf(contact.contactId.toString())
                        else -> contact.addresses.map { it.value }
                    }

                    ClickableText(text = stringResource(actionType.second)) {
                        if (possibleData.size != 1) {
                            showInfoSelectionDialog = true
                            return@ClickableText
                        }

                        ShortcutHelper.createContactShortcut(
                            context,
                            contact,
                            possibleData.first(),
                            actionType.first
                        )
                        onDismissRequest.invoke()
                    }

                    if (showInfoSelectionDialog) {
                        AlertDialog(
                            onDismissRequest = { showInfoSelectionDialog = false },
                            confirmButton = {
                                DialogButton(text = stringResource(R.string.cancel)) {
                                    showInfoSelectionDialog = false
                                }
                            },
                            text = {
                                LazyColumn {
                                    items(possibleData) { data ->
                                        ClickableText(text = data) {
                                            ShortcutHelper.createContactShortcut(
                                                context,
                                                contact,
                                                data,
                                                actionType.first
                                            )
                                            onDismissRequest.invoke()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}
