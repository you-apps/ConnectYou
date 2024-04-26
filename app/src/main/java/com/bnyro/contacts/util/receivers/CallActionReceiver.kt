package com.bnyro.contacts.util.receivers;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bnyro.contacts.util.CallManager

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACCEPT_CALL -> {
                CallManager.acceptCall()
            }

            DECLINE_CALL -> CallManager.cancelCall()
        }
    }

    companion object {
        const val ACCEPT_CALL = "com.bnyro.contacts.accept_call"
        const val DECLINE_CALL = "com.bnyro.contacts.decline_call"
    }
}