package com.bnyro.contacts.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.ContactsContract.AUTHORITY
import android.provider.ContactsContract.CommonDataKinds
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
        CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        CommonDataKinds.StructuredName.GIVEN_NAME,
        CommonDataKinds.StructuredName.FAMILY_NAME,
        CommonDataKinds.Phone.NUMBER,
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
            CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        ) ?: return contactList

        cursor?.use {
            while (it.moveToNext()) {
                val contactId = getLong(CommonDataKinds.Phone.CONTACT_ID)!!

                // check whether already in the list
                val contactIndex = contactList.indexOfFirst {
                    it.contactId == contactId
                }.takeIf { it >= 0 }
                if (contactIndex != null) {
                    getString(CommonDataKinds.Phone.NUMBER)?.let {
                        if (!contactList[contactIndex].phoneNumber.contains(it) && TextUtils.isPhoneNumber(
                                it
                            )
                        ) {
                            contactList[contactIndex].phoneNumber += it
                        }
                    }
                    continue
                }
                val contact = ContactData(
                    contactId = contactId,
                    accountType = getString(RawContacts.ACCOUNT_TYPE),
                    displayName = getString(ContactsContract.Contacts.DISPLAY_NAME),
                    givenName = getString(CommonDataKinds.StructuredName.GIVEN_NAME),
                    familyName = getString(CommonDataKinds.StructuredName.FAMILY_NAME)
                )
                getString(CommonDataKinds.Phone.NUMBER)?.let {
                    @Suppress("SameParameterValue")
                    if (TextUtils.isPhoneNumber(it)) contact.phoneNumber = listOf(it)
                }
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
        val ops = arrayListOf<ContentProviderOperation>()

        var op: ContentProviderOperation.Builder =
            ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, ANDROID_ACCOUNT_TYPE)
                .withValue(RawContacts.ACCOUNT_NAME, contact.displayName)

        ops.add(op.build())

        // Creates the display name for the new raw contact, as a StructuredName data row.
        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(
                ContactsContract.Data.MIMETYPE,
                CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, contact.displayName)

        ops.add(op.build())

        // Inserts the specified phone number and type as a Phone data row
        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(CommonDataKinds.Phone.NUMBER, contact.phoneNumber)
            .withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)

        ops.add(op.build())

        // Inserts the specified email and type as a Phone data row
        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
            .withValue(CommonDataKinds.Email.ADDRESS, contact.emails.firstOrNull())
            .withValue(CommonDataKinds.Email.TYPE, CommonDataKinds.Email.TYPE_HOME)

        op.withYieldAllowed(true)

        ops.add(op.build())

        contentResolver.applyBatch(AUTHORITY, ops)
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
