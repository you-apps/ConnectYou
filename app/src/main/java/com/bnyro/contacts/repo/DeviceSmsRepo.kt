package com.bnyro.contacts.repo

import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import android.util.Log
import com.bnyro.contacts.db.obj.SmsData
import com.bnyro.contacts.ext.intValue
import com.bnyro.contacts.ext.longValue
import com.bnyro.contacts.ext.stringValue
import com.bnyro.contacts.util.SmsUtil
import kotlin.random.Random

class DeviceSmsRepo : SmsRepository {
    private val contentUri = Telephony.Sms.CONTENT_URI

    override suspend fun getSmsList(context: Context): List<SmsData> {
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

    override suspend fun persistSms(context: Context, smsData: SmsData): SmsData {
        val values = ContentValues()
        values.put(Telephony.Sms.ADDRESS, smsData.address)
        values.put(Telephony.Sms.BODY, smsData.body)
        values.put(Telephony.Sms.DATE, smsData.timestamp)
        values.put(Telephony.Sms.READ, 1)
        values.put(Telephony.Sms.TYPE, smsData.type)
        values.put(Telephony.Sms.THREAD_ID, smsData.threadId)

        val messageUri = context.contentResolver.insert(contentUri, values) ?: return smsData

        Log.v("send_transaction", "inserted to uri: $messageUri")

        context.contentResolver.query(
            messageUri,
            arrayOf(Telephony.Sms._ID),
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) smsData.id = it.longValue(Telephony.Sms._ID)!!
        }
        return smsData
    }

    override suspend fun deleteSms(context: Context, id: Long) {
        val uri = contentUri.buildUpon().appendPath(id.toString()).build()
        context.contentResolver.delete(uri, null, null)
    }

    override suspend fun deleteThread(context: Context, threadId: Long) {
        context.contentResolver.query(
            contentUri,
            arrayOf(Telephony.Sms._ID),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.longValue(Telephony.Sms._ID) ?: continue
                SmsUtil.deleteMessage(context, id)
            }
        }
    }

    override suspend fun getOrCreateThreadId(context: Context, address: String): Long {
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
}
