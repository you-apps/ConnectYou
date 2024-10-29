package com.bnyro.contacts.presentation.screens.sms

import android.annotation.SuppressLint
import android.os.Build
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.presentation.components.ClickableIcon
import com.bnyro.contacts.presentation.components.ElevatedTextInputField
import com.bnyro.contacts.presentation.features.AddToContactDialog
import com.bnyro.contacts.presentation.features.ConfirmationDialog
import com.bnyro.contacts.presentation.screens.contact.SingleContactScreen
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.sms.components.Messages
import com.bnyro.contacts.presentation.screens.sms.model.SmsModel
import com.bnyro.contacts.util.SmsUtil

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsThreadScreen(
    smsModel: SmsModel,
    contactsModel: ContactsModel,
    address: String,
    initialText: String = "",
    contactsData: ContactData? = null,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    val allSmsList by smsModel.smsList.collectAsState()
    val smsList = allSmsList.filter { it.address == address }
    val subscriptions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SmsUtil.getSubscriptions(context)
        } else {
            null
        }
    }

    val contactData = remember {
        contactsData ?: contactsModel.getContactByNumber(address)
    }

    var showContactScreen by remember {
        mutableStateOf(false)
    }
    var showAddToContactDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val interactionSource = remember {
                        MutableInteractionSource()
                    }
                    TooltipBox(
                        tooltip = {
                            PlainTooltip {
                                Text(address)
                            }
                        },
                        state = rememberTooltipState(),
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()
                    ) {
                        Text(
                            modifier = Modifier
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
                },
                actions = {
                    if (contactData == null) {
                        ClickableIcon(icon = Icons.Default.PersonAddAlt1) {
                            showAddToContactDialog = true
                        }
                    }
                }
            )
        }
    ) { pV ->
        Column(
            modifier = Modifier
                .padding(pV)
        ) {
            Messages(messages = smsList, smsModel = smsModel)

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

            // can't respond to address short codes that don't include a number
            val showSendButton = remember {
                address.any { !it.isLetter() }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 5.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showConfirmSendMultipleSmsDialog by remember {
                    mutableStateOf(false)
                }

                var text by remember {
                    mutableStateOf(initialText)
                }

                ElevatedTextInputField(
                    modifier = Modifier.weight(1f),
                    query = text,
                    onQueryChange = { text = it },
                    placeholder = stringResource(if (showSendButton) R.string.send else R.string.cant_respond),
                    enabled = showSendButton
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    modifier = Modifier.size(48.dp),
                    enabled = showSendButton,
                    onClick = {
                        if (text.isBlank()) return@FilledIconButton
                        if (!SmsUtil.isShortEnoughForSms(text)) {
                            showConfirmSendMultipleSmsDialog = true
                            return@FilledIconButton
                        }

                        smsModel.sendSms(context, address, text)

                        text = ""
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = stringResource(R.string.send)
                    )
                }

                if (showConfirmSendMultipleSmsDialog) {
                    ConfirmationDialog(
                        onDismissRequest = { showConfirmSendMultipleSmsDialog = false },
                        title = stringResource(R.string.message_too_long),
                        text = stringResource(R.string.send_message_as_multiple)
                    ) {
                        SmsUtil.splitSmsText(text).forEach {
                            smsModel.sendSms(context, address, it)
                        }

                        text = ""
                    }
                }
            }
        }
    }

    if (showContactScreen && contactData != null) {
        SingleContactScreen(contact = contactData, contactsModel) {
            showContactScreen = false
        }
    }

    if (showAddToContactDialog) {
        AddToContactDialog(newNumber = address) {
            showAddToContactDialog = false
        }
    }
}
