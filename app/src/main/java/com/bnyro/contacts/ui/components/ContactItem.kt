package com.bnyro.contacts.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactItem(
    contact: ContactData,
    sortOrder: SortOrder,
    selected: Boolean,
    onSinglePress: () -> Boolean,
    onLongPress: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val viewModel: ContactsModel = viewModel()
    val context = LocalContext.current

    var showContactScreen by remember {
        mutableStateOf(false)
    }
    var showDelete by remember {
        mutableStateOf(false)
    }

    val dismissState = rememberDismissState(
        confirmValueChange = {
            when (it) {
                DismissValue.DismissedToEnd -> {
                    showDelete = true
                }
                else -> {}
            }
            false
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd),
        background = {},
        dismissContent = {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .fillMaxWidth()
                    .clip(shape)
                    .combinedClickable(
                        onClick = {
                            if (!onSinglePress()) showContactScreen = true
                        },
                        onLongClick = {
                            onLongPress.invoke()
                        }
                    ),
                shape = shape,
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = if (selected) 10.dp else 2.dp
                )
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
                        val thumbnail = contact.thumbnail ?: contact.photo
                        if (thumbnail == null) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = (contact.displayName?.firstOrNull() ?: "").toString(),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                bitmap = thumbnail.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        when {
                            sortOrder == SortOrder.FIRSTNAME -> "${contact.firstName ?: ""} ${contact.surName ?: ""}"
                            sortOrder == SortOrder.LASTNAME && !contact.surName.notAName() && !contact.firstName.notAName() -> "${contact.surName}, ${contact.firstName}"
                            else -> contact.displayName.orEmpty()
                        }.trim()
                    )
                }
            }
        }
    )

    if (showContactScreen) {
        val data = runBlocking {
            viewModel.loadAdvancedContactData(contact)
        }
        SingleContactScreen(data) {
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
            viewModel.deleteContacts(context, listOf(contact))
        }
    }
}
