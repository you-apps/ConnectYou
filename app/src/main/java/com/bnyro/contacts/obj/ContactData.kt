package com.bnyro.contacts.obj

data class ContactData(
    val contactId: Long,
    val displayName: String?,
    val givenName: String?,
    val familyName: String?,
    val phoneNumber: List<String>
)
