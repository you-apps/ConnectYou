package com.bnyro.contacts.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import androidx.annotation.RequiresPermission
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.bnyro.contacts.obj.ContactData

class ContactsHelper(context: Context) {
    private val contentResolver = context.contentResolver
    private var cursor: Cursor? = null

    private val projection = arrayOf(
        CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        CommonDataKinds.StructuredName.GIVEN_NAME,
        CommonDataKinds.StructuredName.FAMILY_NAME,
        CommonDataKinds.Phone.NUMBER
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
                            )) {
                            contactList[contactIndex].phoneNumber += it
                        }
                    }
                    continue
                }
                val contact = ContactData(
                    contactId = contactId,
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
