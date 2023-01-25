package com.bnyro.contacts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.enums.IntentActionType
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.ContactEntry
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.components.dialogs.ConfirmationDialog
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.util.IntentHelper

@Composable
fun SingleContactScreen(contact: ContactData, onClose: () -> Unit) {
    val viewModel: ContactsModel = viewModel()
    val context = LocalContext.current
    var showDelete by remember {
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
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .fillMaxWidth()
                    .clip(shape),
                shape = shape
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 50.dp)
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
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = (contact.displayName?.firstOrNull() ?: "").toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 65.sp
                        )
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
                            contact.phoneNumber.firstOrNull() ?: return@ClickableIcon
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    ClickableIcon(icon = Icons.Default.Send) {
                        IntentHelper.launchAction(
                            context,
                            IntentActionType.SMS,
                            contact.phoneNumber.firstOrNull() ?: return@ClickableIcon
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    ClickableIcon(icon = Icons.Default.Delete) {
                        showDelete = true
                    }
                }
            }

            contact.phoneNumber.forEach {
                ContactEntry(label = "Phone", content = it) {}
            }

            if (showDelete) {
                ConfirmationDialog(
                    onDismissRequest = {
                        showDelete = false
                    },
                    title = "Delete contact",
                    text = "Are you sure? This can't be undone!"
                ) {
                    viewModel.deleteContact(contact)
                    onClose.invoke()
                }
            }
        }
    }
}
