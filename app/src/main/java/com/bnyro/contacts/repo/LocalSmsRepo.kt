package com.bnyro.contacts.repo

import android.content.Context
import com.bnyro.contacts.db.DatabaseHolder
import com.bnyro.contacts.db.obj.SmsData
import com.bnyro.contacts.util.ContactsHelper
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class LocalSmsRepo : SmsRepository {
    override fun getSmsStream(context: Context): Flow<List<SmsData>> =
        DatabaseHolder.Db.localSmsDao().getStream()

    override suspend fun persistSms(context: Context, smsData: SmsData) {
        DatabaseHolder.Db.localSmsDao().createSms(smsData)
    }

    override suspend fun getOrCreateThreadId(context: Context, address: String): Long {
        return DatabaseHolder.Db.localSmsDao()
            .getSmsByAddress(ContactsHelper.normalizePhoneNumber(address))
            .firstOrNull()
            ?.threadId
            ?: Random.nextLong()
    }

    override suspend fun deleteSms(context: Context, id: Long) {
        DatabaseHolder.Db.localSmsDao().deleteSms(id)
    }

    override suspend fun deleteThread(context: Context, threadId: Long) {
        DatabaseHolder.Db.localSmsDao().deleteThread(threadId)
    }
}
