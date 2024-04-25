package com.bnyro.contacts.ui.models

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.telecom.TelecomManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class DialerModel : ViewModel() {
    var initialPhoneNumber: String? = null
    var currentMuteState by mutableStateOf(false)
    var currentSpeakerState by mutableStateOf(false)

    var playDtmfTone: (digit: Char) -> Unit = {}

    var dialpadNumber by mutableStateOf("")
        private set

    fun requestDefaultDialerApp(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
        val isAlreadyDefaultDialer =
            context.packageName.equals(telecomManager!!.defaultDialerPackage)
        if (!isAlreadyDefaultDialer) {
            val intent: Intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    context.packageName
                )
            context.startActivity(intent)
        }
    }

    fun onDialpadButtonPress(digit: String) {
        dialpadNumber += digit
        playDtmfTone(digit.first())

    }

    fun toggleMute(context: Context) {
        val audioManager = context.getSystemService(AudioManager::class.java)!!
        audioManager.isMicrophoneMute = !currentMuteState
        currentMuteState = !currentMuteState
    }

    fun toggleSpeakers(context: Context) {
        val audioManager = context.getSystemService(AudioManager::class.java)!!
        audioManager.isSpeakerphoneOn = !currentSpeakerState
        currentSpeakerState = !currentSpeakerState
    }

    companion object {
        val phonePerms = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
        )
    }
}
