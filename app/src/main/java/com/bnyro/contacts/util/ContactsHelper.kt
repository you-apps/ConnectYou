package com.bnyro.contacts.util

import com.bnyro.contacts.obj.ContactData

abstract class ContactsHelper {
    abstract suspend fun createContact(contact: ContactData)

    abstract suspend fun deleteContacts(contacts: List<ContactData>)

    abstract suspend fun getContactList(): List<ContactData>

    abstract suspend fun loadAdvancedData(contact: ContactData): ContactData
}
