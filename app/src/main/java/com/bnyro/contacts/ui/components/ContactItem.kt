package com.bnyro.contacts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.SortOrder
import com.bnyro.contacts.ext.notAName
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.dialogs.ConfirmationDialog
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.screens.SingleContactScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactItem(contact: ContactData, sortOrder: SortOrder) {
    val shape = RoundedCornerShape(20.dp)
    val viewModel: ContactsModel = viewModel()
    val context = LocalContext.current

    var showContactScreen by remember {
        mutableStateOf(false)
    }
    var showDelete by remember {
        mutableStateOf(false)
    }

    val state = rememberDismissState(
        confirmValueChange = {
            showDelete = true
            false
        }
    )

    SwipeToDismiss(
        state = state,
        background = {},
        directions = setOf(DismissDirection.StartToEnd),
        dismissContent = {
            ElevatedCard(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    .fillMaxWidth()
                    .clip(shape)
                    .clickable {
                        showContactScreen = true
                    },
                shape = shape
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            )
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = (contact.displayName?.firstOrNull() ?: "").toString(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        when {
                            contact.firstName.notAName() && contact.surName.notAName() -> contact.displayName.orEmpty()
                            sortOrder == SortOrder.FIRSTNAME -> "${contact.firstName ?: ""} ${contact.surName ?: ""}"
                            sortOrder == SortOrder.SURNAME && contact.surName != null && contact.firstName != null -> "${contact.surName}, ${contact.firstName}"
                            else -> contact.displayName.orEmpty()
                        }.trim()
                    )
                }
            }
        }
    )

    if (showContactScreen) {
        SingleContactScreen(viewModel.loadAdvancedContactData(context, contact)) {
            showContactScreen = false
        }
    }

    if (showDelete) {
        ConfirmationDialog(
            onDismissRequest = {
                showDelete = false
            },
            title = stringResource(R.string.delete_contact),
            text = stringResource(R.string.irreversible)
        ) {
            viewModel.deleteContact(context, contact)
        }
    }
}
