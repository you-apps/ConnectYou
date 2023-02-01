package com.bnyro.contacts.util

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.AUTHORITY
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.RawContacts
import androidx.annotation.RequiresPermission
import com.bnyro.contacts.R
import com.bnyro.contacts.ext.intValue
import com.bnyro.contacts.ext.longValue
import com.bnyro.contacts.ext.notAName
import com.bnyro.contacts.ext.stringValue
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.TranslatedType
import com.bnyro.contacts.obj.ValueWithType

class ContactsHelper(private val context: Context) {
    private val contentResolver = context.contentResolver
    private val androidAccountType = "com.android.contacts"

    private val projection = arrayOf(
        RawContacts.CONTACT_ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        StructuredName.GIVEN_NAME,
        StructuredName.FAMILY_NAME,
        RawContacts.ACCOUNT_TYPE
    )

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun getContactList(): List<ContactData> {
        val contactList = mutableListOf<ContactData>()

        @Suppress("SameParameterValue")
        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            null,
            null,
            Phone.DISPLAY_NAME + " ASC"
        ) ?: return contactList

        cursor.use {
            while (it.moveToNext()) {
                val contactId = it.longValue(RawContacts.CONTACT_ID)!!

                // avoid duplicates
                if (contactList.any { contact -> contact.contactId == contactId }) continue

                val displayName = it.stringValue(ContactsContract.Contacts.DISPLAY_NAME)
                var firstName = it.stringValue(StructuredName.GIVEN_NAME)
                var surName = it.stringValue(StructuredName.FAMILY_NAME)

                // try parsing the display name to a proper name
                if (firstName.notAName() || surName.notAName()) {
                    val displayNameParts = displayName.orEmpty().split(" ")
                    when {
                        displayNameParts.size >= 2 -> {
                            firstName = displayNameParts.subList(0, displayNameParts.size - 1).joinToString(
                                " "
                            )
                            surName = displayNameParts.last()
                        }
                        displayNameParts.size == 1 -> {
                            firstName = displayNameParts.first()
                            surName = ""
                        }
                    }
                }

                val contact = ContactData(
                    contactId = contactId,
                    accountType = it.stringValue(RawContacts.ACCOUNT_TYPE),
                    displayName = displayName,
                    firstName = firstName,
                    surName = surName
                )
                contactList.add(contact)
            }
        }

