package com.bnyro.contacts.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bnyro.contacts.data.database.obj.SmsData
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalSmsDao {
    @Query("SELECT * from localSms")
    fun getStream(): Flow<List<SmsData>>

    @Query("DELETE FROM localSms WHERE id = :id")
    suspend fun deleteSms(id: Long)

    @Query("DELETE FROM localSms WHERE threadId = :threadId")
    suspend fun deleteThread(threadId: Long)

    @Insert
    suspend fun createSms(smsData: SmsData): Long

    @Query("SELECT threadId FROM localSms WHERE address = :address LIMIT 1")
    suspend fun getThreadId(address: String): Long?
}
