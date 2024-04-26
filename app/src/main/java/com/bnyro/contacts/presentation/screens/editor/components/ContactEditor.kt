package com.bnyro.contacts.presentation.screens.editor.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Cases
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Person2
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Web
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.AccountType
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.ValueWithType
import com.bnyro.contacts.presentation.components.LabeledTextField
import com.bnyro.contacts.presentation.features.GroupsDialog
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.ImageHelper
import com.bnyro.contacts.util.Preferences

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactEditor(
    modifier: Modifier = Modifier,
    contact: ContactData? = null,
    isCreatingNewDeviceContact: Boolean,
    onSave: (contact: ContactData) -> Unit
) {
    val context = LocalContext.current
    val contactsModel: ContactsModel = viewModel(factory = ContactsModel.Factory)

    fun List<ValueWithType>?.fillIfEmpty(): List<ValueWithType> {
        if (this.isNullOrEmpty()) {
            return listOf(ValueWithType("", 0))
        }

        return this
    }

    fun List<MutableState<ValueWithType>>.clean(): List<ValueWithType> {
        return this.filter { it.value.value.isNotBlank() }.map { it.value }.distinct()
    }

    var showAdvanced by remember {
        mutableStateOf(false)
    }

    var profilePicture by remember {
        mutableStateOf(contact?.photo)
    }

    val firstName = remember {
        mutableStateOf(contact?.firstName.orEmpty())
    }

    val surName = remember {
        mutableStateOf(contact?.surName.orEmpty())
    }

    val nickName = remember {
        mutableStateOf(contact?.nickName.orEmpty())
    }

    val title = remember {
        mutableStateOf(contact?.title.orEmpty())
    }

    val organization = remember {
        mutableStateOf(contact?.organization.orEmpty())
    }

    val websites = remember {
        contact?.websites.fillIfEmpty().map { mutableStateOf(it) }.toMutableStateList()
    }

    val phoneNumber = remember {
        contact?.numbers.fillIfEmpty().map { mutableStateOf(it) }.toMutableStateList()
    }

    val emails = remember {
        contact?.emails.fillIfEmpty().map { mutableStateOf(it) }.toMutableStateList()
    }

    val addresses = remember {
        contact?.addresses.fillIfEmpty().map { mutableStateOf(it) }.toMutableStateList()
    }

    val events = remember {
        contact?.events.fillIfEmpty().map { mutableStateOf(it) }.toMutableStateList()
    }

    val notes = remember {
        contact?.notes.fillIfEmpty().map { mutableStateOf(it) }.toMutableStateList()
    }

    var showGroupsDialog by remember {
        mutableStateOf(false)
    }

    var groups by remember {
        mutableStateOf(contact?.groups.orEmpty())
    }

    var selectedAccount by remember {
        val lastChosenAccount = Preferences.getLastChosenAccount()
        val account = contact?.let {
            AccountType(it.accountName.orEmpty(), it.accountType.orEmpty())
        } ?: lastChosenAccount
        mutableStateOf(account)
    }

    val availableAccounts = remember {
        contactsModel.getAvailableAccounts(context)
    }

    val uploadImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        ImageHelper.getImageFromUri(context, uri ?: return@rememberLauncherForActivityResult)
            ?.let { bitmap ->
                profilePicture = bitmap
            }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val editedContact = (contact ?: ContactData()).also {
                        it.firstName = firstName.value.trim()
                        it.surName = surName.value.trim()
                        it.nickName = nickName.value.takeIf { n -> n.isNotBlank() }?.trim()
                        it.organization = organization.value.takeIf { o -> o.isNotBlank() }?.trim()
                        it.title = title.value.takeIf { o -> o.isNotBlank() }?.trim()
                        it.displayName = "${firstName.value.trim()} ${surName.value.trim()}"
                        it.photo = profilePicture
                        it.accountType = selectedAccount.type
                        it.accountName = selectedAccount.name
                        it.websites = websites.clean()
                        it.numbers = phoneNumber.clean().map { number ->
                            ValueWithType(ContactsHelper.normalizePhoneNumber(number.value), number.type)
                        }
                        it.emails = emails.clean()
                        it.addresses = addresses.clean()
                        it.events = events.clean()
                        it.notes = notes.clean()
                        it.groups = groups
                    }
                    onSave.invoke(editedContact)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
            }
        }, topBar = {
            if (isCreatingNewDeviceContact) {
                TopAppBar(title = {
                    var expanded by remember { mutableStateOf(false) }
                    Row(
                        Modifier
                            .padding(8.dp)
                            .clickable {
                                expanded = true
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedAccount.name.ifBlank { selectedAccount.type }
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableAccounts.forEach {
                            DropdownMenuItem(
                                text = { Text(it.name) },
                                onClick = {
                                    selectedAccount = it
                                    Preferences.edit {
                                        putString(Preferences.lastChosenAccount, it.identifier)
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                })
            }
        }
    ) { pV ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(pV)
                .padding(horizontal = 8.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 50.dp, bottom = 15.dp)
                    .size(180.dp)
                    .clip(if (profilePicture == null) CircleShape else RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .combinedClickable(
                        onClick = {
                            val request = PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                            uploadImage.launch(request)
                        },
                        onLongClick = {
                            profilePicture = null
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                profilePicture?.let {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    Icon(
                        modifier = Modifier.size(48.dp),
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            LabeledTextField(
                label = R.string.first_name,
                state = firstName,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person2,
                        contentDescription = null
                    )
                }
            )

            LabeledTextField(
                label = R.string.last_name,
                state = surName,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person2,
                        contentDescription = null
                    )
                }
            )

            AnimatedVisibility(showAdvanced) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledTextField(
                        label = R.string.nick_name,
                        state = nickName,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person2,
                                contentDescription = null
                            )
                        }
                    )
                    LabeledTextField(
                        label = R.string.organization,
                        state = organization,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Cases,
                                contentDescription = null
                            )
                        }
                    )
                    LabeledTextField(
                        label = R.string.title,
                        state = title,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Cases,
                                contentDescription = null
                            )
                        }
                    )
                    TextFieldGroup(
                        items = websites,
                        label = R.string.website,
                        types = ContactsHelper.websiteTypes,
                        addActionText = R.string.add_website,
                        keyboardType = KeyboardType.Uri,
                        leadingIcon = Icons.Outlined.Web
                    )
                }
            }

            Row(
                Modifier.clickable {
                    showAdvanced = !showAdvanced
                }
            ) {
                Text(
                    stringResource(
                        if (showAdvanced) R.string.show_less else R.string.show_more_fields
                    )
                )
                Icon(
                    imageVector = if (showAdvanced) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null
                )
            }

            TextFieldGroup(
                items = phoneNumber,
                label = R.string.phone,
                addActionText = R.string.add_phone_number,
                keyboardType = KeyboardType.Phone,
                leadingIcon = Icons.Outlined.Phone,
                types = ContactsHelper.phoneNumberTypes
            )
            TextFieldGroup(
                items = emails,
                label = R.string.email,
                addActionText = R.string.add_e_mail,
                keyboardType = KeyboardType.Email,
                leadingIcon = Icons.Outlined.Email,
                types = ContactsHelper.emailTypes
            )
            TextFieldGroup(
                items = addresses,
                label = R.string.address,
                addActionText = R.string.add_address,
                leadingIcon = Icons.Outlined.LocationOn,
                types = ContactsHelper.addressTypes
            )

            EventFieldGroup(
                items = events,
                label = R.string.event,
                types = ContactsHelper.eventTypes,
                addActionText = R.string.add_event
            )

            TextFieldGroup(
                items = notes,
                label = R.string.note,
                addActionText = R.string.add_note,
                leadingIcon = Icons.Outlined.Note
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 10.dp, start = 10.dp)
            ) {
                Button(
                    onClick = {
                        showGroupsDialog = true
                    }
                ) {
                    Text(stringResource(R.string.manage_groups))
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }

    if (showGroupsDialog) {
        GroupsDialog(
            onDismissRequest = { showGroupsDialog = false },
            participatingGroups = groups
        ) {
            groups = it
        }
    }
}