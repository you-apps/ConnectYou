package com.bnyro.contacts.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bnyro.contacts.db.obj.SmsData

@Dao
interface LocalSmsDao {
    @Query("SELECT * from localSms")
    suspend fun getAll(): List<SmsData>

    @Query("DELETE FROM localSms WHERE id = :id")
    suspend fun deleteSms(id: Long)

    @Query("DELETE FROM localSms WHERE threadId = :threadId")
    suspend fun deleteThread(threadId: Long)

    @Insert
    suspend fun createSms(smsData: SmsData): Long

    @Query("SELECT * FROM localSms WHERE address = :address")
    suspend fun getSmsByAddress(address: String): List<SmsData>
}
