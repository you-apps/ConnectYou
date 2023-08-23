package com.bnyro.contacts.ui.models

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bnyro.contacts.App
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.ContactsSource
import com.bnyro.contacts.ext.toast
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.util.ContactsRepository
import com.bnyro.contacts.util.DeviceContactsRepository
import com.bnyro.contacts.util.ExportHelper
import com.bnyro.contacts.util.IntentHelper
import com.bnyro.contacts.util.LocalContactsRepository
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ContactsModel(
    private val localContactsRepository: LocalContactsRepository,
    private val deviceContactsRepository: DeviceContactsRepository
) : ViewModel() {
    var contacts by mutableStateOf(listOf<ContactData>())
    var isLoading by mutableStateOf(true)
    var contactsSource by mutableStateOf(
        ContactsSource.values().getOrNull(
            Preferences.getInt(
                Preferences.homeTabKey,
                0
            )
        ) ?: ContactsSource.DEVICE
    )
    val contactsRepository: ContactsRepository
        get() = when (contactsSource) {
            ContactsSource.LOCAL -> localContactsRepository
            ContactsSource.DEVICE -> deviceContactsRepository
        }
    private val permissions = arrayOf(
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CONTACTS
    )
    var initialContactId: Long? by mutableStateOf(null)
    var initialContactData: ContactData? by mutableStateOf(null)

    fun loadContacts(context: Context) {
        isLoading = true
        if (contactsSource == ContactsSource.DEVICE &&
            !PermissionHelper.checkPermissions(context, permissions)
        ) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                contacts = contactsRepository.getContactList().toMutableStateList()
            } catch (e: Exception) {
                return@launch
            }
            isLoading = false
            CoroutineScope(Dispatchers.IO).launch {
                contacts.map {
                    async {
                        contactsRepository.loadAdvancedData(it)
                    }
                }.awaitAll()
            }
        }
    }

    fun deleteContacts(contactsToDelete: List<ContactData>) {
        viewModelScope.launch(Dispatchers.IO) {
            contactsRepository.deleteContacts(contactsToDelete)
            contacts = contacts.minus(contactsToDelete.toSet())
        }
    }

    fun createContact(context: Context, contact: ContactData) {
        viewModelScope.launch(Dispatchers.IO) {
            contactsRepository.createContact(contact)
            loadContacts(context)
        }
    }

    fun updateContact(context: Context, contact: ContactData) {
        viewModelScope.launch(Dispatchers.IO) {
            contactsRepository.updateContact(contact)
            loadContacts(context)
        }
    }

    fun copyContacts(context: Context, contacts: List<ContactData>) {
        viewModelScope.launch(Dispatchers.IO) {
            contacts.forEach { contact ->
                val fullContact = loadAdvancedContactData(contact)
                val otherHelper = when (contactsRepository) {
                    is DeviceContactsRepository -> (context.applicationContext as App).localContactsRepository
                    else -> (context.applicationContext as App).deviceContactsRepository
                }
                otherHelper.createContact(fullContact)
            }
        }
    }

    fun moveContacts(context: Context, contacts: List<ContactData>) {
        copyContacts(context, contacts)
        deleteContacts(contacts)
    }

    suspend fun loadAdvancedContactData(contact: ContactData): ContactData {
        return contactsRepository.loadAdvancedData(contact)
    }

    fun importVcf(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val exportHelper = ExportHelper(context, contactsRepository)
            exportHelper.importContacts(uri)
            context.toast(R.string.import_success)
            loadContacts(context)
        }
    }

    fun exportVcf(context: Context, uri: Uri) {
        val exportHelper = ExportHelper(context, contactsRepository)
        exportHelper.exportContacts(uri, contacts)
        context.toast(R.string.export_success)
    }

    fun exportSingleVcf(context: Context, contact: ContactData) {
        viewModelScope.launch(Dispatchers.IO) {
            val exportHelper = ExportHelper(context, contactsRepository)
            val tempFileUri = exportHelper.exportTempContact(contact, true)
            IntentHelper.shareContactVcf(context, tempFileUri)
        }
    }

    /**
     * Returns a list of account type to account name
     */
    fun getAvailableAccounts(): List<Pair<String, String>> {
        if (contacts.isEmpty()) {
            return listOf(
                DeviceContactsRepository.ANDROID_ACCOUNT_TYPE to DeviceContactsRepository.ANDROID_CONTACTS_NAME
            )
        }
        return contacts.mapNotNull {
            it.accountType?.let { type -> type to it.accountName.orEmpty() }
        }.distinct().toMutableList()
    }

    fun getAvailableGroups() = contacts.map { it.groups }.flatten().distinct()

    fun getContactByNumber(number: String): ContactData? {
        val normalizedNumber = number.replace(normalizeNumberRegex, "")
        return contacts.firstOrNull {
            it.numbers.any { (value, _) ->
                value.replace(normalizeNumberRegex, "") == normalizedNumber
            }
        }
    }

    companion object {
        val normalizeNumberRegex = Regex("[-_ ]")

        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as App
                ContactsModel(
                    application.localContactsRepository,
                    application.deviceContactsRepository
                )
            }
        }
    }
}
