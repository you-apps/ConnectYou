package com.bnyro.contacts.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import android.provider.Telephony
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.components.dialogs.ConfirmationDialog
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.util.SmsUtil

@SuppressLint("NewApi")
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

    val threadId = smsModel.smsList.firstOrNull { it.address == address }?.threadId
    val smsList =
        threadId?.let { smsModel.smsGroups[threadId]?.sortedBy { it.timestamp } } ?: listOf()
    val lazyListState = rememberLazyListState()
    var showContactScreen by remember {
        mutableStateOf(false)
    }
    val subscriptions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SmsUtil.getSubscriptions(context)
        } else {
            null
        }
    }

    FullScreenDialog(onClose = onClose) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val interactionSource = remember {
                            MutableInteractionSource()
                        }
                        PlainTooltipBox(
                            tooltip = { Text(address) }
                        ) {
                            Text(
                                modifier = Modifier
                                    .tooltipTrigger()
                                    .clickable(interactionSource, null) {
                                        if (contactData != null) showContactScreen = true
                                    },
                                text = contactData?.displayName ?: address
                            )
                        }
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
                    .padding(pV)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = lazyListState
                ) {
                    items(smsList) { smsData ->
                        var showDeleteSmsDialog by remember {
                            mutableStateOf(false)
                        }

                        val state = rememberDismissState(
                            confirmValueChange = {
                                if (it == DismissValue.DismissedToEnd) {
                                    showDeleteSmsDialog = true
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

                        val messageAlignment = if (isSender) Alignment.End else Alignment.Start
                        SwipeToDismiss(
                            modifier = Modifier.align(messageAlignment),
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
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(
                                        defaultElevation = if (isSender) 20.dp else 5.dp
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        SelectionContainer {
                                            val uriHandler = LocalUriHandler.current
                                            ClickableText(
                                                text = smsData.formatted,
                                                style = TextStyle.Default.copy(
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    fontSize = 15.sp
                                                ),
                                                onClick = { offset ->
                                                    val annotation =
                                                        smsData.formatted.getStringAnnotations(
                                                            offset,
                                                            offset
                                                        ).firstOrNull()
                                                    annotation?.let {
                                                        uriHandler.openUri(it.item)
                                                    }
                                                }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            modifier = Modifier.align(messageAlignment),
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            text = DateUtils.getRelativeTimeSpanString(
                                                smsData.timestamp
                                            )
                                                .toString()
                                        )
                                    }
                                }
                            },
                            directions = setOf(DismissDirection.StartToEnd)
                        )

                        if (showDeleteSmsDialog) {
                            ConfirmationDialog(
                                onDismissRequest = { showDeleteSmsDialog = false },
                                title = stringResource(R.string.delete_message),
                                text = stringResource(R.string.irreversible)
                            ) {
                                smsModel.deleteSms(context, smsData.id, smsData.threadId)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                if (subscriptions != null && subscriptions.size >= 2) {
                    var currentSub by remember { mutableIntStateOf(0) }
                    LaunchedEffect(Unit) {
                        currentSub = 0
                        smsModel.currentSubscription = subscriptions[currentSub]
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = {
                            currentSub = if (currentSub == 0) 1 else 0
                            smsModel.currentSubscription = subscriptions[currentSub]
                        }) {
                            Text(
                                text = "SIM ${subscriptions[currentSub].simSlotIndex + 1} - ${subscriptions[currentSub].displayName}"
                            )
                        }
                    }
                }
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

                    SearchBar(
                        modifier = Modifier.weight(1f),
                        query = text,
                        onQueryChange = { text = it },
                        placeholder = {
                            Text(stringResource(R.string.send))
                        },
                        onSearch = {},
                        active = false,
                        onActiveChange = {},
                        content = {}
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = {
                            if (text.isBlank()) return@FilledIconButton
                            if (!SmsUtil.isShortEnoughForSms(text)) {
                                Toast.makeText(
                                    context,
                                    R.string.message_too_long,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                return@FilledIconButton
                            }

                            smsModel.sendSms(context, address, text)

                            text = ""
                            focusRequester.freeFocus()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = stringResource(R.string.send)
                        )
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
