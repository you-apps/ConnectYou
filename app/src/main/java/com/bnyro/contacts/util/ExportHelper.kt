package com.bnyro.contacts.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import com.bnyro.contacts.ext.pmap
import com.bnyro.contacts.obj.ContactData

class ExportHelper(private val context: Context) {
    private val contactsHelper = ContactsHelper(context)
    private val contentResolver = context.contentResolver

    @SuppressLint("MissingPermission")
    fun importContacts(uri: Uri) {
        contentResolver.openInputStream(uri)?.use {
            val content = it.bufferedReader().readText()
            val contacts = VcardHelper.importVcard(content)
            contacts.forEach { contact ->
                contactsHelper.createContact(contact)
            }
        }
    }

    fun exportContacts(uri: Uri, minimalContacts: List<ContactData>) {
        val contacts = minimalContacts.pmap { contactsHelper.loadAdvancedData(it) }
        val vCardText = VcardHelper.exportVcard(contacts)
        Log.e("vCard", vCardText)
        contentResolver.openOutputStream(uri)?.use {
            it.write(vCardText.toByteArray())
        }
    }
}
