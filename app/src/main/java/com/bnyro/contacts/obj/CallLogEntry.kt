package com.bnyro.contacts.obj

data class CallLogEntry(
    val phoneNumber: String,
    val type: Int,
    val time: Long,
    val duration: Long
)
