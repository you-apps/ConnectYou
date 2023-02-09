package com.bnyro.contacts.obj

import android.graphics.Bitmap

data class ContactData(
    val contactId: Long = 0,
    val accountType: String? = null,
    val displayName: String? = null,
    val firstName: String? = null,
    val surName: String? = null,
    var photo: Bitmap? = null,
    var thumbnail: Bitmap? = null,
    var numbers: List<ValueWithType> = listOf(),
    var emails: List<ValueWithType> = listOf(),
    var addresses: List<ValueWithType> = listOf(),
    var events: List<ValueWithType> = listOf()
)
