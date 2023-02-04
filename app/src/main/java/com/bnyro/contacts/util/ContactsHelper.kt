package com.bnyro.contacts.util

import com.bnyro.contacts.obj.ContactData

abstract class ContactsHelper {
    abstract fun createContact(contact: ContactData)

    abstract fun deleteContact(contact: ContactData)

    abstract fun getAllContacts(): List<ContactData>
}
