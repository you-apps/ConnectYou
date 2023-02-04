package com.bnyro.contacts.ui.models

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.contacts.R
import com.bnyro.contacts.ext.toast
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.DeviceContactsHelper
import com.bnyro.contacts.util.ExportHelper
import com.bnyro.contacts.util.IntentHelper
import com.bnyro.contacts.util.PermissionHelper
import kotlinx.coroutines.launch

class ContactsModel : ViewModel() {
    var contacts by mutableStateOf<List<ContactData>?>(null)
    var contactsHelper by mutableStateOf<ContactsHelper?>(null)

    fun init(context: Context) {
        contactsHelper = DeviceContactsHelper(context)
    }

    @SuppressLint("MissingPermission")
    fun loadContacts(context: Context) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.READ_CONTACTS)) return
        viewModelScope.launch {
            contacts = contactsHelper?.getContactList()
        }
    }

    @SuppressLint("MissingPermission")
    fun deleteContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.READ_CONTACTS)) return
        viewModelScope.launch {
            contactsHelper?.deleteContacts(listOf(contact))
            contacts = contacts?.filter { it.contactId != contact.contactId }
        }
    }

    @Suppress("MissingPermission")
    fun createContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.WRITE_CONTACTS)) return
        viewModelScope.launch {
            contactsHelper?.createContact(contact)
            loadContacts(context)
        }
    }

    @Suppress("MissingPermission")
    fun updateContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.WRITE_CONTACTS)) return
        viewModelScope.launch {
            contactsHelper?.deleteContacts(listOf(contact))
            contactsHelper?.createContact(contact)
        }
    }

    suspend fun loadAdvancedContactData(contact: ContactData): ContactData {
        return contactsHelper?.loadAdvancedData(contact) ?: contact
    }

    fun importVcf(context: Context, uri: Uri) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            val exportHelper = ExportHelper(context, contactsHelper!!)
            val tempFileUri = exportHelper.exportContact(contact)
            IntentHelper.shareContactVcf(context, tempFileUri)
        }
    }
}
