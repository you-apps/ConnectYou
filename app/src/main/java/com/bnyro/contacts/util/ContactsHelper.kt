package com.bnyro.contacts.util

import android.provider.ContactsContract
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.TranslatedType
import ezvcard.parameter.AddressType
import ezvcard.parameter.EmailType
import ezvcard.parameter.TelephoneType

object ContactsHelper {
    val emailTypes = listOf(
        TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_HOME, R.string.home, EmailType.HOME),
        TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_WORK, R.string.work, EmailType.WORK),
        TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_MOBILE, R.string.mobile, EmailType.PREF),
        TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM, R.string.custom),
        TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_OTHER, R.string.other)
    )

    val phoneNumberTypes = listOf(
        TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_HOME, R.string.home, TelephoneType.HOME),
        TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, R.string.mobile, TelephoneType.CELL),
        TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_WORK, R.string.work, TelephoneType.WORK),
        TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_CAR, R.string.car, TelephoneType.CAR),
        TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME, R.string.fax_home, TelephoneType.FAX),
        TranslatedType(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK, R.string.fax_work, TelephoneType.FAX),
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
            R.string.home,
            AddressType.HOME
        ),
        TranslatedType(
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK,
            R.string.work,
            AddressType.WORK
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

    val websiteTypes = listOf(
        TranslatedType(ContactsContract.CommonDataKinds.Website.TYPE_HOME, R.string.home),
        TranslatedType(ContactsContract.CommonDataKinds.Website.TYPE_WORK, R.string.work),
        TranslatedType(
            ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE,
            R.string.homepage
        ),
        TranslatedType(ContactsContract.CommonDataKinds.Website.TYPE_BLOG, R.string.blog),
        TranslatedType(ContactsContract.CommonDataKinds.Website.TYPE_FTP, R.string.ftp),
        TranslatedType(ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM, R.string.custom),
        TranslatedType(ContactsContract.CommonDataKinds.Website.TYPE_OTHER, R.string.other)
    )

    fun splitFullName(displayName: String?): Pair<String, String> {
        val displayNameParts = displayName.orEmpty().split(" ")
        return when {
            displayNameParts.size >= 2 -> {
                displayNameParts.subList(0, displayNameParts.size - 1).joinToString(
                    " "
                ) to displayNameParts.last()
            }

            displayNameParts.size == 1 -> {
                displayNameParts.first() to ""
            }

            else -> {
                "" to ""
            }
        }
    }
}
