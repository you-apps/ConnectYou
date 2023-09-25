package com.bnyro.contacts.db.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "localSms")
data class SmsData(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo val address: String = "",
    @ColumnInfo val body: String = "",
    @ColumnInfo val timestamp: Long = 0,
    @ColumnInfo val threadId: Long = 0,
    @ColumnInfo val type: Int = 0
)
