package com.bnyro.contacts.presentation.screens.dialer.model.state

import androidx.annotation.StringRes
import com.bnyro.contacts.R

sealed class CallState(@StringRes val text: Int) {
    object Disconnected : CallState(R.string.call_ended)
    object Incoming : CallState(R.string.ringing)
    object Outgoing : CallState(R.string.dialing)
    object InCall : CallState(R.string.in_progress)
    object Connecting : CallState(R.string.connecting)
    object Disconnecting : CallState(R.string.disconnecting)
}