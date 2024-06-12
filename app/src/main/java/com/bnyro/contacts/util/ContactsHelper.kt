package com.bnyro.contacts.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.Addresses
import com.bnyro.contacts.domain.enums.Emails
import com.bnyro.contacts.domain.enums.Events
import com.bnyro.contacts.domain.enums.Nickname
import com.bnyro.contacts.domain.enums.Notes
import com.bnyro.contacts.domain.enums.Numbers
import com.bnyro.contacts.domain.enums.Organization
import com.bnyro.contacts.domain.enums.Title
import com.bnyro.contacts.domain.enums.Websites
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.TranslatedType
import com.google.i18n.phonenumbers.PhoneNumberUtil
import ezvcard.parameter.AddressType
import ezvcard.parameter.EmailType
import ezvcard.parameter.TelephoneType
import java.text.Normalizer

object ContactsHelper {
    private val REGEX_NORMALIZE = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    val emailTypes = listOf(
        TranslatedType(
            ContactsContract.CommonDataKinds.Email.TYPE_HOME,
            R.string.home,
            EmailType.HOME
        ),
        TranslatedType(
            ContactsContract.CommonDataKinds.Email.TYPE_WORK,
            R.string.work,
            EmailType.WORK
        ),
        TranslatedType(
            ContactsContract.CommonDataKinds.Email.TYPE_MOBILE,
            R.string.mobile,
            EmailType.PREF
        ),
        TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM, R.string.custom),
        TranslatedType(ContactsContract.CommonDataKinds.Email.TYPE_OTHER, R.string.other)
    )

    val phoneNumberTypes = listOf(
        TranslatedType(
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME,
            R.string.home,
            TelephoneType.HOME
        ),
        TranslatedType(
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
            R.string.mobile,
            TelephoneType.CELL
        ),
        TranslatedType(
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK,
            R.string.work,
            TelephoneType.WORK
        ),
        TranslatedType(
            ContactsContract.CommonDataKinds.Phone.TYPE_CAR,
            R.string.car,
            TelephoneType.CAR
        ),
        TranslatedType(
            ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME,
            R.string.fax_home,
            TelephoneType.FAX
        ),
        TranslatedType(
            ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK,
            R.string.fax_work,
            TelephoneType.FAX
        ),
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

    val contactAttributesTypes = listOf(
        Nickname(), Title(), Organization(),
        Numbers(), Addresses(), Emails(), Events(), Websites(), Notes()
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

    private fun normalizeText(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(REGEX_NORMALIZE, "")
            .lowercase()
    }

    fun normalizePhoneNumber(number: String): String {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val phoneNumber = runCatching { phoneUtil.parse(number, null) }
            .getOrElse { return number }
        return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
    }

    fun matches(contactData: ContactData, query: String): Boolean {
        val normalizedQuery = normalizeText(query)

        val contactInfoStrings = listOf(
            contactData.numbers,
            contactData.emails,
            contactData.addresses,
            contactData.notes,
            contactData.websites,
            contactData.events
        )
            .flatten()
            .map { (value, _) -> value } + listOf(
            contactData.organization,
            contactData.nickName,
            contactData.displayName
        )

        return contactInfoStrings.filterNotNull().any { str ->
            normalizeText(str).contains(normalizedQuery)
        }
    }

    fun filter(contacts: List<ContactData>, searchQuery: String): List<ContactData> =
        contacts.filter { matches(it, searchQuery) }

    fun isContactEmpty(contactData: ContactData): Boolean {
        val stringProperties = listOf(
            contactData.firstName,
            contactData.surName,
            contactData.nickName,
            contactData.organization
        )
        val listProperties = listOf(
            contactData.numbers,
            contactData.emails,
            contactData.events,
            contactData.addresses,
            contactData.notes
        )

        return stringProperties.none { !it.isNullOrBlank() } && listProperties.flatten().isEmpty()
    }

    fun getContactPhotoThumbnail(context: Context, photoThumbnailUri: Uri): Bitmap? {
        val contentResolver = context.contentResolver
        val assetFileDescriptor =
            contentResolver.openAssetFileDescriptor(photoThumbnailUri, "r") ?: return null
        val fileDescriptor = assetFileDescriptor.fileDescriptor
        val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        assetFileDescriptor.close()
        return bitmap
    }
}
