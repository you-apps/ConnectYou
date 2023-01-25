package com.bnyro.contacts.ui.models

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.contacts.MainActivity
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.util.ContactsHelper
import kotlinx.coroutines.launch

class ContactsModel : ViewModel() {
    var contacts by mutableStateOf<List<ContactData>?>(null)
    var contactsHelper: ContactsHelper? = null

    fun loadContacts(context: Context) {
        viewModelScope.launch {
            contactsHelper = ContactsHelper(context)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as MainActivity,
                    arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                    1
                )
                return@launch
            }
            contacts = contactsHelper!!.getContactList()
        }
    }

    fun deleteContact(contact: ContactData) {
        contactsHelper?.deleteContacts(listOf(contact))
        contacts = contacts?.minus(contact)
    }
}
