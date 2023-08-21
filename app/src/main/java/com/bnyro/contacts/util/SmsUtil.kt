package com.bnyro.contacts.util

import android.content.Context
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import com.bnyro.contacts.enums.SmsType
import com.bnyro.contacts.ext.longValue
import com.bnyro.contacts.ext.stringValue
import com.bnyro.contacts.obj.SmsData

object SmsUtil {
    private val contentUri = Telephony.Sms.CONTENT_URI

    fun getSmsList(context: Context): List<SmsData> {
        val cursor = context.contentResolver
            .query(contentUri, null, null, null, null)
            ?: return emptyList()
        if (!cursor.moveToFirst()) return emptyList()

        val smsList = mutableListOf<SmsData>()
        do {
            val id = cursor.longValue(Telephony.Sms._ID) ?: continue
            val threadId = cursor.longValue(Telephony.Sms.THREAD_ID) ?: 0
            val address = cursor.stringValue(Telephony.Sms.ADDRESS).orEmpty()
            val timestamp = cursor.longValue(Telephony.Sms.DATE) ?: 0
            val body = cursor.stringValue(Telephony.Sms.BODY).orEmpty()

            val type = when (cursor.stringValue(Telephony.Sms.TYPE).orEmpty().toInt()) {
                Telephony.Sms.MESSAGE_TYPE_INBOX -> SmsType.INBOX
                Telephony.Sms.MESSAGE_TYPE_SENT -> SmsType.SENT
                Telephony.Sms.MESSAGE_TYPE_DRAFT -> SmsType.DRAFT
                else -> SmsType.DRAFT
            }

            smsList.add(SmsData(id, address, body, timestamp, threadId, type))
        } while (cursor.moveToNext())

        cursor.close()

        return smsList
    }

    private fun getSmsManager(context: Context): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }

    fun sendSms(context: Context, recipient: String, body: String) {
        getSmsManager(context)
            .sendTextMessage(recipient, null, body, null, null)
    }

    fun deleteMessage(context: Context, id: Long) {
        val uri = contentUri.buildUpon().appendPath(id.toString()).build()
        context.contentResolver.delete(uri, null, null)
    }

    fun deleteThread(context: Context, threadId: Long) {
        val cursor = context.contentResolver
            .query(contentUri, arrayOf(Telephony.Sms._ID), null, null, null)
            ?: return

        while (cursor.moveToNext()) {
            val id = cursor.longValue(Telephony.Sms._ID) ?: continue
            deleteMessage(context, id)
        }

        cursor.close()
    }
}