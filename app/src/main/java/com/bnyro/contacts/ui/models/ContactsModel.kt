package com.bnyro.contacts.ui.models

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.ext.toast
import com.bnyro.contacts.ext.withIO
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.DeviceContactsHelper
import com.bnyro.contacts.util.ExportHelper
import com.bnyro.contacts.util.IntentHelper
import com.bnyro.contacts.util.LocalContactsHelper
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.Preferences

class ContactsModel : ViewModel() {
    var contacts by mutableStateOf<List<ContactData>?>(null)
    var contactsHelper by mutableStateOf<ContactsHelper?>(null)

    fun init(context: Context) {
        contactsHelper = when (Preferences.getInt(Preferences.homeTabKey, 0)) {
            0 -> DeviceContactsHelper(context)
            else -> LocalContactsHelper(context)
        }
    }

    @SuppressLint("MissingPermission")
    fun loadContacts(context: Context) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.READ_CONTACTS)) return
        withIO {
            contacts = contactsHelper?.getContactList()
        }
    }

    @SuppressLint("MissingPermission")
    fun deleteContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.READ_CONTACTS)) return
        withIO {
            contactsHelper?.deleteContacts(listOf(contact))
            contacts = contacts?.filter { it.contactId != contact.contactId }
        }
    }

    @Suppress("MissingPermission")
    fun createContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.WRITE_CONTACTS)) return
        withIO {
            contactsHelper?.createContact(contact)
            loadContacts(context)
        }
    }

    @Suppress("MissingPermission")
    fun updateContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.WRITE_CONTACTS)) return
        withIO {
            contactsHelper?.updateContact(contact)
            loadContacts(context)
        }
    }

    fun copyContact(context: Context, contact: ContactData) {
        withIO {
            val fullContact = loadAdvancedContactData(contact)
            val otherHelper = when (contactsHelper) {
                is DeviceContactsHelper -> LocalContactsHelper(context)
                else -> DeviceContactsHelper(context)
            }
            otherHelper.createContact(fullContact)
        }
    }

    fun moveContact(context: Context, contact: ContactData) {
        copyContact(context, contact)
        deleteContact(context, contact)
    }

    suspend fun loadAdvancedContactData(contact: ContactData): ContactData {
        return contactsHelper?.loadAdvancedData(contact) ?: contact
    }

    fun importVcf(context: Context, uri: Uri) {
        withIO {
            val exportHelper = ExportHelper(context, contactsHelper!!)
            exportHelper.importContacts(uri)
            context.toast(R.string.import_success)
            loadContacts(context)
        }
    }

    fun exportVcf(context: Context, uri: Uri) {
        val exportHelper = ExportHelper(context, contactsHelper!!)
        exportHelper.exportContacts(uri, contacts.orEmpty())
        context.toast(R.string.export_success)
    }

    fun exportSingleVcf(context: Context, contact: ContactData) {
        withIO {
            val exportHelper = ExportHelper(context, contactsHelper!!)
            val tempFileUri = exportHelper.exportContact(contact)
            IntentHelper.shareContactVcf(context, tempFileUri)
        }
    }
}
