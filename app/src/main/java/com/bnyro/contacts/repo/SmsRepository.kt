package com.bnyro.contacts.repo

import android.content.Context
import com.bnyro.contacts.db.obj.SmsData

interface SmsRepository {
    suspend fun getSmsList(context: Context): List<SmsData>
    suspend fun persistSms(context: Context, smsData: SmsData): SmsData
    suspend fun getOrCreateThreadId(context: Context, address: String): Long
    suspend fun deleteSms(context: Context, id: Long)
    suspend fun deleteThread(context: Context, threadId: Long)
}