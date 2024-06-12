package com.bnyro.contacts.domain.repositories

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import androidx.annotation.RequiresPermission
import com.bnyro.contacts.data.database.obj.SmsData
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.SmsUtil
import com.bnyro.contacts.util.extension.intValue
import com.bnyro.contacts.util.extension.longValue
import com.bnyro.contacts.util.extension.stringValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.random.Random

class DeviceSmsRepo : SmsRepository {
    private val contentUri = Telephony.Sms.CONTENT_URI

    @SuppressLint("MissingPermission")
    override fun getSmsStream(context: Context): Flow<List<SmsData>> {
        return if (PermissionHelper.hasPermission(context, Manifest.permission.READ_SMS)) {
            context.contentResolver.observe(contentUri).map {
                getSmsList(context)
            }
        } else {
            emptyFlow()
        }
    }

    @RequiresPermission(Manifest.permission.READ_SMS)
    private suspend fun getSmsList(context: Context): List<SmsData> = withContext(Dispatchers.IO) {
        val simSlotMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SmsUtil.getSubscriptions(context)
                .associateBy({ it.subscriptionId }, { it.simSlotIndex })
        } else {
            null
        }
        context.contentResolver
            .query(contentUri, null, null, null, "date ASC")
            ?.use { cursor ->
                if (!cursor.moveToFirst()) return@withContext emptyList()

                val smsList = mutableListOf<SmsData>()
                do {
                    val id = cursor.longValue(Telephony.Sms._ID) ?: continue
                    val threadId = cursor.longValue(Telephony.Sms.THREAD_ID) ?: 0
                    val address = cursor.stringValue(Telephony.Sms.ADDRESS).orEmpty()
                    val timestamp = cursor.longValue(Telephony.Sms.DATE) ?: 0
                    val body = cursor.stringValue(Telephony.Sms.BODY).orEmpty()
                    val type = cursor.intValue(Telephony.Sms.TYPE) ?: 0
                    val simIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        cursor.intValue(Telephony.Sms.SUBSCRIPTION_ID)?.takeIf { it > 0 }?.let {
                            simSlotMap?.get(it)
                        }?.plus(1)
                    } else {
                        null
                    }

                    smsList.add(SmsData(id, address, body, timestamp, threadId, type, simIndex))
                } while (cursor.moveToNext())

                return@withContext smsList
            }

        return@withContext emptyList()
    }

    override suspend fun persistSms(context: Context, smsData: SmsData) {
        val values = ContentValues()
        values.put(Telephony.Sms.ADDRESS, ContactsHelper.normalizePhoneNumber(smsData.address))
        values.put(Telephony.Sms.BODY, smsData.body)
        values.put(Telephony.Sms.DATE, smsData.timestamp)
        values.put(Telephony.Sms.READ, 1)
        values.put(Telephony.Sms.TYPE, smsData.type)
        values.put(Telephony.Sms.THREAD_ID, smsData.threadId)

        context.contentResolver.insert(contentUri, values)
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
                deleteSms(context, id)
            }
        }
    }

    override suspend fun getOrCreateThreadId(context: Context, address: String): Long {
        context.contentResolver
            .query(
                contentUri,
                arrayOf(Telephony.Sms.THREAD_ID),
                "${Telephony.Sms.ADDRESS} = ?",
                arrayOf(ContactsHelper.normalizePhoneNumber(address)),
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

fun ContentResolver.observe(uri: Uri) = callbackFlow {
    val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            trySend(selfChange)
        }
    }
    registerContentObserver(uri, true, observer)
    // trigger first.
    trySend(false)
    awaitClose {
        unregisterContentObserver(observer)
    }
}
