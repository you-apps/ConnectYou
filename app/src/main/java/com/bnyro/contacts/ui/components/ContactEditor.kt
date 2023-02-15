package com.bnyro.contacts.ui.components

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        mutableStateListOf(
            *contact?.numbers.fillIfEmpty().map { mutableStateOf(it) }.toTypedArray()
        )
    }

    val emails = remember {
        mutableStateListOf(
            *contact?.emails.fillIfEmpty().map { mutableStateOf(it) }.toTypedArray()
        )
    }

    val addresses = remember {
        mutableStateListOf(
            *contact?.addresses.fillIfEmpty().map { mutableStateOf(it) }.toTypedArray()
        )
    }

    val events = remember {
        mutableStateListOf(
            *contact?.events.fillIfEmpty().map { mutableStateOf(it) }.toTypedArray()
        )
    }

    val uploadImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) {
        context.contentResolver.openInputStream(it ?: return@rememberLauncherForActivityResult)?.use { stream ->
            profilePicture = BitmapFactory.decodeStream(stream)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
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
            }

            item {
                LabeledTextField(
                    label = R.string.first_name,
                    state = firstName
                )
            }

            item {
                LabeledTextField(
                    label = R.string.last_name,
                    state = surName
                )
            }

            itemsIndexed(phoneNumber) { index, it ->
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

            itemsIndexed(emails) { index, it ->
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

            itemsIndexed(addresses) { index, it ->
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

            itemsIndexed(events) { index, it ->
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
