package com.bnyro.contacts.util

import com.bnyro.contacts.db.DatabaseHolder
import com.bnyro.contacts.db.obj.LocalContact
import com.bnyro.contacts.db.obj.ValuableType
import com.bnyro.contacts.enums.DataCategory
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ValueWithType

class LocalContactsHelper : ContactsHelper() {
    override suspend fun createContact(contact: ContactData) {
        val localContact = LocalContact(
            displayName = contact.displayName,
            firstName = contact.firstName,
            surName = contact.surName
        )
        val contactId = DatabaseHolder.Db.localContactsDao().insertContact(localContact)
        val dataItems = listOf(
            contact.numbers.toValuableType(contactId, DataCategory.NUMBER),
            contact.emails.toValuableType(contactId, DataCategory.EMAIL),
            contact.addresses.toValuableType(contactId, DataCategory.ADDRESS),
            contact.events.toValuableType(contactId, DataCategory.EVENT)
        ).flatten()
        DatabaseHolder.Db.localContactsDao().insertData(*dataItems.toTypedArray())
    }

    override suspend fun deleteContacts(contacts: List<ContactData>) {
        contacts.forEach {
            with(DatabaseHolder.Db.localContactsDao()) {
                deleteContactByID(it.contactId)
                deleteDataByContactID(it.contactId)
            }
        }
    }

    override suspend fun getContactList(): List<ContactData> {
        return DatabaseHolder.Db.localContactsDao().getAll().map {
            ContactData(
                contactId = it.contact.id,
                displayName = it.contact.displayName,
                firstName = it.contact.firstName,
                surName = it.contact.surName,
                numbers = it.dataItems.toValueWithType(DataCategory.NUMBER),
                emails = it.dataItems.toValueWithType(DataCategory.EMAIL),
                addresses = it.dataItems.toValueWithType(DataCategory.ADDRESS),
                events = it.dataItems.toValueWithType(DataCategory.EVENT)
            )
        }
    }

    private fun List<ValuableType>.toValueWithType(category: DataCategory): List<ValueWithType> {
        return filter { it.category == category.value }.map { ValueWithType(it.value, it.type) }
    }

    private fun List<ValueWithType>.toValuableType(contactId: Long, category: DataCategory): List<ValuableType> {
        return map {
            ValuableType(
                contactId = contactId,
                category = category.value,
                value = it.value,
                type = it.type
            )
        }
    }

    override suspend fun loadAdvancedData(contact: ContactData): ContactData = contact
}
