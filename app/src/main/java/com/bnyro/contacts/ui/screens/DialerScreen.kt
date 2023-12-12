package com.bnyro.contacts.ui.screens

import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.R
import com.bnyro.contacts.ui.components.DialerButton
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.DialerModel
import com.bnyro.contacts.util.CallManager

@Composable
fun DialerScreen(
    contactsModel: ContactsModel,
    dialerModel: DialerModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    val contactInfo = remember {
        contactsModel.getContactByNumber(CallManager.callerDisplayNumber)
    }
    var callState by remember {
        mutableIntStateOf(CallManager.currentCallState)
    }
    var elapsedTime by remember {
        mutableLongStateOf(0L)
    }
    val handler = remember { Handler(Looper.getMainLooper()) }

    fun updateTime() {
        elapsedTime++
        handler.postDelayed(::updateTime, 1000L)
    }

    DisposableEffect(Unit) {
        val listener: (Int) -> Unit = {
            callState = it

            if (callState == Call.STATE_DISCONNECTED) {
                handler.removeCallbacks(::updateTime)
                onClose.invoke()
            }

            if (callState == Call.STATE_ACTIVE) {
                updateTime()
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
                Call.STATE_RINGING, Call.STATE_DIALING, Call.STATE_PULLING_CALL -> stringResource(
                    R.string.ringing
                )

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
        LazyHorizontalGrid(rows = GridCells.Fixed(3)) {
            item {
                DialerButton(
                    isEnabled = dialerModel.currentMuteState,
                    icon = Icons.Default.MicOff,
                    hint = stringResource(R.string.mute)
                ) {
                    dialerModel.toggleMute(context)
                }
            }
            item {
                DialerButton(
                    isEnabled = dialerModel.currentSpeakerState,
                    icon = Icons.Default.VolumeUp,
                    hint = stringResource(R.string.speakers)
                ) {
                    dialerModel.toggleSpeakers(context)
                }
            }
        }
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
