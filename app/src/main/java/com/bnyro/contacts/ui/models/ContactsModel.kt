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
    private var contactsHelper: ContactsHelper? = null

    @SuppressLint("MissingPermission")
    fun loadContacts(context: Context) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.READ_CONTACTS)) return
        viewModelScope.launch {
            contactsHelper = ContactsHelper(context)
            contacts = contactsHelper!!.getContactList()
        }
    }

    @SuppressLint("MissingPermission")
    fun deleteContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.READ_CONTACTS)) return
        viewModelScope.launch {
            contactsHelper?.deleteContacts(listOf(contact))
            contacts = contacts?.minus(contact)
        }
    }

    @Suppress("MissingPermission")
    fun createContact(context: Context, contact: ContactData) {
        if (!PermissionHelper.checkPermissions(context, Manifest.permission.WRITE_CONTACTS)) return
        viewModelScope.launch {
            contactsHelper?.createContact(contact)
            contacts = contacts?.plus(contact)
        }
    }

    fun loadAdvancedContactData(contact: ContactData): ContactData {
        contactsHelper ?: return contact
        return contactsHelper!!.loadAdvancedData(contact)
    }
}
