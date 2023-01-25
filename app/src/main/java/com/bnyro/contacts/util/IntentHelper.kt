package com.bnyro.contacts.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.bnyro.contacts.enums.IntentActionType

object IntentHelper {
    fun launchAction(context: Context, type: IntentActionType, argument: String) {
        val query = when (type) {
            IntentActionType.EMAIL -> argument
            else -> argument.replace("-", "")
        }
        val action = when (type) {
            IntentActionType.DIAL -> Intent.ACTION_DIAL
            IntentActionType.SMS -> Intent.ACTION_VIEW
            IntentActionType.EMAIL -> Intent.ACTION_SENDTO
        }
        val actionScheme = when (type) {
            IntentActionType.DIAL -> "tel"
            IntentActionType.SMS -> "sms"
            IntentActionType.EMAIL -> "mailto"
        }

        val intent = Intent(action).apply {
            data = Uri.fromParts(actionScheme, query, null)
        }
        context.startActivity(intent)
    }
}
