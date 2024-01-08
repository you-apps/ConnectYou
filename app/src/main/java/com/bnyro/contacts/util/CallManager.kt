package com.bnyro.contacts.util

import android.telecom.Call

object CallManager {
    private var currentCall: Call? = null

    var callerDisplayNumber = ""
    var currentCallState: Int = Call.STATE_RINGING
    var onStateChangedListeners: MutableList<(Int) -> Unit> = mutableListOf()

    fun setCall(call: Call?) {
        currentCall = call

        if (call == null) return

        callerDisplayNumber = call.details.gatewayInfo?.originalAddress?.schemeSpecificPart
            ?: call.details.handle.schemeSpecificPart
    }

    fun updateCallState(state: Int) {
        this.currentCallState = state
        onStateChangedListeners.forEach {
            it.invoke(state)
        }
    }

    fun cancelCall() {
        if (currentCall == null) return

        if (currentCallState == Call.STATE_RINGING) {
            rejectCall()
        } else {
            disconnectCall()
        }
    }

    fun acceptCall() {
        currentCall?.let { it.answer(it.details.videoState) }
    }

    private fun rejectCall() {
        currentCall?.reject(false, "")
    }

    private fun disconnectCall() {
        currentCall?.disconnect()
    }
}