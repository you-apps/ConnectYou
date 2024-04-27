package com.bnyro.contacts.presentation.screens.dialer.model

import android.app.Application
import android.media.AudioManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.bnyro.contacts.domain.model.CallerInfo
import com.bnyro.contacts.presentation.screens.dialer.model.state.CallState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DialerModel(application: Application) : AndroidViewModel(application) {
    private val audioManager =
        application.applicationContext.getSystemService(AudioManager::class.java)

    var currentMuteState by mutableStateOf(false)
    var currentSpeakerState by mutableStateOf(false)

    var playDtmfTone: (digit: Char) -> Unit = {}
    var acceptCall: () -> Unit = {}
    var cancelCall: () -> Unit = {}

    private val _callState = MutableStateFlow<CallState>(CallState.Disconnected)
    val callState = _callState.asStateFlow()

    private val _callerInfo = MutableStateFlow(CallerInfo())
    val callerInfo = _callerInfo.asStateFlow()


    var dialpadNumber by mutableStateOf("")
        private set

    fun onState(state: CallState) {
        _callState.update {
            state
        }
    }

    fun onCallerInfoUpdate(info: CallerInfo) {
        _callerInfo.update {
            info
        }
    }

    fun onDialpadButtonPress(digit: String) {
        dialpadNumber += digit
        playDtmfTone(digit.first())

    }

    fun toggleMute() {
        audioManager.isMicrophoneMute = !currentMuteState
        currentMuteState = !currentMuteState
    }

    fun toggleSpeakers() {
        audioManager.isSpeakerphoneOn = !currentSpeakerState
        currentSpeakerState = !currentSpeakerState
    }
}
