package com.bnyro.contacts.util

import com.bnyro.contacts.obj.ContactData

class LocalContactsHelper : ContactsHelper() {
    override suspend fun createContact(contact: ContactData) {
    }

    override suspend fun deleteContacts(contacts: List<ContactData>) {
    }

    override suspend fun getContactList(): List<ContactData> {
        return listOf(ContactData(0, null, "Schablonski"))
    }

    override suspend fun loadAdvancedData(contact: ContactData): ContactData {
        return contact
    }
}
