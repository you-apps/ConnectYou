package com.bnyro.contacts.presentation.screens.contact

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.IntentActionType
import com.bnyro.contacts.domain.enums.ListAttribute
import com.bnyro.contacts.domain.enums.Notes
import com.bnyro.contacts.domain.enums.StringAttribute
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.presentation.components.ClickableIcon
import com.bnyro.contacts.presentation.components.FullScreenDialog
import com.bnyro.contacts.presentation.components.LargeButtonWithIcon
import com.bnyro.contacts.presentation.components.SmallButtonWithIcon
import com.bnyro.contacts.presentation.components.shapes.curlyCornerShape
import com.bnyro.contacts.presentation.features.ConfirmationDialog
import com.bnyro.contacts.presentation.features.ShareDialog
import com.bnyro.contacts.presentation.features.ShortcutDialog
import com.bnyro.contacts.presentation.screens.contact.components.ContactProfilePicture
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.editor.EditorScreen
import com.bnyro.contacts.presentation.screens.editor.components.ContactEntryGroup
import com.bnyro.contacts.presentation.screens.editor.components.ContactEntryTextGroup
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.IntentHelper
import com.bnyro.contacts.util.RingtonePickContract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleContactScreen(contact: ContactData, viewModel: ContactsModel, onClose: () -> Unit) {
    val context = LocalContext.current
    var showDelete by rememberSaveable {
        mutableStateOf(false)
    }
    var showEditor by rememberSaveable {
        mutableStateOf(false)
    }
    var showZoomablePhoto by rememberSaveable {
        mutableStateOf(false)
    }
    var showShortcutDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showShareDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var isFavorite by rememberSaveable {
        mutableStateOf(contact.favorite)
    }

    val ringtonePicker =
        rememberLauncherForActivityResult(contract = RingtonePickContract()) { uri ->
            if (uri != null) {
                viewModel.updateContactRingTone(contact, uri)
            }
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
                            icon = if (isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = R.string.favorite
                        ) {
                            isFavorite = !isFavorite
                            contact.favorite = isFavorite
                            viewModel.setFavorite(context, contact, isFavorite)
                        }
                        ClickableIcon(
                            icon = Icons.Default.Edit,
                            contentDescription = R.string.edit
                        ) {
                            showEditor = true
                        }
                        Box {
                            var showMore by remember { mutableStateOf(false) }
                            ClickableIcon(
                                icon = Icons.Rounded.MoreVert,
                                contentDescription = R.string.more
                            ) {
                                showMore = !showMore
                            }
                            DropdownMenu(
                                expanded = showMore,
                                onDismissRequest = {
                                    showMore = false
                                }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = stringResource(R.string.change_ringtone))
                                    },
                                    onClick = {
                                        ringtonePicker.launch()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(text = stringResource(R.string.create_shortcut))
                                    },
                                    onClick = {
                                        showShortcutDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(text = stringResource(R.string.delete_contact))
                                    },
                                    onClick = {
                                        showDelete = true
                                    }
                                )
                            }
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
                            .size(240.dp)
                            .background(
                                shape = curlyCornerShape,
                                color = MaterialTheme.colorScheme.primary
                            )
                    ) {
                        if (contact.photo == null) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = (contact.displayName?.firstOrNull() ?: "").toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 100.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(curlyCornerShape)
                                    .clickable {
                                        showZoomablePhoto = true
                                    },
                                bitmap = contact.photo!!.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                    Text(
                        text = contact.displayName.orEmpty(),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 15.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SmallButtonWithIcon(
                            imageVector = Icons.Default.Message,
                            text = stringResource(R.string.message)
                        ) {
                            IntentHelper.launchAction(
                                context,
                                IntentActionType.SMS,
                                contact.numbers.firstOrNull()?.value ?: return@SmallButtonWithIcon
                            )
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        SmallButtonWithIcon(
                            imageVector = Icons.Default.Share,
                            text = stringResource(R.string.share)
                        ) {
                            showShareDialog = true
                        }
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    LargeButtonWithIcon(
                        imageVector = Icons.Default.Call,
                        text = stringResource(R.string.dial)
                    ) {
                        IntentHelper.launchAction(
                            context,
                            IntentActionType.DIAL,
                            contact.numbers.firstOrNull()?.value ?: return@LargeButtonWithIcon
                        )
                    }
                }

                for (attributesType in ContactsHelper.contactAttributesTypes) {
                    if (attributesType is StringAttribute) {
                        ContactEntryTextGroup(
                            label = stringResource(attributesType.stringRes),
                            entries = listOfNotNull(attributesType.display(contact))
                        )
                    } else if (attributesType is ListAttribute) {
                        ContactEntryGroup(
                            label = stringResource(attributesType.stringRes),
                            entries = attributesType.display(contact),
                            types = attributesType.types,
                            useMarkdown = attributesType is Notes
                        ) {
                            attributesType.intentActionType?.let { intentActionType ->
                                IntentHelper.launchAction(context, intentActionType, it.value)
                            }
                        }
                    }
                }

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
