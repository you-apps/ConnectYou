package com.bnyro.contacts.util

import android.Manifest
import android.annotation.SuppressLint
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
import android.provider.ContactsContract.CommonDataKinds.Photo
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.RawContacts
import androidx.annotation.RequiresPermission
import com.bnyro.contacts.ext.intValue
import com.bnyro.contacts.ext.longValue
import com.bnyro.contacts.ext.notAName
import com.bnyro.contacts.ext.pmap
import com.bnyro.contacts.ext.stringValue
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ValueWithType

class DeviceContactsHelper(private val context: Context) : ContactsHelper() {
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
    override suspend fun getContactList(): List<ContactData> {
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
                var lastName = it.stringValue(StructuredName.FAMILY_NAME)

                // try parsing the display name to a proper name
                if (firstName.notAName() || lastName.notAName()) {
                    val displayNameParts = displayName.orEmpty().split(" ")
                    when {
                        displayNameParts.size >= 2 -> {
                            firstName = displayNameParts.subList(0, displayNameParts.size - 1).joinToString(
                                " "
                            )
                            lastName = displayNameParts.last()
                        }
                        displayNameParts.size == 1 -> {
                            firstName = displayNameParts.first()
                            lastName = ""
                        }
                    }
                }

                val contact = ContactData(
                    contactId = contactId,
                    accountType = it.stringValue(RawContacts.ACCOUNT_TYPE),
                    displayName = displayName,
                    firstName = firstName,
                    lastName = lastName
                )
                contactList.add(contact)
            }
        }

        return contactList.pmap {
            it.apply {
                thumbnail = getContactPhotoThumbnail(contactId)
                photo = getContactPhoto(contactId)
            }
        }
    }

    override suspend fun loadAdvancedData(contact: ContactData) = contact.apply {
        events = getExtras(
            contactId,
            Event.START_DATE,
            Event.TYPE,
            Event.CONTENT_ITEM_TYPE
        )
        numbers = getExtras(
            contactId,
            Phone.NUMBER,
            Phone.TYPE,
            Phone.CONTENT_ITEM_TYPE
        )
        emails = getExtras(
            contactId,
            Email.ADDRESS,
            Email.TYPE,
            Email.CONTENT_ITEM_TYPE
        )
        addresses = getExtras(
            contactId,
            StructuredPostal.FORMATTED_ADDRESS,
            StructuredPostal.TYPE,
            StructuredPostal.CONTENT_ITEM_TYPE
        )
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
    override suspend fun deleteContacts(contacts: List<ContactData>) {
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

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.WRITE_CONTACTS)
    override suspend fun createContact(contact: ContactData) {
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
                contact.lastName.orEmpty()
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
        ).apply {
            addPhoto(contact)?.let { add(it) }
        }

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
        value: Any,
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

    private fun getContactPhotoThumbnail(contactId: Long): Bitmap? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val `is` = ContactsContract.Contacts.openContactPhotoInputStream(
            context.contentResolver,
            contactUri
        )
        return BitmapFactory.decodeStream(`is`)
    }

    private fun getContactPhoto(contactId: Long): Bitmap? {
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

    private fun addPhoto(contact: ContactData): ContentProviderOperation? {
        val bitmap = contact.photo ?: return null
        var bytes: ByteArray = ImageHelper.bitmapToByteArray(bitmap)

        // prevent crashes due to a too large transaction
        if (bytes.size / 1024 > 900) {
            val scaleFactor = MAX_PHOTO_SIZE / maxOf(bitmap.width, bitmap.height)
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scaleFactor).toInt(),
                (bitmap.height * scaleFactor).toInt(),
                false
            )
            bytes = ImageHelper.bitmapToByteArray(scaledBitmap)
        }

        return getInsertAction(Photo.CONTENT_ITEM_TYPE, Photo.PHOTO, bytes)
    }

    companion object {
        const val MAX_PHOTO_SIZE = 700f
    }
}
