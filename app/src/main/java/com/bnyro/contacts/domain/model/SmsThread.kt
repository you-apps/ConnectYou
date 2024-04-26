package com.bnyro.contacts.domain.model

import com.bnyro.contacts.data.database.obj.SmsData

data class SmsThread(
    val threadId: Long,
    val address: String,
    val contactData: ContactData?,
    val smsList: List<SmsData>
)