        return contactList
    }

    fun loadAdvancedData(contact: ContactData): ContactData {
        contact.events = getExtras(
            contact.contactId,
            Event.START_DATE,
            Event.TYPE,
            Event.CONTENT_ITEM_TYPE
        )
        contact.numbers = getExtras(
            contact.contactId,
            Phone.NUMBER,
            Phone.TYPE,
            Phone.CONTENT_ITEM_TYPE
        )
        contact.emails = getExtras(
            contact.contactId,
            Email.ADDRESS,
            Email.TYPE,
            Email.CONTENT_ITEM_TYPE
        )
        contact.addresses = getExtras(
            contact.contactId,
            StructuredPostal.FORMATTED_ADDRESS,
            StructuredPostal.TYPE,
            StructuredPostal.CONTENT_ITEM_TYPE
        )
        return contact
    }

    @Suppress("SameParameterValue")
    private fun getExtras(contactId: Long, valueIndex: String, typeIndex: String, itemType: String): List<ValueWithType> {
        val entries = mutableListOf<ValueWithType>()
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            valueIndex,
            typeIndex
        )

        contentResolver.query(
            uri,
            projection,
            getSourcesSelection(),
            getSourcesSelectionArgs(itemType, contactId),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val startDate = cursor.stringValue(valueIndex) ?: return@use
                val type = cursor.intValue(typeIndex)

                val event = ValueWithType(startDate, type)
                if (!entries.contains(event)) entries.add(event)
            }
        }

        return entries
    }

    private fun getSourcesSelectionArgs(mimeType: String? = null, contactId: Long? = null): Array<String> {
        val args = ArrayList<String>()

        if (mimeType != null) {
            args.add(mimeType)
        }

        if (contactId != null) {
            args.add(contactId.toString())
        }

        return args.toTypedArray()
    }

    private fun getSourcesSelection(addMimeType: Boolean = true, addContactId: Boolean = true): String {
        val strings = ArrayList<String>()
        if (addMimeType) {
            strings.add("${ContactsContract.Data.MIMETYPE} = ?")
        }

        if (addContactId) {
            strings.add("${ContactsContract.Data.CONTACT_ID} = ?")
        }

        return strings.joinToString(" AND ")
    }

    @RequiresPermission(Manifest.permission.WRITE_CONTACTS)
    fun deleteContacts(contacts: List<ContactData>) {
        val operations = ArrayList<ContentProviderOperation>()
        val selection = "${RawContacts.CONTACT_ID} = ?"
        contacts.forEach {
            ContentProviderOperation.newDelete(RawContacts.CONTENT_URI).apply {
                val selectionArgs = arrayOf(it.contactId.toString())
                withSelection(selection, selectionArgs)
                operations.add(build())
            }
        }

        context.contentResolver.applyBatch(AUTHORITY, operations)
    }

    @RequiresPermission(Manifest.permission.WRITE_CONTACTS)
    fun createContact(contact: ContactData) {
        val ops = arrayListOf(
            getCreateAction(contact.displayName.orEmpty(), contact.accountType),
            getInsertAction(
                StructuredName.CONTENT_ITEM_TYPE,
                StructuredName.DISPLAY_NAME,
                contact.displayName.orEmpty()
            ),
            getInsertAction(
                StructuredName.CONTENT_ITEM_TYPE,
                StructuredName.GIVEN_NAME,
                contact.firstName.orEmpty()
            ),
            getInsertAction(
                StructuredName.CONTENT_ITEM_TYPE,
                StructuredName.FAMILY_NAME,
                contact.surName.orEmpty()
            ),
            *contact.numbers.map {
                getInsertAction(
                    Phone.CONTENT_ITEM_TYPE,
                    Phone.NUMBER,
                    it.value,
                    Phone.TYPE,
                    it.type
                )
            }.toTypedArray(),
            *contact.emails.map {
                getInsertAction(
                    Email.CONTENT_ITEM_TYPE,
                    Email.ADDRESS,
                    it.value,
                    Email.TYPE,
                    it.type
                )
            }.toTypedArray(),
            *contact.addresses.map {
                getInsertAction(
                    StructuredPostal.CONTENT_ITEM_TYPE,
                    StructuredPostal.FORMATTED_ADDRESS,
                    it.value,
                    StructuredPostal.TYPE,
                    it.type
                )
            }.toTypedArray(),
            *contact.events.map {
                getInsertAction(
                    Event.CONTENT_ITEM_TYPE,
                    Event.START_DATE,
                    it.value,
                    Event.TYPE,
                    it.type
                )
            }.toTypedArray()
        )

        contentResolver.applyBatch(AUTHORITY, ops)
    }

    private fun getCreateAction(accountName: String, accountType: String?): ContentProviderOperation {
        return ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
            .withValue(RawContacts.ACCOUNT_TYPE, accountType ?: androidAccountType)
            .withValue(RawContacts.ACCOUNT_NAME, accountName)
            .build()
    }

    private fun getInsertAction(
        mimeType: String,
        valueIndex: String,
        value: String,
        typeIndex: String? = null,
        type: Int? = null
    ): ContentProviderOperation {
        return ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, mimeType)
            .withValue(valueIndex, value)
            .apply {
                typeIndex?.let {
                    withValue(it, type)
                }
            }
            .build()
    }

    fun getContactPhotoThumbnail(contactId: Long): Bitmap? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val `is` = ContactsContract.Contacts.openContactPhotoInputStream(
            context.contentResolver,
            contactUri
        )
        return BitmapFactory.decodeStream(`is`)
    }

    fun getContactPhoto(contactId: Long): Bitmap? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val displayPhotoUri =
            Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
        return runCatching {
            context.contentResolver.openAssetFileDescriptor(displayPhotoUri, "r").use { fd ->
                BitmapFactory.decodeStream(fd!!.createInputStream())
            }
        }.getOrNull()
    }

    companion object {
        val emailTypes = listOf(
            TranslatedType(Email.TYPE_HOME, R.string.home),
            TranslatedType(Email.TYPE_WORK, R.string.work),
            TranslatedType(Email.TYPE_MOBILE, R.string.mobile),
            TranslatedType(Email.TYPE_CUSTOM, R.string.custom),
            TranslatedType(Email.TYPE_OTHER, R.string.other)
        )

        val phoneNumberTypes = listOf(
            TranslatedType(Phone.TYPE_HOME, R.string.home),
            TranslatedType(Phone.TYPE_MOBILE, R.string.mobile),
            TranslatedType(Phone.TYPE_WORK, R.string.work),
            TranslatedType(Phone.TYPE_CAR, R.string.car),
            TranslatedType(Phone.TYPE_FAX_HOME, R.string.fax_home),
            TranslatedType(Phone.TYPE_FAX_WORK, R.string.fax_work),
            TranslatedType(Phone.TYPE_ASSISTANT, R.string.assistant),
            TranslatedType(Phone.TYPE_CUSTOM, R.string.custom),
            TranslatedType(Phone.TYPE_OTHER, R.string.other)
        )

        val addressTypes = listOf(
            TranslatedType(StructuredPostal.TYPE_HOME, R.string.home),
            TranslatedType(StructuredPostal.TYPE_WORK, R.string.work),
            TranslatedType(StructuredPostal.TYPE_CUSTOM, R.string.custom),
            TranslatedType(StructuredPostal.TYPE_OTHER, R.string.other)
        )

        val eventTypes = listOf(
            TranslatedType(Event.TYPE_BIRTHDAY, R.string.birthday),
            TranslatedType(Event.TYPE_ANNIVERSARY, R.string.anniversary),
            TranslatedType(Event.TYPE_CUSTOM, R.string.custom),
            TranslatedType(Event.TYPE_OTHER, R.string.other)
        )
    }
}
