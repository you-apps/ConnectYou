package com.bnyro.contacts.repo

import android.content.Context
import com.bnyro.contacts.db.DatabaseHolder
import com.bnyro.contacts.db.obj.SmsData
import kotlin.random.Random

class LocalSmsRepo : SmsRepository {
    override suspend fun getSmsList(context: Context): List<SmsData> {
        return DatabaseHolder.Db.localSmsDao().getAll()
    }

    override suspend fun persistSms(context: Context, smsData: SmsData): SmsData {
        val id = DatabaseHolder.Db.localSmsDao().createSms(smsData)
        return smsData.copy(id = id)
    }

    override suspend fun getOrCreateThreadId(context: Context, address: String): Long {
        DatabaseHolder.Db.localSmsDao().getSmsByAddress(address).firstOrNull()?.let {
            return it.threadId
        }

        return Random.nextLong()
    }

    override suspend fun deleteSms(context: Context, id: Long) {
        DatabaseHolder.Db.localSmsDao().deleteSms(id)
    }

    override suspend fun deleteThread(context: Context, threadId: Long) {
        DatabaseHolder.Db.localSmsDao().deleteThread(threadId)
    }
}
