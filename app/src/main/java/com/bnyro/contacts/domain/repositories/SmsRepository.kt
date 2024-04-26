package com.bnyro.contacts.domain.repositories

import android.content.Context
import com.bnyro.contacts.data.database.obj.SmsData
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    fun getSmsStream(context: Context): Flow<List<SmsData>>
    suspend fun persistSms(context: Context, smsData: SmsData)
    suspend fun getOrCreateThreadId(context: Context, address: String): Long
    suspend fun deleteSms(context: Context, id: Long)
    suspend fun deleteThread(context: Context, threadId: Long)
}
