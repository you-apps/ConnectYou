package com.bnyro.contacts.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.net.toUri
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.IntentActionType

object IntentHelper {
    fun launchAction(context: Context, type: IntentActionType, argument: String) {
        runCatching {
            context.startActivity(getLaunchIntent(type, argument))
        }
    }

    fun getLaunchIntent(type: IntentActionType, argument: String): Intent {
        if (type == IntentActionType.CONTACT) {
            val contactUri =
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, argument.toLong())
            return Intent(Intent.ACTION_VIEW, contactUri)
        }

        if (type == IntentActionType.WEBSITE) {
            return Intent(Intent.ACTION_VIEW, argument.toUri())
        }

        val query = when (type) {
            IntentActionType.EMAIL -> argument
            IntentActionType.ADDRESS -> "0,0?q="
            else -> argument.replace("-", "")
        }
        val action = when (type) {
            IntentActionType.DIAL -> Intent.ACTION_DIAL
            IntentActionType.SMS, IntentActionType.ADDRESS -> Intent.ACTION_VIEW
            IntentActionType.EMAIL -> Intent.ACTION_SENDTO
            else -> throw IllegalArgumentException()
        }
        val actionScheme = when (type) {
            IntentActionType.DIAL -> "tel"
            IntentActionType.SMS -> "sms"
            IntentActionType.EMAIL -> "mailto"
            IntentActionType.ADDRESS -> "geo"
            else -> throw IllegalArgumentException()
        }

        return Intent(action).apply {
            data = Uri.fromParts(actionScheme, query, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun shareContactVcf(context: Context, uri: Uri) {
        val target = Intent().apply {
            action = Intent.ACTION_SEND
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = context.contentResolver.getType(uri)
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        val chooser = Intent.createChooser(target, context.getString(R.string.share))
        context.startActivity(chooser)
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
