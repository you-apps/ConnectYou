package com.bnyro.contacts.presentation.screens.dialer

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bnyro.contacts.R
import com.bnyro.contacts.presentation.components.DialerButton
import com.bnyro.contacts.presentation.components.NumberInput
import com.bnyro.contacts.presentation.components.PhoneNumberOnlyDisplay
import com.bnyro.contacts.presentation.screens.dialer.model.DialerModel
import com.bnyro.contacts.presentation.screens.dialer.model.state.CallState

@Composable
fun DialerScreen(
    dialerModel: DialerModel
) {
    val callState by dialerModel.callState.collectAsState()
    val callerInfo by dialerModel.callerInfo.collectAsState()

    when (val state = callState) {
        CallState.Incoming -> {
            CallAlertScreen(
                callerNumber = callerInfo.formattedPhoneNumber,
                callerName = callerInfo.callerName,
                callerPhoto = callerInfo.callerPhoto,
                onAcceptCall = dialerModel.acceptCall,
                onDeclineCall = dialerModel.cancelCall,
            )
        }

        else -> {
            InCallScreen(
                callStateText = stringResource(id = state.text),
                callerNumber = callerInfo.formattedPhoneNumber,
                callerName = callerInfo.callerName,
                callerPhoto = callerInfo.callerPhoto,
                muteState = dialerModel.currentMuteState,
                speakerState = dialerModel.currentSpeakerState,
                onToggleMute = dialerModel::toggleMute,
                onToggleSpeaker = dialerModel::toggleSpeakers,
                onCancelCall = dialerModel.cancelCall,
                dialpadNumber = dialerModel.dialpadNumber,
                onDialpadButtonPress = dialerModel::onDialpadButtonPress,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun InCallScreen(
    callStateText: String,
    callerNumber: String,
    callerName: String? = null,
    callerPhoto: Uri? = null,
    muteState: Boolean,
    speakerState: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onCancelCall: () -> Unit,
    dialpadNumber: String,
    onDialpadButtonPress: (String) -> Unit,
) {
    var showDialPad by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Box(
            modifier = Modifier
                .size(128.dp)
                .background(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                )
        ) {
            if (callerPhoto == null) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit,
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null
                )
            } else {
                AsyncImage(
                    callerPhoto,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = callStateText,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = callerName ?: callerNumber,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(text = callStateText)
        Spacer(modifier = Modifier.weight(1f))
        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth()) {
            item {
                DialerButton(
                    isEnabled = muteState,
                    icon = Icons.Rounded.MicOff,
                    hint = stringResource(R.string.mute)
                ) {
                    onToggleMute.invoke()
                }
            }
            item {
                DialerButton(
                    isEnabled = speakerState,
                    icon = Icons.AutoMirrored.Rounded.VolumeUp,
                    hint = stringResource(R.string.speakers)
                ) {
                    onToggleSpeaker.invoke()
                }
            }
            item {
                DialerButton(
                    isEnabled = showDialPad,
                    icon = Icons.Rounded.Dialpad,
                    hint = stringResource(R.string.dial_pad)
                ) {
                    showDialPad = true
                }
            }

        }
        Spacer(modifier = Modifier.weight(1f))
        Row {
            ExtendedFloatingActionButton(
                onClick = onCancelCall,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }

    if (showDialPad) {
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { showDialPad = false }, sheetState = state) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PhoneNumberOnlyDisplay(displayText = dialpadNumber)
                NumberInput(
                    onNumberInput = onDialpadButtonPress
                )
            }
        }
    }
}

@Composable
private fun CallAlertScreen(
    callerNumber: String,
    callerName: String? = null,
    callerPhoto: Uri? = null,
    onAcceptCall: () -> Unit,
    onDeclineCall: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = stringResource(R.string.call_from),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp
        )
        if (callerPhoto != null) {
            Spacer(modifier = Modifier.height(10.dp))
            AsyncImage(
                callerPhoto,
                modifier = Modifier
                    .size(256.dp)
                    .clip(CircleShape),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = callerName ?: callerNumber,
            fontSize = 24.sp
        )
        if (callerName != null) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = callerNumber)
        }
        Spacer(modifier = Modifier.weight(1f))
        Row {
            ExtendedFloatingActionButton(
                onClick = onAcceptCall,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Call, contentDescription = null)
            }

            Spacer(modifier = Modifier.width(20.dp))

            ExtendedFloatingActionButton(
                onClick = onDeclineCall,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}