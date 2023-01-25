package com.bnyro.contacts.obj

data class ContactData(
    val contactId: Long,
    val accountType: String?,
    val displayName: String?,
    val givenName: String?,
    val familyName: String?,
    var phoneNumber: List<String> = listOf(),
    var emails: List<String> = listOf()
)
