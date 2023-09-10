package com.bnyro.contacts.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import com.bnyro.contacts.R
import com.bnyro.contacts.ext.intValue
import com.bnyro.contacts.ext.longValue
import com.bnyro.contacts.ext.stringValue
import com.bnyro.contacts.obj.SmsData
import java.lang.Character.UnicodeBlock
import java.util.Calendar
import kotlin.random.Random

object SmsUtil {
    private val contentUri = Telephony.Sms.CONTENT_URI
    private const val MAX_CHAR_LIMIT = 160
    private const val MAX_CHAR_LIMIT_WITH_UNICODE = 70

    fun getSmsList(context: Context): List<SmsData> {
        context.contentResolver
            .query(contentUri, null, null, null, null)
            ?.use { cursor ->
                if (!cursor.moveToFirst()) return emptyList()

                val smsList = mutableListOf<SmsData>()
                do {
                    val id = cursor.longValue(Telephony.Sms._ID) ?: continue
                    val threadId = cursor.longValue(Telephony.Sms.THREAD_ID) ?: 0
                    val address = cursor.stringValue(Telephony.Sms.ADDRESS).orEmpty()
                    val timestamp = cursor.longValue(Telephony.Sms.DATE) ?: 0
                    val body = cursor.stringValue(Telephony.Sms.BODY).orEmpty()
                    val type = cursor.intValue(Telephony.Sms.TYPE) ?: 0

                    smsList.add(SmsData(id, address, body, timestamp, threadId, type))
                } while (cursor.moveToNext())

                return smsList
            }

        return emptyList()
    }

    private fun getSmsManager(context: Context): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }

    fun sendSms(context: Context, address: String, body: String): SmsData? {
        if (!ConnectionHelper.hasSignalForSms(context)) {
            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_LONG).show()
            return null
        }

        getSmsManager(context)
            .sendTextMessage(address, null, body, null, null)

        val timestamp = Calendar.getInstance().timeInMillis
        val threadId = getOrCreateThreadId(context, address)

        val smsData = SmsData(-1, address, body, timestamp, threadId, Telephony.Sms.MESSAGE_TYPE_SENT)
        persistMessage(context, smsData)

        return smsData
    }

    fun deleteMessage(context: Context, id: Long) {
        val uri = contentUri.buildUpon().appendPath(id.toString()).build()
        context.contentResolver.delete(uri, null, null)
    }

    fun deleteThread(context: Context, threadId: Long) {
        context.contentResolver.query(
            contentUri,
            arrayOf(Telephony.Sms._ID),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.longValue(Telephony.Sms._ID) ?: continue
                deleteMessage(context, id)
            }
        }
    }

    fun getOrCreateThreadId(context: Context, address: String): Long {
        context.contentResolver
            .query(
                contentUri,
                arrayOf(Telephony.Sms.THREAD_ID),
                "${Telephony.Sms.ADDRESS} = ?",
                arrayOf(address),
                null
            )
            ?.use {
                if (it.moveToFirst()) {
                    return it.longValue(Telephony.Sms.THREAD_ID) ?: return@use
                }
            }

        return Random.nextLong()
    }

    fun persistMessage(context: Context, smsData: SmsData): SmsData {
        val values = ContentValues()
        values.put(Telephony.Sms.ADDRESS, smsData.address)
        values.put(Telephony.Sms.BODY, smsData.body)
        values.put(Telephony.Sms.DATE, smsData.timestamp)
        values.put(Telephony.Sms.READ, 1)
        values.put(Telephony.Sms.TYPE, smsData.type)
        values.put(Telephony.Sms.THREAD_ID, smsData.threadId)

        val messageUri = context.contentResolver.insert(contentUri, values) ?: return smsData

        Log.v("send_transaction", "inserted to uri: $messageUri")

        context.contentResolver.query(messageUri, arrayOf(Telephony.Sms._ID), null, null, null)?.use {
            if (it.moveToFirst()) smsData.id = it.longValue(Telephony.Sms._ID)!!
        }
        return smsData
    }

    fun isShortEnoughForSms(text: String): Boolean {
        if (text.length > MAX_CHAR_LIMIT) return false

        // text messages containing one or more unicode chars are limited to 70 characters
        if (text.any { c -> UnicodeBlock.of(c) != UnicodeBlock.BASIC_LATIN }) {
            return text.length < MAX_CHAR_LIMIT_WITH_UNICODE
        }

        return true
    }
}