package com.bnyro.contacts.obj

import android.graphics.Bitmap

data class ContactData(
    var dataId: Int = 0,
    var rawContactId: Int = 0,
    var contactId: Long = 0,
    var accountType: String? = null,
    var accountName: String? = null,
    var displayName: String? = null,
    var alternativeName: String? = null,
    var firstName: String? = null,
    var surName: String? = null,
    var nickName: String? = null,
    var organization: String? = null,
    var photo: Bitmap? = null,
    var thumbnail: Bitmap? = null,
    var numbers: List<ValueWithType> = listOf(),
    var emails: List<ValueWithType> = listOf(),
    var addresses: List<ValueWithType> = listOf(),
    var events: List<ValueWithType> = listOf(),
    var notes: List<ValueWithType> = listOf(),
    var groups: List<ContactsGroup> = listOf(),
    var websites: List<ValueWithType> = listOf()
)
