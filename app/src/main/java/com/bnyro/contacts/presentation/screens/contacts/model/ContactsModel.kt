package com.bnyro.contacts.presentation.screens.contacts.model

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bnyro.contacts.App
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.ContactsSource
import com.bnyro.contacts.domain.model.AccountType
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.repositories.ContactsRepository
import com.bnyro.contacts.domain.repositories.DeviceContactsRepository
import com.bnyro.contacts.domain.repositories.LocalContactsRepository
import com.bnyro.contacts.presentation.screens.contacts.model.state.ContactListState
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.ExportHelper
import com.bnyro.contacts.util.IntentHelper
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.util.extension.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContactsModel(
    context: Context,
    private val localContactsRepository: LocalContactsRepository,
    private val deviceContactsRepository: DeviceContactsRepository
) : ViewModel() {
    var contactsSource by mutableStateOf(
        ContactsSource.values().getOrNull(
            Preferences.getInt(Preferences.selectedContactsRepo, 0)
        ) ?: ContactsSource.DEVICE
    )
    val contactsRepository: ContactsRepository
        get() = when (contactsSource) {
            ContactsSource.LOCAL -> localContactsRepository
            ContactsSource.DEVICE -> deviceContactsRepository
        }
    var initialContactId: Long? = null
    var initialContactData: ContactData? by mutableStateOf(null)

    var localContacts: ContactListState by mutableStateOf(ContactListState.Loading)
        private set

    var deviceContacts: ContactListState by mutableStateOf(ContactListState.Loading)
        private set

    var contacts: List<ContactData>
        get() = when (contactsSource) {
            ContactsSource.LOCAL -> (localContacts as? ContactListState.Success)?.contacts.orEmpty()
            ContactsSource.DEVICE -> (deviceContacts as? ContactListState.Success)?.contacts.orEmpty()
        }
        private set(value) {
            when (contactsSource) {
                ContactsSource.LOCAL -> localContacts = ContactListState.Success(value)
                ContactsSource.DEVICE -> deviceContacts = ContactListState.Success(value)
            }
        }

    init {
        loadContacts(context)

        initialContactId?.let {
            setInitialContactID(it, context)
        }
    }

    private suspend inline fun getLocalContacts() {
        localContacts = try {
            localContactsRepository.getContactList().takeIf { it.isNotEmpty() }?.let {
                ContactListState.Success(it)
            } ?: ContactListState.Empty
        } catch (_: Exception) {
            ContactListState.Error
        }
    }

    @SuppressLint("MissingPermission")
    private suspend inline fun getDeviceContacts(context: Context) {
        deviceContacts = ContactListState.Loading
        while (!PermissionHelper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            deviceContacts = ContactListState.Error
            delay(500)
        }
        deviceContacts = try {
            deviceContactsRepository.getContactList().takeIf { it.isNotEmpty() }?.let {
                ContactListState.Success(it)
            } ?: ContactListState.Empty
        } catch (_: Exception) {
            ContactListState.Error
        }

        (deviceContacts as? ContactListState.Success)?.contacts?.let {
            viewModelScope.launch {
                it.map {
                    async {
                        deviceContactsRepository.loadAdvancedData(it)
                    }
                }.awaitAll()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun setInitialContactID(id: Long, context: Context) {
        if (!PermissionHelper.hasPermission(context, Manifest.permission.READ_CONTACTS)) return
        contacts.firstOrNull {
            it.contactId == id
        }?.let {
            viewModelScope.launch {
                initialContactData = deviceContactsRepository.loadAdvancedData(it)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadContacts(context: Context) {
        viewModelScope.launch {
            getDeviceContacts(context)
        }
        viewModelScope.launch {
            getLocalContacts()
        }
    }

    fun deleteContacts(contactsToDelete: List<ContactData>) {
        viewModelScope.launch {
            contactsRepository.deleteContacts(contactsToDelete)
            contacts = contacts.minus(contactsToDelete.toSet())
        }
    }

    fun createContact(context: Context, contact: ContactData) {
        viewModelScope.launch {
            contactsRepository.createContact(contact)
            loadContacts(context)
        }
    }

    fun updateContact(context: Context, contact: ContactData) {
        viewModelScope.launch {
            contactsRepository.updateContact(contact)
            loadContacts(context)
        }
    }

    fun setFavorite(context: Context, contact: ContactData, favorite: Boolean) {
        viewModelScope.launch {
            contactsRepository.setFavorite(contact, favorite)
            loadContacts(context)
        }
    }

    fun copyContacts(context: Context, contacts: List<ContactData>) {
        val otherHelper = when (contactsRepository) {
            is DeviceContactsRepository -> localContactsRepository
            else -> deviceContactsRepository
        }
        viewModelScope.launch {
            contacts.forEach { contact ->
                val fullContact = loadAdvancedContactData(contact)
                otherHelper.createContact(fullContact)
            }
        }
        loadContacts(context)
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

    fun exportVcf(context: Context, uri: Uri, contactsToExport: List<ContactData>? = null) {
        val exportHelper = ExportHelper(context, contactsRepository)
        exportHelper.exportContacts(uri, contactsToExport ?: contacts)
        context.toast(R.string.export_success)
    }

    fun shareTempContacts(context: Context, contacts: List<ContactData>) {
        viewModelScope.launch(Dispatchers.IO) {
            val exportHelper = ExportHelper(context, contactsRepository)
            val tempFileUri = exportHelper.exportTempContact(contacts)
            IntentHelper.shareContactVcf(context, tempFileUri)
        }
    }

    /**
     * Returns a list of account type to account name
     */
    fun getAvailableAccounts(context: Context): List<AccountType> {
        if (!PermissionHelper.hasPermission(context, Manifest.permission.READ_SYNC_SETTINGS))
            return listOf(AccountType.androidDefault)

        return deviceContactsRepository.getAccountTypes()
    }

    fun getAvailableGroups() = contacts.map { it.groups }.flatten().distinct()

    fun getContactByNumber(number: String): ContactData? {
        val normalizedNumber = ContactsHelper.normalizePhoneNumber(number)
        return contacts.firstOrNull {
            it.numbers.any { (value, _) ->
                ContactsHelper.normalizePhoneNumber(value) == normalizedNumber
            }
        }
    }

    fun updateContactRingTone(contact: ContactData, uri: Uri) {
        viewModelScope.launch {
            deviceContactsRepository.updateContactRingTone(contact.contactId.toString(), uri)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as App
                ContactsModel(
                    application.applicationContext,
                    application.localContactsRepository,
                    application.deviceContactsRepository
                )
            }
        }
    }
}
