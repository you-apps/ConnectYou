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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.R
import com.bnyro.contacts.ui.components.NothingHere
import com.bnyro.contacts.ui.components.NumberPickerDialog
import com.bnyro.contacts.ui.components.dialogs.ConfirmationDialog
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.SmsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsListScreen(smsModel: SmsModel, contactsModel: ContactsModel) {
    val context = LocalContext.current
    var showNumberPicker by remember {
        mutableStateOf(false)
    }

    var smsAddress by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(floatingActionButton = {
        FloatingActionButton(
            onClick = {
                showNumberPicker = true
            }
        ) {
            Icon(Icons.Default.Edit, null)
        }
    }) { pv ->
        val smsList by smsModel.smsList.collectAsState()
        if (smsList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv)
            ) {
                val smsGroups = smsList.groupBy { it.threadId }.toList()
                    .sortedBy { (_, smsList) -> smsList.maxOf { it.timestamp } }
                    .reversed()

                items(smsGroups) { (threadId, smsList) ->
                    var showThreadScreen by remember {
                        mutableStateOf(false)
                    }
                    var showDeleteThreadDialog by remember {
                        mutableStateOf(false)
                    }

                    // safe call to avoid crashes when re-rendering
                    val address = smsList.firstOrNull()?.address.orEmpty()
                    val contactData = contactsModel.getContactByNumber(address)

                    val dismissState = rememberDismissState(
                        confirmValueChange = {
                            if (it == DismissValue.DismissedToEnd) {
                                showDeleteThreadDialog = true
                            }
                            return@rememberDismissState false
                        }
                    )

                    SwipeToDismiss(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        state = dismissState,
                        background = {},
                        dismissContent = {
                            val shape = RoundedCornerShape(20.dp)

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape)
                                    .clickable {
                                        showThreadScreen = true
                                    },
                                shape = shape
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 5.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(58.dp)
                                            .clip(CircleShape)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (contactData?.thumbnail != null) {
                                            Image(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
                                                bitmap = contactData.thumbnail!!.asImageBitmap(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Image(
                                                modifier = Modifier
                                                    .padding(vertical = 12.dp)
                                                    .fillMaxSize(),
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                colorFilter = ColorFilter.tint(
                                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                        10.dp
                                                    )
                                                )
                                            )
                                        }
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
                                            // safe call to avoid crashes when re-rendering
                                            text = smsList.lastOrNull()?.body.orEmpty(),
                                            maxLines = 2,
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp
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

                    if (showDeleteThreadDialog) {
                        ConfirmationDialog(
                            onDismissRequest = { showDeleteThreadDialog = false },
                            title = stringResource(R.string.delete_thread),
                            text = stringResource(R.string.irreversible)
                        ) {
                            smsModel.deleteThread(context, threadId)
                        }
                    }
                }
            }
        } else {
            Column(Modifier.padding(pv)) {
                NothingHere()
            }
        }

        if (showNumberPicker) {
            NumberPickerDialog(
                onDismissRequest = { showNumberPicker = false },
                onNumberSelect = {
                    smsAddress = it
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
