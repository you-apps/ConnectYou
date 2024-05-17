package com.bnyro.contacts.presentation.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.SortOrder
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.ContactSingleDataItem
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.editor.components.ContactIconPlaceholder
import com.bnyro.contacts.presentation.screens.settings.model.ThemeModel

@Composable
fun NumberPickerDialog(
    contactsModel: ContactsModel,
    themeModel: ThemeModel,
    onDismissRequest: () -> Unit,
    onNumberSelect: (number: String, contactData: ContactData?) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = remember { DialogProperties(usePlatformDefaultWidth = false) }) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                var filteredContacts by remember {
                    mutableStateOf(contactsModel.contacts.flatMap {
                        it.numbers.map { num ->
                            ContactSingleDataItem(
                                name = it.getNameBySortOrder(SortOrder.FIRSTNAME).orEmpty(),
                                data = num.value,
                                thumbnail = it.thumbnail,
                                contactData = it
                            )
                        }
                    })
                }
                var searchQuery by remember {
                    mutableStateOf("")
                }
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        val lowerQuery = searchQuery.lowercase()
                        filteredContacts = contactsModel.contacts.flatMap {
                            it.numbers.map { num ->
                                ContactSingleDataItem(
                                    name = it.getNameBySortOrder(SortOrder.FIRSTNAME).orEmpty(),
                                    data = num.value,
                                    thumbnail = it.thumbnail,
                                    contactData = it
                                )
                            }
                        }.filter { item ->
                            item.name.lowercase()
                                .contains(lowerQuery) || item.data.lowercase().contains(lowerQuery)
                        }
                    },
                    placeholder = { Text(stringResource(R.string.search_contacts)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(50),
                    leadingIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                                contentDescription = null
                            )
                        }
                    }
                )

                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    if (searchQuery.length > 2) {
                        item {
                            NewNumberCard(
                                number = searchQuery,
                                onClick = {
                                    onNumberSelect.invoke(searchQuery, null)
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                    items(filteredContacts) {
                        ContactCard(
                            name = it.name,
                            number = it.data,
                            thumbnail = it.thumbnail,
                            themeModel = themeModel,
                            onClick = {
                                onNumberSelect.invoke(it.data, it.contactData)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactCard(
    name: String,
    number: String,
    thumbnail: Any?,
    themeModel: ThemeModel,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .clickable {
                onClick.invoke()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (thumbnail != null) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        )
                ) {
                    AsyncImage(
                        model = thumbnail,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                ContactIconPlaceholder(
                    themeModel = themeModel,
                    firstChar = name.firstOrNull(),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NewNumberCard(
    number: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .clickable {
                onClick.invoke()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    imageVector = Icons.Rounded.AddComment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.start_a_new_conversation),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}