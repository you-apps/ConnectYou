package com.bnyro.contacts.ui.screens

import android.provider.Telephony
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.SmsData
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.util.SmsUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsThreadScreen(
    smsModel: SmsModel,
    contactData: ContactData?,
    address: String,
    initialText: String = "",
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var smsList by remember {
        mutableStateOf(listOf<SmsData>())
    }
    val lazyListState = rememberLazyListState()
    var showContactScreen by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(address + smsModel.smsList) {
        smsList = smsModel.smsList
            .filter { it.address == address }
            .sortedBy { it.timestamp }
        if (smsList.isNotEmpty()) lazyListState.scrollToItem(smsList.size - 1)
    }

    FullScreenDialog(onClose = onClose) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val interactionSource = remember {
                            MutableInteractionSource()
                        }
                        Text(
                            modifier = Modifier.clickable(interactionSource, null) {
                                if (contactData != null) showContactScreen = true
                            },
                            text = contactData?.displayName ?: address
                        )
                    },
                    navigationIcon = {
                        ClickableIcon(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = R.string.okay
                        ) {
                            onClose.invoke()
                        }
                    }
                )
            }
        ) { pV ->
            Column(
                modifier = Modifier
                    .padding(pV),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = lazyListState
                ) {
                    items(smsList) { smsData ->
                        val state = rememberDismissState(
                            confirmValueChange = {
                                if (it == DismissValue.DismissedToEnd) {
                                    SmsUtil.deleteMessage(context, smsData.id)
                                    smsModel.fetchSmsList(context)
                                    return@rememberDismissState true
                                }
                                return@rememberDismissState false
                            }
                        )
                        val isSender = smsData.type in listOf(
                            Telephony.Sms.MESSAGE_TYPE_DRAFT,
                            Telephony.Sms.MESSAGE_TYPE_SENT,
                            Telephony.Sms.MESSAGE_TYPE_OUTBOX
                        )
                        val defaultCornerRadius = 12.dp
                        val edgedCornerRadius = 3.dp
                        val messageSidePadding = 70.dp

                        SwipeToDismiss(
                            modifier = Modifier.align(
                                if (isSender) Alignment.End else Alignment.Start
                            ),
                            state = state,
                            background = {},
                            dismissContent = {
                                ElevatedCard(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                        .padding(
                                            start = if (isSender) messageSidePadding else 0.dp,
                                            end = if (!isSender) messageSidePadding else 0.dp
                                        )
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(
                                        bottomEnd = if (isSender) edgedCornerRadius else defaultCornerRadius,
                                        bottomStart = if (isSender) defaultCornerRadius else edgedCornerRadius,
                                        topEnd = defaultCornerRadius,
                                        topStart = defaultCornerRadius
                                    )
                                ) {
                                    Text(
                                        modifier = Modifier.padding(10.dp),
                                        text = smsData.body
                                    )
                                }
                            },
                            directions = setOf(DismissDirection.StartToEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 5.dp, bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var text by remember {
                        mutableStateOf(initialText)
                    }
                    val focusRequester = remember {
                        FocusRequester()
                    }

                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        value = text,
                        onValueChange = { text = it },
                        placeholder = {
                            Text(stringResource(R.string.send))
                        }
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    ClickableIcon(
                        icon = Icons.Default.Send,
                        contentDescription = R.string.send
                    ) {
                        if (text.isBlank()) return@ClickableIcon

                        smsModel.sendSms(context, address, text)

                        text = ""
                        focusRequester.freeFocus()
                    }
                }
            }
        }
    }

    if (showContactScreen && contactData != null) {
        SingleContactScreen(contact = contactData) {
            showContactScreen = false
        }
    }
}
