package com.bnyro.contacts.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.ContactsContract.AUTHORITY
import android.provider.ContactsContract.CommonDataKinds
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.RawContacts
import androidx.annotation.RequiresPermission
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.bnyro.contacts.obj.ContactData

class ContactsHelper(private val context: Context) {
    private val contentResolver = context.contentResolver
    private var cursor: Cursor? = null
    private val ANDROID_ACCOUNT_TYPE = "com.android.contacts"

    private val projection = arrayOf(
        Phone.CONTACT_ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        StructuredName.GIVEN_NAME,
        StructuredName.FAMILY_NAME,
        RawContacts.ACCOUNT_TYPE
    )

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun getContactList(): List<ContactData> {
        val contactList = mutableListOf<ContactData>()

        @Suppress("SameParameterValue")
        cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            null,
            null,
            Phone.DISPLAY_NAME + " ASC"
        ) ?: return contactList

        cursor?.use {
            while (it.moveToNext()) {
                val contactId = getLong(Phone.CONTACT_ID)!!

                // check whether already in the list
                val contactIndex = contactList.indexOfFirst {
                    it.contactId == contactId
                }.takeIf { it >= 0 }
                if (contactIndex != null) {
                    continue
                }

                val contact = ContactData(
                    contactId = contactId,
                    accountType = getString(RawContacts.ACCOUNT_TYPE),
                    displayName = getString(ContactsContract.Contacts.DISPLAY_NAME),
                    firstName = getString(StructuredName.GIVEN_NAME),
                    surName = getString(StructuredName.FAMILY_NAME)
                )
                contactList.add(contact)
            }
        }

        return contactList
    }

    @RequiresPermission(Manifest.permission.WRITE_CONTACTS)
    fun deleteContacts(contacts: List<ContactData>) {
        val operations = ArrayList<ContentProviderOperation>()
        val selection = "${RawContacts._ID} = ?"
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
            getCreateAction(contact.displayName.orEmpty()),
            getInsertAction(StructuredName.CONTENT_ITEM_TYPE, StructuredName.DISPLAY_NAME, contact.displayName.orEmpty()),
            getInsertAction(StructuredName.CONTENT_ITEM_TYPE, StructuredName.GIVEN_NAME, contact.firstName.orEmpty()),
            getInsertAction(StructuredName.CONTENT_ITEM_TYPE, StructuredName.FAMILY_NAME, contact.surName.orEmpty()),
            *contact.phoneNumber.map { getInsertAction(Phone.CONTENT_ITEM_TYPE, Phone.NUMBER, it, Phone.TYPE, Phone.TYPE_MOBILE) }.toTypedArray(),
            *contact.emails.map { getInsertAction(Email.CONTENT_ITEM_TYPE, Email.ADDRESS, it, Email.TYPE, Email.TYPE_HOME) }.toTypedArray(),
            *contact.addresses.map { getInsertAction(StructuredPostal.CONTENT_ITEM_TYPE, StructuredPostal.FORMATTED_ADDRESS, it, StructuredPostal.TYPE, StructuredPostal.TYPE_HOME) }.toTypedArray(),
            *contact.events.map { getInsertAction(Event.CONTENT_ITEM_TYPE, Event.START_DATE, it, Event.TYPE, Event.TYPE_BIRTHDAY) }.toTypedArray()
        )

        contentResolver.applyBatch(AUTHORITY, ops)
    }

    private fun getCreateAction(accountName: String): ContentProviderOperation {
        return ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
            .withValue(RawContacts.ACCOUNT_TYPE, ANDROID_ACCOUNT_TYPE)
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

    @SuppressLint("Range")
    private fun getString(index: String): String? {
        return cursor?.getStringOrNull(cursor!!.getColumnIndex(index))
    }

    @Suppress("SameParameterValue")
    @SuppressLint("Range")
    private fun getLong(index: String): Long? {
        return cursor?.getLongOrNull(cursor!!.getColumnIndex(index))
    }
}
