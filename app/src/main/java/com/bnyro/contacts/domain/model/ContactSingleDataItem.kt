package com.bnyro.contacts.domain.model

data class ContactSingleDataItem(
    val thumbnail: Any?,
    val name: String,
    val data: String,
    val contactData: ContactData?
)
