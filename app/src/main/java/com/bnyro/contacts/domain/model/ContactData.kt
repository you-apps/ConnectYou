package com.bnyro.contacts.domain.model

import android.graphics.Bitmap
import android.net.Uri
import com.bnyro.contacts.domain.enums.SortOrder

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
    var title: String? = null,
    var organization: String? = null,
    var photo: Bitmap? = null,
    var thumbnail: Bitmap? = null,
    var numbers: List<ValueWithType> = listOf(),
    var emails: List<ValueWithType> = listOf(),
    var addresses: List<ValueWithType> = listOf(),
    var events: List<ValueWithType> = listOf(),
    var notes: List<ValueWithType> = listOf(),
    var groups: List<ContactsGroup> = listOf(),
    var websites: List<ValueWithType> = listOf(),
    var ringTone: Uri? = null,
    var favorite: Boolean = false
) {
    val accountIdentifier get() = "$accountType|$accountName"
    fun getNameBySortOrder(sortOrder: SortOrder): String? {
        return when (sortOrder) {
            SortOrder.FIRSTNAME -> displayName
            SortOrder.LASTNAME -> alternativeName
            SortOrder.NICKNAME -> nickName ?: displayName
        }?.ifBlank {
            organization ?: numbers.firstOrNull()?.value
        }
    }
}
