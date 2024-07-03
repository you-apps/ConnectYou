package com.bnyro.contacts.domain.repositories

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.AUTHORITY
import android.provider.ContactsContract.CALLER_IS_SYNCADAPTER
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Photo
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import androidx.annotation.RequiresPermission
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.BackupType
import com.bnyro.contacts.domain.enums.ListAttribute
import com.bnyro.contacts.domain.enums.StringAttribute
import com.bnyro.contacts.domain.model.AccountType
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.ContactsGroup
import com.bnyro.contacts.domain.model.ValueWithType
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.ImageHelper
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.util.extension.boolValue
import com.bnyro.contacts.util.extension.intValue
import com.bnyro.contacts.util.extension.longValue
import com.bnyro.contacts.util.extension.notAName
import com.bnyro.contacts.util.extension.stringValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DeviceContactsRepository(private val context: Context) : ContactsRepository {
    override val label: String = context.getString(R.string.device)

    private val contentResolver = context.contentResolver
    private val contentUri = Data.CONTENT_URI

    private val projection = arrayOf(
        Data.RAW_CONTACT_ID,
        RawContacts.CONTACT_ID,
        Contacts.DISPLAY_NAME,
        Contacts.DISPLAY_NAME_ALTERNATIVE,
        StructuredName.GIVEN_NAME,
        StructuredName.FAMILY_NAME,
        RawContacts.ACCOUNT_TYPE,
        RawContacts.ACCOUNT_NAME,
        Contacts.STARRED
    )

    private var storedContactGroups: List<ValueWithType> = emptyList()

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override suspend fun getContactList(): List<ContactData> {
        storedContactGroups = getStoredGroups()
        val contactList = mutableListOf<ContactData>()

        @Suppress("SameParameterValue")
        val cursor = contentResolver.query(
            contentUri,
            projection,
            null,
            null,
            Phone.DISPLAY_NAME + " ASC"
        ) ?: return contactList

        withContext(Dispatchers.IO) {
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
                        val nameParts = ContactsHelper.splitFullName(displayName)
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
                        surName = surName,
                        favorite = it.boolValue(Contacts.STARRED) ?: false
                    )

                    contactList.add(contact)
                }
            }
        }
        return contactList
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    private fun getEntry(contactId: Long, type: String, column: String): String? {
        return getExtras(contactId, column, null, type).firstOrNull()?.value
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override suspend fun loadAdvancedData(contact: ContactData) = withContext(Dispatchers.IO) {
        contact.apply {
            thumbnail = getContactPhotoThumbnail(contactId)
            photo = getContactPhoto(contactId) ?: thumbnail
            groups = getGroups(contactId, storedContactGroups)

            ContactsHelper.contactAttributesTypes.forEach { attribute ->
                if (attribute is StringAttribute) {
                    val dataStr = getEntry(
                        contactId,
                        attribute.androidContentType,
                        attribute.androidValueColumn
                    )
                    attribute.set(this, dataStr)
                } else if (attribute is ListAttribute) {
                    val dataEntries = getExtras(
                        contactId,
                        attribute.androidValueColumn,
                        attribute.androidTypeColumn,
                        attribute.androidContentType
                    )
                    attribute.set(this, dataEntries)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
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
        return withContext(Dispatchers.IO) {
            val operations = ArrayList<ContentProviderOperation>()
            ContentProviderOperation.newInsert(ContactsContract.Groups.CONTENT_URI).apply {
                withValue(ContactsContract.Groups.TITLE, groupName)
                withValue(ContactsContract.Groups.GROUP_VISIBLE, 1)
                withValue(ContactsContract.Groups.ACCOUNT_NAME, AccountType.androidDefault.name)
                withValue(ContactsContract.Groups.ACCOUNT_TYPE, AccountType.androidDefault.type)
                operations.add(build())
            }

            runCatching {
                val results = context.contentResolver.applyBatch(AUTHORITY, operations)
                val rawId = ContentUris.parseId(results[0].uri!!)
                return@withContext ContactsGroup(groupName, rawId.toInt())
            }
            return@withContext null
        }
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
        withContext(Dispatchers.IO) {
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
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    private fun getExtras(
        contactId: Long,
        valueIndex: String,
        typeIndex: String?,
        itemType: String
    ): List<ValueWithType> {
        val entries = mutableListOf<ValueWithType>()
        val projection = arrayOf(Data.CONTACT_ID, valueIndex, typeIndex ?: "data2")

        contentResolver.query(
            contentUri,
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
        withContext(Dispatchers.IO) {
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
    }

    override suspend fun setFavorite(contact: ContactData, favorite: Boolean): Unit =
        withContext(Dispatchers.IO) {
            val op = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI).apply {
                val selection = "${RawContacts.CONTACT_ID} = ?"
                val selectionArgs = arrayOf(contact.rawContactId.toString())
                withSelection(selection, selectionArgs)
                withValue(Contacts.STARRED, if (contact.favorite) 1 else 0)
            }.build()

            contentResolver.applyBatch(AUTHORITY, arrayListOf(op))
        }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.WRITE_CONTACTS)
    override suspend fun createContact(contact: ContactData) {
        withContext(Dispatchers.IO) {
            val lastChosenAccount = Preferences.getLastChosenAccount()
            val ops = listOfNotNull(
                getCreateAction(
                    contact.accountType ?: lastChosenAccount.type,
                    contact.accountName ?: lastChosenAccount.name
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
                contact.photo?.let {
                    getInsertAction(Photo.CONTENT_ITEM_TYPE, Photo.PHOTO, getBitmapBytes(it))
                },
                *contact.groups.map {
                    getInsertAction(
                        GroupMembership.CONTENT_ITEM_TYPE,
                        GroupMembership.GROUP_ROW_ID,
                        it.rowId.toString()
                    )
                }.toTypedArray(),
                *ContactsHelper.contactAttributesTypes.filterIsInstance<StringAttribute>()
                    .map { attribute ->
                        attribute.get(contact)?.let {
                            getInsertAction(
                                attribute.androidContentType,
                                attribute.androidValueColumn,
                                it
                            )
                        }
                    }.toTypedArray(),
                *ContactsHelper.contactAttributesTypes.filterIsInstance<ListAttribute>()
                    .map { attribute ->
                        attribute.get(contact).map {
                            getInsertAction(
                                attribute.androidContentType,
                                attribute.androidValueColumn,
                                it.value,
                                attribute.androidTypeColumn,
                                it.type
                            )
                        }
                    }.flatten().toTypedArray()
            ).let { ArrayList(it) }

            contentResolver.applyBatch(AUTHORITY, ops)
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_CONTACTS)
    override suspend fun updateContact(contact: ContactData) {
        withContext(Dispatchers.IO) {
            val operations = ArrayList<ContentProviderOperation>()
            val rawContactId = contact.rawContactId.toString()

            val selection = "${Data.RAW_CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?"
            ContentProviderOperation.newUpdate(contentUri).apply {
                val selectionArgs = arrayOf(rawContactId, StructuredName.CONTENT_ITEM_TYPE)
                withSelection(selection, selectionArgs)
                withValue(StructuredName.GIVEN_NAME, contact.firstName)
                withValue(StructuredName.FAMILY_NAME, contact.surName)
                withValue(StructuredName.DISPLAY_NAME, contact.displayName)
                operations.add(build())
            }

            for (attribute in ContactsHelper.contactAttributesTypes) {
                if (attribute is StringAttribute) {
                    operations.addAll(
                        getUpdateSingleAction(
                            rawContactId, attribute.androidContentType,
                            attribute.androidValueColumn, attribute.get(contact)
                        )
                    )
                } else if (attribute is ListAttribute) {
                    operations.addAll(
                        getUpdateMultipleAction(
                            rawContactId, attribute.androidContentType, attribute.get(contact),
                            attribute.androidValueColumn, attribute.androidTypeColumn
                        )
                    )
                }
            }

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
    }

    fun getAccountTypes(): List<AccountType> {
        val accounts = AccountManager.get(context).accounts.filter {
            ContentResolver.getIsSyncable(it, AUTHORITY) > 0
                    && ContentResolver.getSyncAutomatically(it, AUTHORITY)
        }

        return listOf(AccountType.androidDefault) + accounts.map { AccountType(it.name, it.type) }
    }

    private fun getCreateAction(
        accountType: String,
        accountName: String
    ): ContentProviderOperation {
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
        return ContentProviderOperation.newInsert(contentUri)
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

        ContentProviderOperation.newDelete(contentUri).apply {
            withSelection(selection, selectionArgs)
            operations.add(build())
        }

        // add new entries
        entries.forEach {
            ContentProviderOperation.newInsert(contentUri).apply {
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
        return ContentProviderOperation.newDelete(contentUri).apply {
            val selection = "${Data.RAW_CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?"
            val selectionArgs = arrayOf(rawContactId.toString(), Photo.CONTENT_ITEM_TYPE)
            withSelection(selection, selectionArgs)
        }.build()
    }

    suspend fun updateContactRingTone(contactId: String, ringtoneUri: Uri): Unit =
        withContext(Dispatchers.IO) {
            val contactUri = Uri.withAppendedPath(Contacts.CONTENT_URI, contactId)

            val op = ContentProviderOperation.newUpdate(contactUri)
                .withValue(Contacts.CUSTOM_RINGTONE, ringtoneUri.toString())
                .build()

            contentResolver.applyBatch(AUTHORITY, arrayListOf(op))
        }

    companion object {
        const val MAX_PHOTO_SIZE = 700f
    }
}
