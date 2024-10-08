package com.bnyro.contacts.presentation.screens.contacts.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.domain.enums.SortOrder
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.presentation.screens.contact.SingleContactScreen
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.editor.components.ContactIconPlaceholder
import com.bnyro.contacts.presentation.screens.settings.model.ThemeModel
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactItem(
    modifier: Modifier = Modifier,
    contact: ContactData,
    sortOrder: SortOrder,
    selected: Boolean,
    onSinglePress: () -> Boolean,
    onLongPress: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val viewModel: ContactsModel = viewModel(factory = ContactsModel.Factory)
    val themeModel: ThemeModel = viewModel()

    var showContactScreen by rememberSaveable {
        mutableStateOf(false)
    }

    val contactName = contact.getNameBySortOrder(sortOrder).orEmpty().trim()

    ElevatedCard(
        modifier = modifier
            .padding(vertical = 5.dp)
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
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val thumbnail = contact.thumbnail ?: contact.photo
            if (selected || thumbnail != null) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        )
                ) {
                    if (selected) {
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else if (thumbnail != null) {
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
            } else {
                ContactIconPlaceholder(
                    themeModel = themeModel,
                    firstChar = contactName.firstOrNull(),
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(contactName)
        }
    }

    if (showContactScreen) {
        val data = runBlocking {
            viewModel.loadAdvancedContactData(contact)
        }
        SingleContactScreen(data, viewModel) {
            showContactScreen = false
        }
    }
}
