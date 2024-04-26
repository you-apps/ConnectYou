package com.bnyro.contacts.presentation.screens.calllog

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.CallLogEntry
import com.bnyro.contacts.presentation.components.NothingHere
import com.bnyro.contacts.presentation.components.NumberInput
import com.bnyro.contacts.presentation.components.PhoneNumberDisplay
import com.bnyro.contacts.presentation.features.ConfirmationDialog
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.dialer.model.DialerModel
import com.bnyro.contacts.presentation.screens.editor.components.ContactIconPlaceholder
import com.bnyro.contacts.presentation.screens.settings.model.ThemeModel
import com.bnyro.contacts.util.CallLogHelper
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.SmsUtil
import com.bnyro.contacts.util.extension.removeLastChar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogsScreen(
    contactsModel: ContactsModel,
    dialerModel: DialerModel,
    themeModel: ThemeModel
) {
    val context = LocalContext.current

    var showNumberPicker by remember {
        mutableStateOf(false)
    }
    var numberToCall by remember {
        mutableStateOf(dialerModel.initialPhoneNumber.orEmpty())
    }
    var callLog by remember {
        mutableStateOf(emptyList<CallLogEntry>())
    }
    val subscriptions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SmsUtil.getSubscriptions(context)
        } else {
            null
        }
    }

    var chosenSubInfo = remember {
        subscriptions?.firstOrNull()
    }

    fun callNumber(number: String) {
        if (!PermissionHelper.checkPermissions(context, DialerModel.phonePerms)) return

        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                chosenSubInfo?.let {
                    val phoneAccountHandle = PhoneAccountHandle(
                        ComponentName(
                            "com.android.phone",
                            "com.android.services.telephony.TelephonyConnectionService"
                        ),
                        it.subscriptionId.toString()
                    )
                    putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
                }
            }
        }
        context.startActivity(intent)
    }

    LaunchedEffect(Unit) {
        callLog = CallLogHelper.getCallLog(context)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNumberPicker = true }
            ) {
                Icon(Icons.Default.Dialpad, contentDescription = null)
            }
        }
    ) { pV ->
        if (callLog.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.padding(pV)
            ) {
                items(callLog) {
                    val contact = remember {
                        contactsModel.getContactByNumber(it.phoneNumber)
                    }
                    var showCallDialog by remember {
                        mutableStateOf(false)
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .clickable { if (it.phoneNumber.isNotBlank()) showCallDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ContactIconPlaceholder(
                                themeModel = themeModel,
                                firstChar = contact?.displayName?.firstOrNull()
                            )

                            Spacer(modifier = Modifier.width(20.dp))

                            Column {
                                Text(text = contact?.displayName ?: it.phoneNumber, maxLines = 1)
                                Text(
                                    text = DateUtils.getRelativeTimeSpanString(it.time)?.toString()
                                        .orEmpty()
                                )
                            }
                        }
                    }

                    if (showCallDialog) {
                        ConfirmationDialog(
                            onDismissRequest = { showCallDialog = false },
                            title = stringResource(id = R.string.dial),
                            text = stringResource(
                                id = R.string.confirm_start_call,
                                contact?.displayName ?: it.phoneNumber
                            )
                        ) {
                            callNumber(it.phoneNumber)
                        }
                    }
                }
            }
        } else {
            NothingHere()
        }
    }

    if (showNumberPicker) {
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { showNumberPicker = false }, sheetState = state) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PhoneNumberDisplay(displayText = numberToCall)
                NumberInput(
                    onNumberInput = {
                        numberToCall += it
                    },
                    onDelete = {
                        numberToCall = numberToCall.removeLastChar()
                    },
                    onDial = {
                        callNumber(numberToCall)
                    },
                    subscriptions = subscriptions,
                    onSubscriptionIndexChange = {
                        chosenSubInfo = subscriptions?.get(it)
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        dialerModel.requestDefaultDialerApp(context)
    }
}
