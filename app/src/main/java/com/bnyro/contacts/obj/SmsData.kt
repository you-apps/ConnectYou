package com.bnyro.contacts.obj

data class SmsData(
    var id: Long,
    val address: String,
    val body: String,
    val timestamp: Long,
    val threadId: Long,
    val type: Int
)
