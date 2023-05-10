package com.bnyro.contacts.ui.models

import android.Manifest
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
    private val permissions = arrayOf(
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CONTACTS
    )

    fun init(context: Context) {
        contactsHelper = when (Preferences.getInt(Preferences.homeTabKey, 0)) {
            0 -> DeviceContactsHelper(context)
            else -> LocalContactsHelper(context)
        }
    }

    fun loadContacts(context: Context) {
        if (!PermissionHelper.checkPermissions(context, permissions)) return
        withIO {
            contacts = contactsHelper?.getContactList()
        }
    }

    private suspend fun deleteContactsSuspend(contactsToDelete: List<ContactData>) {
        contactsHelper?.deleteContacts(contactsToDelete)
        contacts = contacts?.filter { ct ->
            contactsToDelete.none {
                it.contactId == ct.contactId
            }
        }
    }

    fun deleteContacts(contactsToDelete: List<ContactData>) {
        withIO {
            deleteContactsSuspend(contactsToDelete)
        }
    }

    fun createContact(context: Context, contact: ContactData) {
        withIO {
            contactsHelper?.createContact(contact)
            loadContacts(context)
        }
    }

    fun updateContact(context: Context, contact: ContactData) {
        withIO {
            contactsHelper?.updateContact(contact)
            loadContacts(context)
        }
    }

    private suspend fun copyContactsSuspend(context: Context, contacts: List<ContactData>) {
        contacts.forEach { contact ->
            val fullContact = loadAdvancedContactData(contact)
            val otherHelper = when (contactsHelper) {
                is DeviceContactsHelper -> LocalContactsHelper(context)
                else -> DeviceContactsHelper(context)
            }
            otherHelper.createContact(fullContact)
        }
    }

    fun copyContacts(context: Context, contacts: List<ContactData>) {
        withIO {
            copyContactsSuspend(context, contacts)
        }
    }

    fun moveContacts(context: Context, contacts: List<ContactData>) {
        withIO {
            copyContactsSuspend(context, contacts)
            deleteContactsSuspend(contacts)
        }
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

    fun getAvailableAccountTypes() = contacts.orEmpty().mapNotNull {
        it.accountType
    }.distinct()

    fun getAvailableAccountNames() = contacts.orEmpty().mapNotNull { it.accountName }.distinct()

    fun getAvailableAccountTypesAndNames() = contacts.orEmpty().mapNotNull {
        it.accountType?.let { type -> type to it.accountName }
    }.distinct().toMutableList()

    fun getAvailableGroups() = contacts?.map { it.groups }?.flatten().orEmpty().distinct()
}
