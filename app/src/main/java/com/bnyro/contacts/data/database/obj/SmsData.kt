package com.bnyro.contacts.data.database.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bnyro.contacts.domain.model.SmsBackup

@Entity(tableName = "localSms")
data class SmsData(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo var address: String = "",
    @ColumnInfo var body: String = "",
    @ColumnInfo var timestamp: Long = 0,
    @ColumnInfo var threadId: Long = 0,
    @ColumnInfo var type: Int = 0,
    @ColumnInfo(defaultValue = "NULL") var simNumber: Int? = null
) {
    fun toSmsBackup() = SmsBackup(
        subscriptionId = simNumber?.toLong() ?: 0,
        address = address,
        body = body,
        date = timestamp,
        dateSent = timestamp,
        type = type,
    )
}