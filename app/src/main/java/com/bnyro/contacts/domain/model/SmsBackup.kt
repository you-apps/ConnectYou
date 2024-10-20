package com.bnyro.contacts.domain.model

import com.bnyro.contacts.data.database.obj.SmsData
import kotlinx.serialization.Serializable

@Serializable
data class SmsBackup(
    val subscriptionId: Long,
    val address: String,
    val body: String?,
    val date: Long,
    val dateSent: Long,
    val type: Int,
    val locked: Int = 1,
    val protocol: String? = "0",
    val read: Int = 0,
    val status: Int = -1,
    val serviceCenter: String? = null,
    val backupType: String = "sms",
) {
    fun toSmsData() = SmsData(
        address = address,
        body = body.orEmpty(),
        timestamp = date,
        simNumber = subscriptionId.toInt(),
        type = type,
    )
}
