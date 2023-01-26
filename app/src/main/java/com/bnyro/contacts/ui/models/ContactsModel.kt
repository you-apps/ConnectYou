package com.bnyro.contacts.ui.models

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.PermissionHelper
import kotlinx.coroutines.launch

class ContactsModel : ViewModel() {
    var contacts by mutableStateOf<List<ContactData>?>(null)

    @SuppressLint("MissingPermission")
    fun loadContacts(context: Context) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.READ_CONTACTS)) return
        viewModelScope.launch {
            val contactsHelper = ContactsHelper(context)
            contacts = contactsHelper.getContactList()
        }
    }

    @SuppressLint("MissingPermission")
    fun deleteContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.READ_CONTACTS)) return
        viewModelScope.launch {
            val contactsHelper = ContactsHelper(context)
            contactsHelper.deleteContacts(listOf(contact))
            contacts = contacts?.filter { it.contactId != contact.contactId }
        }
    }

    @Suppress("MissingPermission")
    fun createContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.WRITE_CONTACTS)) return
        viewModelScope.launch {
            val contactsHelper = ContactsHelper(context)
            contactsHelper.createContact(contact)
            loadContacts(context)
        }
    }

    @Suppress("MissingPermission")
    fun updateContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.WRITE_CONTACTS)) return
        val contactsHelper = ContactsHelper(context)
        contactsHelper.deleteContacts(listOf(contact))
        contactsHelper.createContact(contact)
    }

    fun loadAdvancedContactData(context: Context, contact: ContactData): ContactData {
        val contactsHelper = ContactsHelper(context)
        return contactsHelper.loadAdvancedData(contact)
    }
}
