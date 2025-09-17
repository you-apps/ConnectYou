package com.bnyro.contacts.data.database.obj

import android.provider.Telephony
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bnyro.contacts.domain.model.SmsBackup

enum class SmsStatus(val androidValue: Int) {
    NONE(Telephony.Sms.STATUS_NONE),
    SENT(Telephony.Sms.STATUS_PENDING),
    DELIVERED(Telephony.Sms.STATUS_COMPLETE),
    ERROR(Telephony.Sms.STATUS_FAILED);

    companion object {
        fun smsStatusFromInt(androidValue: Int): SmsStatus {
            return SmsStatus.entries.firstOrNull { it.androidValue == androidValue } ?: NONE
        }
    }
}

@Entity(tableName = "localSms")
data class SmsData(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo var address: String = "",
    @ColumnInfo var body: String = "",
    @ColumnInfo var timestamp: Long = 0,
    @ColumnInfo var threadId: Long = 0,
    @ColumnInfo var type: Int = 0,
    @ColumnInfo(defaultValue = "NULL") var simNumber: Int? = null,
    @ColumnInfo(defaultValue = "NONE") var status: SmsStatus
) {
    fun toSmsBackup() = SmsBackup(
        subscriptionId = simNumber?.toLong() ?: 0,
        address = address,
        body = body,
        date = timestamp,
        dateSent = timestamp,
        type = type,
        status = status.androidValue,
    )
}