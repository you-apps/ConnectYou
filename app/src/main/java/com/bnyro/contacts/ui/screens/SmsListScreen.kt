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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.SortOrder
import com.bnyro.contacts.ui.components.ContactItem
import com.bnyro.contacts.ui.components.NothingHere
import com.bnyro.contacts.ui.components.dialogs.DialogButton
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.util.NotificationHelper
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.SmsUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsListScreen(smsModel: SmsModel, contactsModel: ContactsModel) {
    val context = LocalContext.current
    var showContactPicker by remember {
        mutableStateOf(false)
    }

    var smsAddress by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        PermissionHelper.checkPermissions(context, NotificationHelper.notificationPermissions)
        smsModel.fetchSmsList(context)
    }

    Box {
        if (smsModel.smsList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                val smsList = smsModel.smsGroups.entries.toList()
                    .sortedBy { (_, smsList) -> smsList.maxOf { it.timestamp } }
                    .reversed()

                items(smsList) { (threadId, smsList) ->
                    var showThreadScreen by remember {
                        mutableStateOf(false)
                    }
                    val address = smsList.first().address
                    val contactData = contactsModel.getContactByNumber(address)

                    val dismissState = rememberDismissState(
                        confirmValueChange = {
                            if (it == DismissValue.DismissedToEnd) {
                                SmsUtil.deleteThread(context, threadId)
                                smsModel.fetchSmsList(context)
                                return@rememberDismissState true
                            }
                            return@rememberDismissState false
                        }
                    )

                    SwipeToDismiss(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        state = dismissState,
                        background = {},
                        dismissContent = {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CardDefaults.shape)
                                    .clickable {
                                        showThreadScreen = true
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            modifier = Modifier
                                                .padding(15.dp)
                                                .fillMaxSize(),
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Text(
                                            text = contactData?.displayName ?: address,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = smsList.first().body,
                                            maxLines = 2,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        },
                        directions = setOf(DismissDirection.StartToEnd)
                    )

                    if (showThreadScreen) {
                        SmsThreadScreen(smsModel, contactData, address) {
                            showThreadScreen = false
                        }
                    }
                }
            }
        } else {
            NothingHere()
        }

        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                showContactPicker = true
            }
        ) {
            Icon(Icons.Default.Edit, null)
        }

        if (showContactPicker) {
            AlertDialog(
                onDismissRequest = { showContactPicker = false },
                confirmButton = {
                    DialogButton(text = stringResource(R.string.cancel)) {
                        showContactPicker = false
                    }
                },
                title = {
                    Text(stringResource(R.string.pick_contact))
                },
                text = {
                    LazyColumn {
                        items(contactsModel.contacts) {
                            ContactItem(
                                contact = it,
                                sortOrder = SortOrder.FIRSTNAME,
                                selected = false,
                                onSinglePress = {
                                    smsAddress = it.numbers.firstOrNull()?.value
                                    showContactPicker = false
                                    true
                                },
                                onLongPress = {}
                            )
                        }
                    }
                }
            )
        }

        smsAddress?.let {
            val contactData = contactsModel.getContactByNumber(it)
            SmsThreadScreen(smsModel, contactData, it) {
                smsAddress = null
            }
        }

        smsModel.initialAddressAndBody?.let {
            val contactData = contactsModel.getContactByNumber(it.first)
            SmsThreadScreen(smsModel, contactData, it.first) {
                smsModel.initialAddressAndBody = null
            }
        }
    }
}