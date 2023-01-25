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
        viewModelScope.launch {
            contactsHelper = ContactsHelper(context)
            if (!PermissionHelper.checkPermissions(context, Manifest.permission.READ_CONTACTS)) return@launch
            contacts = contactsHelper!!.getContactList()
        }
    }

    @SuppressLint("MissingPermission")
    fun deleteContact(context: Context, contact: ContactData) {
        viewModelScope.launch {
            if (!PermissionHelper.checkPermissions(context, Manifest.permission.READ_CONTACTS)) return@launch
            contactsHelper?.deleteContacts(listOf(contact))
            contacts = contacts?.minus(contact)
        }
    }
}
