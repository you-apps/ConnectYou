package com.bnyro.contacts.ui.models

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.bnyro.contacts.util.LocalContactsHelper
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ContactsModel : ViewModel() {
    var contacts = mutableStateListOf<ContactData>()
    var isLoading by mutableStateOf(true)
    var contactsHelper by mutableStateOf<ContactsHelper?>(null)
    private val permissions = arrayOf(
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CONTACTS
    )
    var initialContactId: Long? by mutableStateOf(null)
    var initialContactData: ContactData? by mutableStateOf(null)

    fun init(context: Context) {
        contactsHelper = when (Preferences.getInt(Preferences.homeTabKey, 0)) {
            0 -> DeviceContactsHelper(context)
            else -> LocalContactsHelper(context)
        }
    }

    fun loadContacts(context: Context) {
        isLoading = true
        if (contactsHelper is DeviceContactsHelper &&
            !PermissionHelper.checkPermissions(context, permissions)
        ) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                val ct = contactsHelper?.getContactList().orEmpty()
                contacts.clear()
                contacts.addAll(ct)
            } catch (e: Exception) {
                return@launch
            }
            isLoading = false
            CoroutineScope(Dispatchers.IO).launch {
                contacts.map {
                    async {
                        contactsHelper?.loadAdvancedData(it)
                    }
                }.awaitAll()
            }
        }
    }

    private suspend fun deleteContactsSuspend(contactsToDelete: List<ContactData>) {
        contactsHelper?.deleteContacts(contactsToDelete)
        contacts.removeAll { ct ->
            contactsToDelete.any {
                it.contactId == ct.contactId
            }
        }
    }

    fun deleteContacts(contactsToDelete: List<ContactData>) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteContactsSuspend(contactsToDelete)
        }
    }

    fun createContact(context: Context, contact: ContactData) {
        viewModelScope.launch(Dispatchers.IO) {
            contactsHelper?.createContact(contact)
            loadContacts(context)
        }
    }

    fun updateContact(context: Context, contact: ContactData) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            copyContactsSuspend(context, contacts)
        }
    }

    fun moveContacts(context: Context, contacts: List<ContactData>) {
        viewModelScope.launch(Dispatchers.IO) {
            copyContactsSuspend(context, contacts)
            deleteContactsSuspend(contacts)
        }
    }

    suspend fun loadAdvancedContactData(contact: ContactData): ContactData {
        return contactsHelper?.loadAdvancedData(contact) ?: contact
    }

    fun importVcf(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val exportHelper = ExportHelper(context, contactsHelper!!)
            exportHelper.importContacts(uri)
            context.toast(R.string.import_success)
            loadContacts(context)
        }
    }

    fun exportVcf(context: Context, uri: Uri) {
        val exportHelper = ExportHelper(context, contactsHelper!!)
        exportHelper.exportContacts(uri, contacts)
        context.toast(R.string.export_success)
    }

    fun exportSingleVcf(context: Context, contact: ContactData) {
        viewModelScope.launch(Dispatchers.IO) {
            val exportHelper = ExportHelper(context, contactsHelper!!)
            val tempFileUri = exportHelper.exportTempContact(contact, true)
            IntentHelper.shareContactVcf(context, tempFileUri)
        }
    }

    fun getAvailableAccountTypes() = getAvailableAccounts().map { it.first }

    fun getAvailableAccounts(): List<Pair<String, String>> {
        if (contacts.isEmpty()) {
            return listOf(
                DeviceContactsHelper.ANDROID_ACCOUNT_TYPE to DeviceContactsHelper.ANDROID_CONTACTS_NAME
            )
        }
        return contacts.mapNotNull {
            it.accountType?.let { type -> type to it.accountName.orEmpty() }
        }.distinct().toMutableList()
    }

    fun getAvailableGroups() = contacts.map { it.groups }.flatten().distinct()
}
