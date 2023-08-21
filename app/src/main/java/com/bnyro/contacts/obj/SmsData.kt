package com.bnyro.contacts.obj

import com.bnyro.contacts.enums.SmsType

data class SmsData(
    val id: Long,
    val address: String,
    val body: String,
    val timestamp: Long,
    val threadId: Long,
    val type: SmsType
)
