package com.bnyro.contacts.domain.repositories

import android.Manifest
import android.content.Context
import android.provider.CallLog
import com.bnyro.contacts.domain.model.CallLogEntry
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.extension.intValue
import com.bnyro.contacts.util.extension.longValue
import com.bnyro.contacts.util.extension.stringValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallLogRepository(private val context: Context) {
    suspend fun getCallLog(): List<CallLogEntry> = withContext(Dispatchers.IO) {
        if (!PermissionHelper.hasPermission(context, Manifest.permission.READ_CALL_LOG))
            return@withContext emptyList()

        val callLog = mutableListOf<CallLogEntry>()

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null, null, null, CallLog.Calls.DATE + " DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val phoneNumber = cursor.stringValue(CallLog.Calls.NUMBER)
                val callType = cursor.intValue(CallLog.Calls.TYPE)!!
                val callDate = cursor.longValue(CallLog.Calls.DATE)!!
                val callDuration = cursor.longValue(CallLog.Calls.DURATION)!!
                val subscriptionId = cursor.stringValue(CallLog.Calls.PHONE_ACCOUNT_ID)
                callLog.add(
                    CallLogEntry(
                        phoneNumber.orEmpty(),
                        callType,
                        callDate,
                        callDuration,
                        subscriptionId
                    )
                )
            }
        }

        return@withContext callLog
    }

    suspend fun deleteAll(callLog: List<CallLogEntry>) = withContext(Dispatchers.IO) {
        if (!PermissionHelper.hasPermission(context, Manifest.permission.WRITE_CALL_LOG))
            return@withContext

        callLog.distinctBy { it.phoneNumber }.forEach { entry ->
            context.contentResolver.delete(
                CallLog.Calls.CONTENT_URI,
                "NUMBER=${entry.phoneNumber}",
                null
            )
        }
    }
}