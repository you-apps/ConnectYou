package com.bnyro.contacts.presentation.screens.dialer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bnyro.contacts.R
import com.bnyro.contacts.presentation.components.DialerButton
import com.bnyro.contacts.presentation.screens.calllog.components.NumberInput
import com.bnyro.contacts.presentation.screens.calllog.components.PhoneNumberOnlyDisplay
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
    callerPhoto: Any? = null,
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            if (callerPhoto == null) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit,
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
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

        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = callStateText,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = callerName ?: callerNumber,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp
        )
        if (callerName != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = callerNumber,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
        ) {
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
                    hint = stringResource(R.string.key_pad)
                ) {
                    showDialPad = true
                }
            }

        }
        FloatingActionButton(
            onClick = onCancelCall,
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            shape = RoundedCornerShape(50),
            elevation = FloatingActionButtonDefaults.elevation(
                0.dp, 0.dp, 0.dp, 0.dp
            )
        ) {
            Icon(Icons.Default.CallEnd, contentDescription = null)
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
    callerPhoto: Any? = null,
    onAcceptCall: () -> Unit,
    onDeclineCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = stringResource(R.string.call_from),
            color = MaterialTheme.colorScheme.outline,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .size(250.dp)
                .background(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            if (callerPhoto == null) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit,
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
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
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = callerName ?: callerNumber,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp
        )
        if (callerName != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = callerNumber,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            FloatingActionButton(
                onClick = onAcceptCall,
                containerColor = Color(0xFF348540),
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                elevation = FloatingActionButtonDefaults.elevation(
                    0.dp, 0.dp, 0.dp, 0.dp
                )
            ) {
                Icon(Icons.Default.Call, contentDescription = null)
            }

            FloatingActionButton(
                onClick = onDeclineCall,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                shape = RoundedCornerShape(50),
                elevation = FloatingActionButtonDefaults.elevation(
                    0.dp, 0.dp, 0.dp, 0.dp
                )
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Preview
@Composable
private fun InCallScreenPreview() {
    InCallScreen(
        callStateText = "Connecting..",
        callerNumber = "1234567890",
        callerName = "Test Caller",
        callerPhoto = null,
        muteState = false,
        speakerState = false,
        onToggleMute = {},
        onToggleSpeaker = {},
        onCancelCall = {},
        dialpadNumber = "",
        onDialpadButtonPress = {}
    )
}

@Preview
@Composable
private fun CallAlertScreenPreview() {
    CallAlertScreen(
        callerNumber = "1234567890",
        callerName = "Test Caller",
        callerPhoto = null,
        onAcceptCall = {},
        onDeclineCall = {}
    )

}