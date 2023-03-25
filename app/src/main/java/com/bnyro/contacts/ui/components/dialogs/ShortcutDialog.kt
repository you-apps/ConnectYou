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

    val actionTypes = listOf(
        IntentActionType.CONTACT to R.string.contact,
        IntentActionType.DIAL to R.string.dial,
        IntentActionType.SMS to R.string.message,
        IntentActionType.EMAIL to R.string.email
    )

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
                items(actionTypes) { actionType ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                ShortcutHelper.createContactShortcut(
                                    context,
                                    contact,
                                    actionType.first
                                )
                                onDismissRequest.invoke()
                            }
                            .padding(vertical = 15.dp, horizontal = 20.dp),
                        text = stringResource(actionType.second)
                    )
                }
            }
        }
    )
}
