package com.bnyro.contacts.obj

data class ContactData(
    val contactId: Long = 0,
    val accountType: String? = null,
    val displayName: String? = null,
    val firstName: String? = null,
    val surName: String? = null,
    var phoneNumber: List<String> = listOf(),
    var emails: List<String> = listOf(),
    var addresses: List<String> = listOf(),
    var events: List<String> = listOf()
)
