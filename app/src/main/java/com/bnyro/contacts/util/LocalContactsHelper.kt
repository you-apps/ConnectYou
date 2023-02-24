package com.bnyro.contacts.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bnyro.contacts.R
import com.bnyro.contacts.db.DatabaseHolder
import com.bnyro.contacts.db.obj.LocalContact
import com.bnyro.contacts.db.obj.ValuableType
import com.bnyro.contacts.enums.BackupType
import com.bnyro.contacts.enums.DataCategory
import com.bnyro.contacts.ext.pmap
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ValueWithType
import java.io.File

class LocalContactsHelper(context: Context) : ContactsHelper() {
    override val label: String = context.getString(R.string.local)

    private val picturesDir = File(context.filesDir, "images").also {
        if (!it.exists()) it.mkdirs()
    }

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
            contact.events.toValuableType(contactId, DataCategory.EVENT),
            contact.notes.toValuableType(contactId, DataCategory.NOTE)
        ).flatten()
        DatabaseHolder.Db.localContactsDao().insertData(*dataItems.toTypedArray())
        contact.photo?.let { saveProfileImage(contactId, it) }
    }

    override suspend fun updateContact(contact: ContactData) {
        deleteContacts(listOf(contact))
        createContact(contact)
    }

    override suspend fun deleteContacts(contacts: List<ContactData>) {
        contacts.forEach {
            with(DatabaseHolder.Db.localContactsDao()) {
                deleteContactByID(it.contactId)
                deleteDataByContactID(it.contactId)
            }
            deleteProfileImage(it.contactId)
        }
    }

    override suspend fun getContactList(): List<ContactData> {
        return DatabaseHolder.Db.localContactsDao().getAll().pmap {
            val profileImage = getProfileImage(it.contact.id)
            ContactData(
                contactId = it.contact.id,
                displayName = it.contact.displayName,
                firstName = it.contact.firstName,
                surName = it.contact.surName,
                photo = profileImage,
                thumbnail = profileImage,
                numbers = it.dataItems.toValueWithType(DataCategory.NUMBER),
                emails = it.dataItems.toValueWithType(DataCategory.EMAIL),
                addresses = it.dataItems.toValueWithType(DataCategory.ADDRESS),
                events = it.dataItems.toValueWithType(DataCategory.EVENT),
                notes = it.dataItems.toValueWithType(DataCategory.NOTE),
                groups = it.dataItems.toValueWithType(DataCategory.GROUP)
            )
        }
    }

    private fun saveProfileImage(contactId: Long, bitmap: Bitmap) {
        val file = File(picturesDir, contactId.toString())
        val bytes = ImageHelper.bitmapToByteArray(bitmap)
        file.outputStream().use {
            it.write(bytes)
        }
    }

    private fun getProfileImage(contactId: Long): Bitmap? {
        val file = File(picturesDir, contactId.toString())
        if (!file.exists()) return null
        return file.inputStream().use {
            BitmapFactory.decodeStream(it)
        }
    }

    private fun deleteProfileImage(contactId: Long) {
        File(picturesDir, contactId.toString()).delete()
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
    override fun isAutoBackupEnabled(): Boolean {
        return Preferences.getBackupType() in listOf(BackupType.BOTH, BackupType.LOCAL)
    }

    override suspend fun createGroup(
        groupName: String,
        accountName: String,
        accountType: String
    ): ValueWithType? {
        TODO("Not yet implemented")
    }

    override suspend fun renameGroup(group: ValueWithType) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroup(groupId: Long) {
        TODO("Not yet implemented")
    }
}
