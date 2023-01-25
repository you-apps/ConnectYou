package com.bnyro.contacts.obj

data class ContactData(
    val contactId: Long = 0,
    val accountType: String? = null,
    val displayName: String? = null,
    val givenName: String? = null,
    val familyName: String? = null,
    var phoneNumber: List<String> = listOf(),
    var emails: List<String> = listOf()
)
