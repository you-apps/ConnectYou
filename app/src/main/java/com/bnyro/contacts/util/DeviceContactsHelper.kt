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
import android.provider.ContactsContract.CALLER_IS_SYNCADAPTER
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Photo
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import androidx.annotation.RequiresPermission
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.BackupType
import com.bnyro.contacts.ext.intValue
import com.bnyro.contacts.ext.longValue
import com.bnyro.contacts.ext.notAName
import com.bnyro.contacts.ext.stringValue
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ContactsGroup
import com.bnyro.contacts.obj.ValueWithType

class DeviceContactsHelper(private val context: Context) : ContactsHelper() {
    override val label: String = context.getString(R.string.device)

    private val contentResolver = context.contentResolver
    private val contactsUri = Data.CONTENT_URI

    private val projection = arrayOf(
        Data.RAW_CONTACT_ID,
        RawContacts.CONTACT_ID,
        Contacts.DISPLAY_NAME,
        Contacts.DISPLAY_NAME_ALTERNATIVE,
        StructuredName.GIVEN_NAME,
        StructuredName.FAMILY_NAME,
        Nickname.NAME,
        Organization.COMPANY,
        RawContacts.ACCOUNT_TYPE,
        RawContacts.ACCOUNT_NAME
    )

    private var storedContactGroups: List<ValueWithType> = emptyList()

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override suspend fun getContactList(): List<ContactData> {
        storedContactGroups = getStoredGroups()
        val contactList = mutableListOf<ContactData>()

        @Suppress("SameParameterValue")
        val cursor = contentResolver.query(
            contactsUri,
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

                val displayName = it.stringValue(Contacts.DISPLAY_NAME)
                val alternativeName = it.stringValue(Contacts.DISPLAY_NAME_ALTERNATIVE)
                var firstName = it.stringValue(StructuredName.GIVEN_NAME)
                var surName = it.stringValue(StructuredName.FAMILY_NAME)

                // try parsing the display name to a proper name
                if (firstName.notAName() || surName.notAName()) {
                    val nameParts = splitFullName(displayName)
                    firstName = nameParts.first
                    surName = nameParts.second
                }

                val contact = ContactData(
                    rawContactId = it.intValue(Data.RAW_CONTACT_ID) ?: 0,
                    contactId = contactId,
                    accountType = it.stringValue(RawContacts.ACCOUNT_TYPE),
                    accountName = it.stringValue(RawContacts.ACCOUNT_NAME),
                    displayName = displayName,
                    alternativeName = alternativeName,
                    firstName = firstName,
                    surName = surName
                )

                contactList.add(contact)
            }
        }

