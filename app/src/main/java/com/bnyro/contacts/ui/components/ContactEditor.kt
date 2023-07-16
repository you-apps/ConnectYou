package com.bnyro.contacts.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ValueWithType
import com.bnyro.contacts.ui.components.base.LabeledTextField
import com.bnyro.contacts.ui.components.dialogs.DialogButton
import com.bnyro.contacts.ui.components.dialogs.GroupsDialog
import com.bnyro.contacts.ui.components.editor.DatePickerEditor
import com.bnyro.contacts.ui.components.editor.TextFieldEditor
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.DeviceContactsHelper
import com.bnyro.contacts.util.ImageHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactEditor(
    modifier: Modifier = Modifier,
    contact: ContactData? = null,
    isCreatingNewDeviceContact: Boolean,
    onSave: (contact: ContactData) -> Unit
) {
    val context = LocalContext.current
    val contactsModel: ContactsModel = viewModel()

    fun List<ValueWithType>?.fillIfEmpty(): List<ValueWithType> {
        return if (this.isNullOrEmpty()) {
            listOf(ValueWithType("", 0))
        } else {
            this
        }
    }

    fun List<MutableState<ValueWithType>>.clean(): List<ValueWithType> {
        return this.filter { it.value.value.isNotBlank() }.map { it.value }
    }

    fun emptyMutable() = mutableStateOf(ValueWithType("", 0))

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

    var showAccountTypeDialog by remember {
        mutableStateOf(false)
    }

    var groups by remember {
        mutableStateOf(contact?.groups.orEmpty())
    }

    var selectedAccount by remember {
        mutableStateOf(
            (contact?.accountType ?: DeviceContactsHelper.ANDROID_ACCOUNT_TYPE) to
                    (contact?.accountName ?: DeviceContactsHelper.ANDROID_CONTACTS_NAME)
        )
    }

    val availableAccounts = remember {
        contactsModel.getAvailableAccounts()
    }

    val uploadImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        ImageHelper.getImageFromUri(context, uri ?: return@rememberLauncherForActivityResult)
            ?.let { bitmap ->
                profilePicture = bitmap
            }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 50.dp, bottom = 15.dp)
                    .size(180.dp)
                    .clip(RoundedCornerShape(20.dp))
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
                    )
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
                        modifier = Modifier.fillMaxSize(),
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                }
            }

            LabeledTextField(
                label = R.string.first_name,
                state = firstName
            )

            LabeledTextField(
                label = R.string.last_name,
                state = surName
            )

            AnimatedVisibility(showAdvanced) {
                Column {
                    LabeledTextField(
                        label = R.string.nick_name,
                        state = nickName
                    )
                    LabeledTextField(
                        label = R.string.organization,
                        state = organization
                    )
                    websites.forEachIndexed { index, it ->
                        TextFieldEditor(
                            label = R.string.website,
                            state = it,
                            types = ContactsHelper.websiteTypes,
                            keyboardType = KeyboardType.Uri,
                            onDelete = {
                                websites.removeAt(index)
                            },
                            showDeleteAction = websites.size > 1,
                            moveToTop = {
                                websites.add(0, it)
                                websites.removeAt(index + 1)
                            }
                        ) {
                            websites.add(emptyMutable())
                        }
                    }
                }
            }

            Button(
                onClick = {
                    showAdvanced = !showAdvanced
                }
            ) {
                Text(
                    stringResource(
                        if (showAdvanced) R.string.show_less else R.string.show_more_fields
                    )
                )
            }

            phoneNumber.forEachIndexed { index, it ->
                TextFieldEditor(
                    label = R.string.phone,
                    state = it,
                    types = ContactsHelper.phoneNumberTypes,
                    keyboardType = KeyboardType.Phone,
                    onDelete = {
                        phoneNumber.removeAt(index)
                    },
                    showDeleteAction = phoneNumber.size > 1,
                    moveToTop = {
                        phoneNumber.add(0, it)
                        phoneNumber.removeAt(index + 1)
                    }
                ) {
                    phoneNumber.add(emptyMutable())
                }
            }

            emails.forEachIndexed { index, it ->
                TextFieldEditor(
                    label = R.string.email,
                    state = it,
                    types = ContactsHelper.emailTypes,
                    keyboardType = KeyboardType.Email,
                    onDelete = {
                        emails.removeAt(index)
                    },
                    showDeleteAction = emails.size > 1
                ) {
                    emails.add(emptyMutable())
                }
            }

            addresses.forEachIndexed { index, it ->
                TextFieldEditor(
                    label = R.string.address,
                    state = it,
                    types = ContactsHelper.addressTypes,
                    imeAction = if (it == addresses.last()) ImeAction.Done else ImeAction.Next,
                    onDelete = {
                        addresses.removeAt(index)
                    },
                    showDeleteAction = addresses.size > 1
                ) {
                    addresses.add(emptyMutable())
                }
            }

            events.forEachIndexed { index, it ->
                DatePickerEditor(
                    label = R.string.event,
                    state = it,
                    types = ContactsHelper.eventTypes,
                    onDelete = {
                        events.removeAt(index)
                    },
                    showDeleteAction = events.size > 1
                ) {
                    events.add(emptyMutable())
                }
            }

            notes.forEachIndexed { index, it ->
                TextFieldEditor(
                    label = R.string.note,
                    state = it,
                    types = listOf(),
                    imeAction = if (it == notes.last()) ImeAction.Done else ImeAction.Next,
                    onDelete = {
                        notes.removeAt(index)
                    },
                    showDeleteAction = notes.size > 1
                ) {
                    notes.add(emptyMutable())
                }
            }

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

                if (isCreatingNewDeviceContact && availableAccounts.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            showAccountTypeDialog = true
                        }
                    ) {
                        Text(
                            text = "${stringResource(R.string.account_type)}: ${
                                selectedAccount.second.ifBlank { selectedAccount.first }
                            }"
                        )
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = {
                val editedContact = (contact ?: ContactData()).also {
                    it.firstName = firstName.value.trim()
                    it.surName = surName.value.trim()
                    it.nickName = nickName.value.takeIf { n -> n.isNotBlank() }?.trim()
                    it.organization = organization.value.takeIf { o -> o.isNotBlank() }?.trim()
                    it.displayName = "${firstName.value.trim()} ${surName.value.trim()}"
                    it.photo = profilePicture
                    it.accountType = selectedAccount.first
                    it.accountName = selectedAccount.second
                    it.websites = websites.clean()
                    it.numbers = phoneNumber.clean()
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
    }

    if (showGroupsDialog) {
        GroupsDialog(
            onDismissRequest = { showGroupsDialog = false },
            participatingGroups = groups
        ) {
            groups = it
        }
    }

    if (showAccountTypeDialog) {
        AlertDialog(
            onDismissRequest = { showAccountTypeDialog = false },
            confirmButton = {
                DialogButton(text = stringResource(R.string.cancel)) {
                    showAccountTypeDialog = false
                }
            },
            title = {
                Text(text = stringResource(R.string.account_type))
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableAccounts) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    selectedAccount = it
                                    showAccountTypeDialog = false
                                }
                                .padding(vertical = 15.dp, horizontal = 20.dp),
                            text = it.second
                        )
                    }
                }
            }
        )
    }
}
