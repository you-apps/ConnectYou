package com.bnyro.contacts.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.BlockedNumberContract.BlockedNumbers
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.IntentActionType
import com.bnyro.contacts.domain.enums.ListAttribute
import com.bnyro.contacts.domain.enums.StringAttribute
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.TranslatedType
import com.bnyro.contacts.domain.model.ValueWithType


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

    fun extractContactFromIntent(intent: Intent): ContactData {
        val name = intent.getStringExtra(ContactsContract.Intents.Insert.NAME)
            ?: intent.getStringExtra(ContactsContract.Intents.Insert.PHONETIC_NAME)

        val data = ContactData(
            displayName = name,
            firstName = name?.split(" ")?.firstOrNull(),
            surName = name?.split(" ", limit = 2)?.lastOrNull(),
        )

        ContactsHelper.contactAttributesTypes.forEach { attribute ->
            if (attribute is StringAttribute) {
                attribute.set(data, intent.getStringExtra(attribute.insertKey))
            } else if (attribute is ListAttribute) {
                val values = attribute.insertKeys.mapNotNull { insertKey ->
                    extractIntentValue(intent, insertKey.first, insertKey.second)
                }
                attribute.set(data, values)
            }
        }

        return data
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun openBlockedNumberManager(context: Context) {
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        val intent = telecomManager.createManageBlockedNumbersIntent()
        context.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun blockNumberOrAddress(context: Context, number: String) {
        if (!BlockedNumberContract.canCurrentUserBlockNumbers(context)) {
            Toast.makeText(
                context,
                context.getString(R.string.blocking_number_is_not_supported),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val values = ContentValues().apply {
            put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
        }
        context.contentResolver.insert(BlockedNumbers.CONTENT_URI, values)
        Toast.makeText(
            context,
            context.getString(R.string.number_blocked),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun extractIntentValue(
        intent: Intent,
        key: String,
        typeKey: String? = null,
        types: List<TranslatedType> = emptyList()
    ): ValueWithType? {
        val entry = intent.getStringExtra(key) ?: return null

        val type = if (typeKey != null) {
            val typeIdentifier = intent.getStringExtra(typeKey)

            types.firstOrNull {
                it.vcardType?.value?.uppercase() == typeIdentifier
            }?.id
        } else null

        return ValueWithType(entry, type ?: 0)
    }
}
