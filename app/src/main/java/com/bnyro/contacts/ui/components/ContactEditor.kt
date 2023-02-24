package com.bnyro.contacts.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ValueWithType
import com.bnyro.contacts.ui.components.base.LabeledTextField
import com.bnyro.contacts.ui.components.editor.DatePickerEditor
import com.bnyro.contacts.ui.components.editor.TextFieldEditor
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.ImageHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactEditor(
    contact: ContactData? = null,
    onSave: (contact: ContactData) -> Unit
) {
    val context = LocalContext.current

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

    var profilePicture by remember {
        mutableStateOf(contact?.photo)
    }

    val firstName = remember {
        mutableStateOf(contact?.firstName.orEmpty())
    }

    val surName = remember {
        mutableStateOf(contact?.surName.orEmpty())
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

    val uploadImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        ImageHelper.getImageFromUri(context, uri ?: return@rememberLauncherForActivityResult)?.let { bitmap ->
            profilePicture = bitmap
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
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

            phoneNumber.forEachIndexed { index, it ->
                TextFieldEditor(
                    label = R.string.phone,
                    state = it,
                    types = ContactsHelper.phoneNumberTypes,
                    keyboardType = KeyboardType.Phone,
                    onDelete = {
                        phoneNumber.removeAt(index)
                    },
                    showDeleteAction = phoneNumber.size > 1 && index == phoneNumber.size - 1
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
                    showDeleteAction = emails.size > 1 && index == emails.size - 1
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
                    showDeleteAction = addresses.size > 1 && index == addresses.size - 1
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
                    showDeleteAction = events.size > 1 && index == events.size - 1
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
                    showDeleteAction = notes.size > 1 && index == notes.size - 1
                ) {
                    notes.add(emptyMutable())
                }
            }

            Spacer(Modifier.height(100.dp))
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = {
                val editedContact = (contact ?: ContactData()).also {
                    it.firstName = firstName.value.trim()
                    it.surName = surName.value.trim()
                    it.displayName = "${firstName.value.trim()} ${surName.value.trim()}"
                    it.photo = profilePicture
                    it.numbers = phoneNumber.clean()
                    it.emails = emails.clean()
                    it.addresses = addresses.clean()
                    it.events = events.clean()
                    it.notes = notes.clean()
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
}
