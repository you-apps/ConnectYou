package com.bnyro.contacts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!listOf(SMS_RECEIVED, SMS_DELIVER).contains(intent.action)) return

        Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach { message ->
            Log.e("body", message.displayMessageBody.toString())
            Log.e("address", message.displayOriginatingAddress.toString())
        }
    }

    companion object {
        private const val SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER"
        private const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
    }
}