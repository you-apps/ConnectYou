package com.bnyro.contacts.services

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.bnyro.contacts.ui.activities.CallActivity
import com.bnyro.contacts.util.CallManager

class CallService : InCallService() {
    private val callCallback: Call.Callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            CallManager.updateCallState(state)
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callCallback)
        val intent = Intent(applicationContext, CallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        CallManager.setCall(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callCallback)
        CallManager.setCall(null)
    }
}