        return contactList
    }

    private fun getEntry(contactId: Long, type: String, column: String): String? {
        return getExtras(contactId, column, null, type).firstOrNull()?.value
    }

    override suspend fun loadAdvancedData(contact: ContactData) = contact.apply {
        thumbnail = getContactPhotoThumbnail(contactId)
        photo = getContactPhoto(contactId) ?: thumbnail
        groups = getGroups(contactId, storedContactGroups)
        nickName = getEntry(contactId, Nickname.CONTENT_ITEM_TYPE, Nickname.NAME)
        organization = getEntry(contactId, Organization.CONTENT_ITEM_TYPE, Organization.COMPANY)
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
        notes = getExtras(
            contactId,
            Note.NOTE,
            Note.DATA2,
            Note.CONTENT_ITEM_TYPE
        )
        websites = getExtras(
            contactId,
            Website.URL,
            Website.TYPE,
            Website.CONTENT_ITEM_TYPE
        )
    }

    private fun getGroups(contactId: Long, storedGroups: List<ValueWithType>): List<ContactsGroup> {
        val groups = getExtras(
            contactId,
            GroupMembership.GROUP_ROW_ID,
            GroupMembership.DATA2,
            GroupMembership.CONTENT_ITEM_TYPE
        )
        return groups.mapNotNull { group ->
            storedGroups.firstOrNull { it.type == group.value.toInt() }?.let {
                it.type?.let { type -> ContactsGroup(it.value, type) }
            }
        }
    }

    override suspend fun createGroup(groupName: String): ContactsGroup? {
        val operations = ArrayList<ContentProviderOperation>()
        ContentProviderOperation.newInsert(ContactsContract.Groups.CONTENT_URI).apply {
            withValue(ContactsContract.Groups.TITLE, groupName)
            withValue(ContactsContract.Groups.GROUP_VISIBLE, 1)
            withValue(ContactsContract.Groups.ACCOUNT_NAME, ANDROID_CONTACTS_NAME)
            withValue(ContactsContract.Groups.ACCOUNT_TYPE, ANDROID_ACCOUNT_TYPE)
            operations.add(build())
        }

        runCatching {
            val results = context.contentResolver.applyBatch(AUTHORITY, operations)
            val rawId = ContentUris.parseId(results[0].uri!!)
            return ContactsGroup(groupName, rawId.toInt())
        }
        return null
    }

    override suspend fun renameGroup(group: ContactsGroup, newName: String) {
        val operations = ArrayList<ContentProviderOperation>()
        ContentProviderOperation.newUpdate(ContactsContract.Groups.CONTENT_URI).apply {
            val selection = "${ContactsContract.Groups._ID} = ?"
            val selectionArgs = arrayOf(group.rowId.toString())
            withSelection(selection, selectionArgs)
            withValue(ContactsContract.Groups.TITLE, newName)
            operations.add(build())
        }

        runCatching {
            context.contentResolver.applyBatch(AUTHORITY, operations)
        }
    }

    override suspend fun deleteGroup(group: ContactsGroup) {
        val operations = ArrayList<ContentProviderOperation>()
        val uri = ContentUris.withAppendedId(
            ContactsContract.Groups.CONTENT_URI,
            group.rowId.toLong()
        )
            .buildUpon()
            .appendQueryParameter(CALLER_IS_SYNCADAPTER, "true")
            .build()

        operations.add(ContentProviderOperation.newDelete(uri).build())

        runCatching {
            context.contentResolver.applyBatch(AUTHORITY, operations)
        }
    }

    private fun getStoredGroups(): List<ValueWithType> {
        val groups = ArrayList<ValueWithType>()

        val uri = ContactsContract.Groups.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Groups._ID,
            ContactsContract.Groups.TITLE
        )

        contentResolver.query(uri, projection, null, arrayOf(), null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.intValue(ContactsContract.Groups._ID)
                val title = cursor.stringValue(ContactsContract.Groups.TITLE) ?: return@use

                val group = ValueWithType(title, id)
                if (groups.none { it.value == title }) groups.add(group)
            }
        }
        return groups
    }

    override fun isAutoBackupEnabled(): Boolean {
        return Preferences.getBackupType() in listOf(BackupType.BOTH, BackupType.DEVICE)
    }

    @Suppress("SameParameterValue")
    private fun getExtras(contactId: Long, valueIndex: String, typeIndex: String?, itemType: String): List<ValueWithType> {
        val entries = mutableListOf<ValueWithType>()
        val projection = arrayOf(Data.CONTACT_ID, valueIndex, typeIndex ?: "data2")

        contentResolver.query(
            contactsUri,
            projection,
            "${Data.MIMETYPE} = ? AND ${Data.CONTACT_ID} = ?",
            arrayOf(itemType, contactId.toString()),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val entry = ValueWithType(
                    cursor.stringValue(valueIndex) ?: return@use,
                    typeIndex?.let { cursor.intValue(it) }
                )
                if (!entries.contains(entry)) entries.add(entry)
            }
        }

        return entries
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
        val ops = listOfNotNull(
            getCreateAction(
                contact.accountType ?: ANDROID_ACCOUNT_TYPE,
                contact.accountName ?: ANDROID_CONTACTS_NAME
            ),
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
            contact.nickName?.let {
                getInsertAction(
                    Nickname.CONTENT_ITEM_TYPE,
                    Nickname.NAME,
                    it
                )
            },
            contact.organization?.let {
                getInsertAction(
                    Organization.CONTENT_ITEM_TYPE,
                    Organization.COMPANY,
                    it
                )
            },
            contact.photo?.let {
                getInsertAction(Photo.CONTENT_ITEM_TYPE, Photo.PHOTO, getBitmapBytes(it))
            },
            *contact.websites.map {
                getInsertAction(
                    Website.CONTENT_ITEM_TYPE,
                    Website.URL,
                    it.value,
                    Website.TYPE,
                    it.type
                )
            }.toTypedArray(),
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
            }.toTypedArray(),
            *contact.notes.map {
                getInsertAction(
                    Note.CONTENT_ITEM_TYPE,
                    Note.NOTE,
                    it.value
                )
            }.toTypedArray(),
            *contact.groups.map {
                getInsertAction(
                    GroupMembership.CONTENT_ITEM_TYPE,
                    GroupMembership.GROUP_ROW_ID,
                    it.rowId.toString()
                )
            }.toTypedArray()
        ).let { ArrayList(it) }

        contentResolver.applyBatch(AUTHORITY, ops)
    }

    @RequiresPermission(Manifest.permission.WRITE_CONTACTS)
    override suspend fun updateContact(contact: ContactData) {
        val operations = ArrayList<ContentProviderOperation>()
        val rawContactId = contact.rawContactId.toString()

        val selection = "${Data.RAW_CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?"
        ContentProviderOperation.newUpdate(contactsUri).apply {
            val selectionArgs = arrayOf(rawContactId, StructuredName.CONTENT_ITEM_TYPE)
            withSelection(selection, selectionArgs)
            withValue(StructuredName.GIVEN_NAME, contact.firstName)
            withValue(StructuredName.FAMILY_NAME, contact.surName)
            withValue(StructuredName.DISPLAY_NAME, contact.displayName)
            operations.add(build())
        }

        operations.addAll(
            getUpdateSingleAction(
                rawContactId,
                Nickname.CONTENT_ITEM_TYPE,
                Nickname.NAME,
                contact.nickName
            )
        )
        operations.addAll(
            getUpdateSingleAction(
                rawContactId,
                Organization.CONTENT_ITEM_TYPE,
                Organization.COMPANY,
                contact.organization
            )
        )

        operations.addAll(
            getUpdateMultipleAction(
                rawContactId,
                Website.CONTENT_ITEM_TYPE,
                contact.websites,
                Website.URL,
                Website.TYPE
            )
        )
        operations.addAll(
            getUpdateMultipleAction(
                rawContactId,
                Phone.CONTENT_ITEM_TYPE,
                contact.numbers,
                Phone.NUMBER,
                Phone.TYPE
            )
        )
        operations.addAll(
            getUpdateMultipleAction(
                rawContactId,
                Email.CONTENT_ITEM_TYPE,
                contact.emails,
                Email.ADDRESS,
                Email.TYPE
            )
        )
        operations.addAll(
            getUpdateMultipleAction(
                rawContactId,
                StructuredPostal.CONTENT_ITEM_TYPE,
                contact.addresses,
                StructuredPostal.FORMATTED_ADDRESS,
                StructuredPostal.TYPE
            )
        )
        operations.addAll(
            getUpdateMultipleAction(
                rawContactId,
                Event.CONTENT_ITEM_TYPE,
                contact.events,
                Event.START_DATE,
                Event.TYPE
            )
        )
        operations.addAll(
            getUpdateMultipleAction(
                rawContactId,
                Note.CONTENT_ITEM_TYPE,
                contact.notes,
                Note.NOTE,
                null
            )
        )
        operations.addAll(
            getUpdateMultipleAction(
                rawContactId,
                GroupMembership.CONTENT_ITEM_TYPE,
                // The value to be saved here is only the row id!
                contact.groups.map { ValueWithType(it.rowId.toString(), null) },
                GroupMembership.GROUP_ROW_ID,
                null
            )
        )

        operations.add(deletePhoto(contact.rawContactId))
        contact.photo?.let {
            operations.add(
                getInsertAction(
                    Photo.CONTENT_ITEM_TYPE,
                    Photo.PHOTO,
                    getBitmapBytes(it),
                    rawContactId = contact.rawContactId
                )
            )
        }

        context.contentResolver.applyBatch(AUTHORITY, operations)
    }

    private fun getCreateAction(accountType: String, accountName: String): ContentProviderOperation {
        return ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
            .withValue(RawContacts.ACCOUNT_TYPE, accountType)
            .withValue(RawContacts.ACCOUNT_NAME, accountName)
            .build()
    }

    private fun getInsertAction(
        mimeType: String,
        valueIndex: String,
        value: Any,
        typeIndex: String? = null,
        type: Int? = null,
        rawContactId: Int? = null
    ): ContentProviderOperation {
        return ContentProviderOperation.newInsert(contactsUri)
            .let { builder ->
                // if creating a new contact, the previous contact id is going to be taken
                // if updating an already existing contact, don't worry about the previous batch id
                rawContactId?.let { builder.withValue(Data.RAW_CONTACT_ID, it) }
                    ?: builder.withValueBackReference(Data.RAW_CONTACT_ID, 0)
            }
            .withValue(Data.MIMETYPE, mimeType)
            .withValue(valueIndex, value)
            .apply {
                typeIndex?.let {
                    withValue(it, type)
                }
            }
            .build()
    }

    private fun getUpdateSingleAction(
        contactId: String,
        mimeType: String,
        valueIndex: String,
        value: String?
    ) = getUpdateMultipleAction(
        contactId,
        mimeType,
        listOfNotNull(value?.let { ValueWithType(it, null) }),
        valueIndex,
        null
    )

    @Suppress("SameParameterValue")
    private fun getUpdateMultipleAction(
        contactId: String,
        mimeType: String,
        entries: List<ValueWithType>,
        valueIndex: String,
        typeIndex: String?
    ): List<ContentProviderOperation> {
        val operations = mutableListOf<ContentProviderOperation>()

        // delete all entries
        val selection = "${Data.RAW_CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(contactId, mimeType)

        ContentProviderOperation.newDelete(contactsUri).apply {
            withSelection(selection, selectionArgs)
            operations.add(build())
        }

        // add new entries
        entries.forEach {
            ContentProviderOperation.newInsert(contactsUri).apply {
                withValue(Data.RAW_CONTACT_ID, contactId)
                withValue(Data.MIMETYPE, mimeType)
                withValue(valueIndex, it.value)
                typeIndex?.let { t -> withValue(t, it.type) }
                operations.add(build())
            }
        }

        return operations
    }

    private fun getContactPhotoThumbnail(contactId: Long): Bitmap? {
        val contactUri =
            ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)
        val `is` = Contacts.openContactPhotoInputStream(
            context.contentResolver,
            contactUri
        )
        return BitmapFactory.decodeStream(`is`)
    }

    private fun getContactPhoto(contactId: Long): Bitmap? {
        val contactUri =
            ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)
        val displayPhotoUri =
            Uri.withAppendedPath(contactUri, Contacts.Photo.DISPLAY_PHOTO)
        return runCatching {
            context.contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")?.use { fd ->
                BitmapFactory.decodeStream(fd.createInputStream())
            }
        }.getOrNull()
    }

    private fun getBitmapBytes(bitmap: Bitmap): ByteArray {
        var bytes = ImageHelper.bitmapToByteArray(bitmap)

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

        return bytes
    }

    private fun deletePhoto(rawContactId: Int): ContentProviderOperation {
        return ContentProviderOperation.newDelete(contactsUri).apply {
            val selection = "${Data.RAW_CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?"
            val selectionArgs = arrayOf(rawContactId.toString(), Photo.CONTENT_ITEM_TYPE)
            withSelection(selection, selectionArgs)
        }.build()
    }

    companion object {
        const val MAX_PHOTO_SIZE = 700f
        const val ANDROID_ACCOUNT_TYPE = "com.android.contacts"
        const val ANDROID_CONTACTS_NAME = "DEVICE"
    }
}
