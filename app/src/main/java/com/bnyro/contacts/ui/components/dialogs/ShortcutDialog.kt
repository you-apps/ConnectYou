package com.bnyro.contacts.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.IntentActionType
import com.bnyro.contacts.obj.ContactData
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

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                if (possibleData.size != 1) {
                                    showInfoSelectionDialog = true
                                    return@clickable
                                }

                                ShortcutHelper.createContactShortcut(
                                    context,
                                    contact,
                                    possibleData.first(),
                                    actionType.first
                                )
                                onDismissRequest.invoke()
                            }
                            .padding(vertical = 15.dp, horizontal = 20.dp),
                        text = stringResource(actionType.second)
                    )

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
                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(20.dp))
                                                .clickable {
                                                    ShortcutHelper.createContactShortcut(
                                                        context,
                                                        contact,
                                                        data,
                                                        actionType.first
                                                    )
                                                    onDismissRequest.invoke()
                                                }
                                                .padding(vertical = 15.dp, horizontal = 20.dp),
                                            text = data
                                        )
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
