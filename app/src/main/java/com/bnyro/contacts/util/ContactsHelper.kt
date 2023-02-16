package com.bnyro.contacts.util

import android.provider.ContactsContract
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.TranslatedType

abstract class ContactsHelper {
    abstract val label: String

    abstract suspend fun createContact(contact: ContactData)

    abstract suspend fun updateContact(contact: ContactData)

    abstract suspend fun deleteContacts(contacts: List<ContactData>)

    abstract suspend fun getContactList(): List<ContactData>

    abstract suspend fun loadAdvancedData(contact: ContactData): ContactData

    abstract fun isAutoBackupEnabled(): Boolean

    companion object {
        val emailTypes = listOf(
            TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_HOME, R.string.home),
            TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_WORK, R.string.work),
            TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_MOBILE, R.string.mobile),
            TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM, R.string.custom),
            TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_OTHER, R.string.other)
        )

        val phoneNumberTypes = listOf(
            TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_HOME, R.string.home),
            TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, R.string.mobile),
            TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_WORK, R.string.work),
            TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_CAR, R.string.car),
            TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME, R.string.fax_home),
            TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK, R.string.fax_work),
            TranslatedType(
                ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT,
                R.string.assistant
            ),
            TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM, R.string.custom),
            TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER, R.string.other)
        )

        val addressTypes = listOf(
            TranslatedType(
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME,
                R.string.home
            ),
            TranslatedType(
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK,
                R.string.work
            ),
            TranslatedType(
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM,
                R.string.custom
            ),
            TranslatedType(
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER,
                R.string.other
            )
        )

        val eventTypes = listOf(
            TranslatedType(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY, R.string.birthday),
            TranslatedType(
                ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY,
                R.string.anniversary
            ),
            TranslatedType(ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM, R.string.custom),
            TranslatedType(ContactsContract.CommonDataKinds.Event.TYPE_OTHER, R.string.other)
        )
    }
}
