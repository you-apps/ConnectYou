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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.bnyro.contacts.obj.ValueWithType
import com.bnyro.contacts.ui.components.ContactEntryGroup
import com.bnyro.contacts.ui.components.ContactEntryTextGroup
import com.bnyro.contacts.ui.components.ContactProfilePicture
import com.bnyro.contacts.ui.components.ShareDialog
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.components.dialogs.ConfirmationDialog
import com.bnyro.contacts.ui.components.dialogs.ShortcutDialog
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.util.CalendarUtils
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.IntentHelper

@OptIn(ExperimentalMaterial3Api::class)
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
    var showShortcutDialog by remember {
        mutableStateOf(false)
    }
    var showShareDialog by remember {
        mutableStateOf(false)
    }

    FullScreenDialog(onClose = onClose) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        ClickableIcon(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = R.string.okay
                        ) {
                            onClose.invoke()
                        }
                    },
                    actions = {
                        ClickableIcon(
                            icon = Icons.Default.Shortcut,
                            contentDescription = R.string.create_shortcut
                        ) {
                            showShortcutDialog = true
                        }
                    }
                )
            }
        ) { pV ->
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(pV),
                horizontalAlignment = Alignment.CenterHorizontally
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

                ElevatedCard(
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 15.dp)
                    ) {
                        ClickableIcon(
                            icon = Icons.Default.Call,
                            contentDescription = R.string.dial
                        ) {
                            IntentHelper.launchAction(
                                context,
                                IntentActionType.DIAL,
                                contact.numbers.firstOrNull()?.value ?: return@ClickableIcon
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        ClickableIcon(
                            icon = Icons.Default.Message,
                            contentDescription = R.string.message
                        ) {
                            IntentHelper.launchAction(
                                context,
                                IntentActionType.SMS,
                                contact.numbers.firstOrNull()?.value ?: return@ClickableIcon
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        ClickableIcon(
                            icon = Icons.Default.Share,
                            contentDescription = R.string.share
                        ) {
                            showShareDialog = true
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        ClickableIcon(
                            icon = Icons.Default.Edit,
                            contentDescription = R.string.edit
                        ) {
                            showEditor = true
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        ClickableIcon(
                            icon = Icons.Default.Delete,
                            contentDescription = R.string.delete
                        ) {
                            showDelete = true
                        }
                    }
                }

                ContactEntryTextGroup(
                    label = stringResource(R.string.nick_name),
                    entries = listOfNotNull(contact.nickName)
                )

                ContactEntryTextGroup(
                    label = stringResource(R.string.organization),
                    entries = listOfNotNull(contact.organization)
                )

                ContactEntryGroup(
                    label = stringResource(R.string.website),
                    entries = contact.websites
                ) {
                    IntentHelper.launchAction(context, IntentActionType.WEBSITE, it.value)
                }

                ContactEntryGroup(
                    label = stringResource(R.string.phone),
                    entries = contact.numbers,
                    types = ContactsHelper.phoneNumberTypes
                ) {
                    IntentHelper.launchAction(context, IntentActionType.DIAL, it.value)
                }

                ContactEntryGroup(
                    label = stringResource(R.string.email),
                    entries = contact.emails,
                    types = ContactsHelper.emailTypes
                ) {
                    IntentHelper.launchAction(context, IntentActionType.EMAIL, it.value)
                }

                ContactEntryGroup(
                    label = stringResource(R.string.address),
                    entries = contact.addresses,
                    types = ContactsHelper.addressTypes
                ) {
                    IntentHelper.launchAction(context, IntentActionType.ADDRESS, it.value)
                }

                ContactEntryGroup(
                    label = stringResource(R.string.event),
                    entries = contact.events.map {
                        ValueWithType(CalendarUtils.localizeIsoDate(it.value), it.type)
                    },
                    types = ContactsHelper.eventTypes
                )

                ContactEntryGroup(
                    label = stringResource(R.string.note),
                    entries = contact.notes
                )

                ContactEntryTextGroup(
                    label = stringResource(R.string.groups),
                    entries = contact.groups.map { it.title }
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
            viewModel.deleteContacts(listOf(contact))
            onClose.invoke()
        }
    }

    if (showZoomablePhoto) {
        ContactProfilePicture(contact) {
            showZoomablePhoto = false
        }
    }

    if (showShortcutDialog) {
        ShortcutDialog(contact) {
            showShortcutDialog = false
        }
    }

    if (showShareDialog) {
        ShareDialog(contact) {
            showShareDialog = false
        }
    }
}
