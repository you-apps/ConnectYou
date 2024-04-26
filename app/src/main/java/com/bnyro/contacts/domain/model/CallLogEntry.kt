package com.bnyro.contacts.domain.model

data class CallLogEntry(
    val phoneNumber: String,
    val type: Int,
    val time: Long,
    val duration: Long
)
