package com.bnyro.contacts.obj

import com.bnyro.contacts.db.obj.SmsData

data class SmsThread(
    val threadId: Long,
    val address: String,
    val contactData: ContactData?,
    val smsList: List<SmsData>
)
