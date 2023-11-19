package com.bnyro.contacts.ui.screens

import android.telecom.Call
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.R
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.DialerModel
import com.bnyro.contacts.util.CallManager
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

@Composable
fun DialerScreen(
    contactsModel: ContactsModel,
    dialerModel: DialerModel,
    onClose: () -> Unit
) {
    val contactInfo = remember {
        contactsModel.getContactByNumber(CallManager.callerDisplayNumber)
    }
    var callState by remember {
        mutableIntStateOf(CallManager.currentCallState)
    }
    var elapsedTime by remember {
        mutableLongStateOf(0L)
    }
    val timer = Timer()

    DisposableEffect(Unit) {
        val listener: (Int) -> Unit = {
            callState = it

            if (callState == Call.STATE_DISCONNECTED) onClose.invoke()

            if (callState == Call.STATE_ACTIVE) {
                timer.cancel()
                timer.scheduleAtFixedRate(1000, 1000) {
                    elapsedTime++
                }
            }
        }

        CallManager.onStateChangedListeners.add(listener)

        onDispose {
            CallManager.onStateChangedListeners.remove(listener)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = when (callState) {
                Call.STATE_RINGING, Call.STATE_DIALING, Call.STATE_PULLING_CALL -> stringResource(R.string.ringing)
                Call.STATE_DISCONNECTING -> stringResource(R.string.disconnecting)
                Call.STATE_ACTIVE -> stringResource(R.string.in_progress)
                Call.STATE_CONNECTING -> stringResource(R.string.connecting)
                else -> ""
            },
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = contactInfo?.displayName ?: CallManager.callerDisplayNumber,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(text = DateUtils.formatElapsedTime(elapsedTime))
        Spacer(modifier = Modifier.weight(1f))
        Row {
            if (callState == Call.STATE_RINGING) {
                ExtendedFloatingActionButton(
                    onClick = { CallManager.acceptCall() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                }

                Spacer(modifier = Modifier.width(20.dp))
            }

            ExtendedFloatingActionButton(
                onClick = { CallManager.cancelCall() },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}
