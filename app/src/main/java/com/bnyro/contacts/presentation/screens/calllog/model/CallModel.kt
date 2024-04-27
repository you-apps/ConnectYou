package com.bnyro.contacts.presentation.screens.calllog.model

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bnyro.contacts.App
import com.bnyro.contacts.domain.model.CallLogEntry
import com.bnyro.contacts.navigation.HomeRoutes
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.SmsUtil
import com.bnyro.contacts.util.extension.removeLastChar
import kotlinx.coroutines.launch

class CallModel(private val application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {
    val callLogRepository = (application as App).callLogRepository

    val initialPhoneNumber = savedStateHandle.get<String>(HomeRoutes.Phone.phoneNumber)

    var numberToCall: String by mutableStateOf(initialPhoneNumber.orEmpty())
        private set

    var callLogs by mutableStateOf<List<CallLogEntry>>(emptyList(), policy = neverEqualPolicy())
        private set

    private val toneGenerator = ToneGenerator(
        AudioManager.STREAM_SYSTEM,
        100
    )

    val subscriptions =
        SmsUtil.getSubscriptions(application.applicationContext)

    var chosenSubInfo =
        subscriptions.firstOrNull()


    init {
        viewModelScope.launch {
            callLogs = callLogRepository.getCallLog()
        }
    }

    fun onSubscriptionIndexChange(index: Int) {
        chosenSubInfo = subscriptions.get(index)
    }

    fun onNumberInput(digit: String) {
        numberToCall += digit
        playToneForDigit(digit)
    }

    fun onBackSpace() {
        numberToCall = numberToCall.removeLastChar()
    }

    fun callNumber(number: String = numberToCall) {
        if (!PermissionHelper.checkPermissions(application.applicationContext, phonePerms)) return

        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
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
        application.applicationContext.startActivity(intent)
    }

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

    fun playToneForDigit(digit: String) {
        val numericDigit = when (digit) {
            "0" -> ToneGenerator.TONE_DTMF_0
            "1" -> ToneGenerator.TONE_DTMF_1
            "2" -> ToneGenerator.TONE_DTMF_2
            "3" -> ToneGenerator.TONE_DTMF_3
            "4" -> ToneGenerator.TONE_DTMF_4
            "5" -> ToneGenerator.TONE_DTMF_5
            "6" -> ToneGenerator.TONE_DTMF_6
            "7" -> ToneGenerator.TONE_DTMF_7
            "8" -> ToneGenerator.TONE_DTMF_8
            "9" -> ToneGenerator.TONE_DTMF_9
            "#" -> ToneGenerator.TONE_DTMF_P
            "*" -> ToneGenerator.TONE_DTMF_S
            else -> error("Invalid digit: $digit")
        }

        val durationMs = 100
        toneGenerator.stopTone()
        toneGenerator.startTone(numericDigit, durationMs)
    }

    companion object {
        val phonePerms = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
        )
    }
}