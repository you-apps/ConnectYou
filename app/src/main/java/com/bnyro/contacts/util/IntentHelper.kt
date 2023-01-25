package com.bnyro.contacts.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.bnyro.contacts.enums.IntentActionType

object IntentHelper {
    fun launchAction(context: Context, type: IntentActionType, number: String) {
        val num = number.replace("-", "")
        val action = when (type) {
            IntentActionType.DIAL -> Intent.ACTION_DIAL
            IntentActionType.SMS -> Intent.ACTION_VIEW
        }
        val actionScheme = when (type) {
            IntentActionType.DIAL -> "tel"
            IntentActionType.SMS -> "sms"
        }

        val intent = Intent(action).apply {
            data = Uri.fromParts(actionScheme, num, null)
        }
        context.startActivity(intent)
    }
}
