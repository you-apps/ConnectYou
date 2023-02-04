package com.bnyro.contacts.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.IntentActionType
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.ContactEntry
import com.bnyro.contacts.ui.components.ContactProfilePicture
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.components.dialogs.ConfirmationDialog
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.util.CalendarUtils
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.ExportHelper
import com.bnyro.contacts.util.IntentHelper

@Composable
fun SingleContactScreen(contact: ContactData, onClose: () -> Unit) {
    val viewModel: ContactsModel = viewModel()
    val context = LocalContext.current
    var showDelete by remember {
        mutableStateOf(false)
    }
    var showEditor by remember {
        mutableStateOf(false)
    }
    var showZoomablePhoto by remember {
        mutableStateOf(false)
    }

    FullScreenDialog(onClose = onClose) {
        val scrollState = rememberScrollState()
        val shape = RoundedCornerShape(20.dp)

        Column(
            modifier = Modifier.verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                modifier = Modifier
                    .padding(start = 15.dp, end = 15.dp, bottom = 5.dp, top = 20.dp)
                    .fillMaxWidth()
                    .clip(shape),
                shape = shape
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 35.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(135.dp)
                            .background(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            )
                    ) {
                        if (contact.photo == null) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = (contact.displayName?.firstOrNull() ?: "").toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 65.sp
                            )
                        } else {
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .clickable {
                                        showZoomablePhoto = true
                                    },
                                bitmap = contact.photo!!.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = contact.displayName.orEmpty(),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ElevatedCard(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp)
                ) {
                    ClickableIcon(icon = Icons.Default.Call) {
                        IntentHelper.launchAction(
                            context,
                            IntentActionType.DIAL,
                            contact.numbers.firstOrNull()?.value ?: return@ClickableIcon
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    ClickableIcon(icon = Icons.Default.Send) {
                        IntentHelper.launchAction(
                            context,
                            IntentActionType.SMS,
                            contact.numbers.firstOrNull()?.value ?: return@ClickableIcon
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    ClickableIcon(icon = Icons.Default.Edit) {
                        showEditor = true
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    ClickableIcon(icon = Icons.Default.Share) {
                        val tempFileUri = ExportHelper(context).exportContact(contact)
                        IntentHelper.shareContactVcf(context, tempFileUri)
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    ClickableIcon(icon = Icons.Default.Delete) {
                        showDelete = true
                    }
                }
            }

            contact.numbers.forEach {
                ContactEntry(
                    label = stringResource(R.string.phone),
                    content = it.value,
                    type = ContactsHelper.phoneNumberTypes.firstOrNull { type -> it.type == type.id }?.title
                ) {
                    IntentHelper.launchAction(context, IntentActionType.DIAL, it.value)
                }
            }

            contact.emails.forEach {
                ContactEntry(
                    label = stringResource(R.string.email),
                    content = it.value,
                    type = ContactsHelper.emailTypes.firstOrNull { type -> it.type == type.id }?.title
                ) {
                    IntentHelper.launchAction(context, IntentActionType.EMAIL, it.value)
                }
            }

            contact.addresses.forEach {
                ContactEntry(
                    label = stringResource(R.string.address),
                    content = it.value,
                    type = ContactsHelper.addressTypes.firstOrNull { type -> it.type == type.id }?.title
                ) {
                    IntentHelper.launchAction(context, IntentActionType.ADDRESS, it.value)
                }
            }

            contact.events.forEach {
                ContactEntry(
                    label = stringResource(R.string.event),
                    content = CalendarUtils.localizeIsoDate(it.value),
                    type = ContactsHelper.eventTypes.firstOrNull { type -> it.type == type.id }?.title
                )
            }
        }
    }

    if (showEditor) {
        EditorScreen(
            contact = contact,
            onClose = {
                showEditor = false
            },
            onSave = {
                viewModel.updateContact(context, it)
                onClose.invoke()
            }
        )
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
            onClose.invoke()
        }
    }

    if (showZoomablePhoto) {
        ContactProfilePicture(contact) {
            showZoomablePhoto = false
        }
    }
}
