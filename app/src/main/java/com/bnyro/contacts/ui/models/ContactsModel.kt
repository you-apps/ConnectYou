package com.bnyro.contacts.ui.models

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
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.DeviceContactsHelper
import com.bnyro.contacts.util.ExportHelper
import com.bnyro.contacts.util.IntentHelper
import com.bnyro.contacts.util.LocalContactsHelper
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

    fun loadContacts() {
        withIO {
            contacts = contactsHelper?.getContactList()
        }
    }

    fun deleteContacts(context: Context, contactsToDelete: List<ContactData>) {
        withIO {
            contactsHelper?.deleteContacts(contactsToDelete)
            contacts = contacts?.filter { ct -> contactsToDelete.none {
                it.contactId == ct.contactId
            } }
            autoBackup(context)
        }
    }

    fun createContact(context: Context, contact: ContactData) {
        withIO {
            contactsHelper?.createContact(contact)
            loadContacts()
            autoBackup(context)
        }
    }

    fun updateContact(context: Context, contact: ContactData) {
        withIO {
            contactsHelper?.updateContact(contact)
            loadContacts()
            autoBackup(context)
        }
    }

    fun copyContacts(context: Context, contacts: List<ContactData>) {
        withIO {
            contacts.forEach { contact ->
                val fullContact = loadAdvancedContactData(contact)
                val otherHelper = when (contactsHelper) {
                    is DeviceContactsHelper -> LocalContactsHelper(context)
                    else -> DeviceContactsHelper(context)
                }
                otherHelper.createContact(fullContact)
            }
            autoBackup(context)
        }
    }

    fun moveContacts(context: Context, contacts: List<ContactData>) {
        copyContacts(context, contacts)
        deleteContacts(context, contacts)
        withIO {
            autoBackup(context)
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
            loadContacts()
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

    private suspend fun autoBackup(context: Context) {
        contactsHelper?.let {
            if (it.isAutoBackupEnabled()) BackupHelper.backup(context, it)
        }
    }
}
