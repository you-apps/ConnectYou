package com.bnyro.contacts.domain.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bnyro.contacts.R
import com.bnyro.contacts.data.database.DatabaseHolder
import com.bnyro.contacts.data.database.obj.DbDataItem
import com.bnyro.contacts.data.database.obj.LocalContact
import com.bnyro.contacts.domain.enums.BackupType
import com.bnyro.contacts.domain.enums.DataCategory
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.ContactsGroup
import com.bnyro.contacts.domain.model.ValueWithType
import com.bnyro.contacts.util.ImageHelper
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.util.extension.pmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalContactsRepository(context: Context) : ContactsRepository {
    override val label: String = context.getString(R.string.local)

    private val picturesDir = File(context.filesDir, PICTURES_DIR).also {
        if (!it.exists()) it.mkdirs()
    }

    override suspend fun createContact(contact: ContactData) = withContext(Dispatchers.IO) {
        val localContact = LocalContact(
            displayName = contact.displayName,
            firstName = contact.firstName,
            surName = contact.surName,
            nickName = contact.nickName,
            title = contact.title,
            organization = contact.organization,
            favorite = contact.favorite
        )
        val contactId = DatabaseHolder.Db.localContactsDao().insertContact(localContact)
        val dataItems = listOf(
            contact.numbers.toDataItem(contactId, DataCategory.NUMBER),
            contact.emails.toDataItem(contactId, DataCategory.EMAIL),
            contact.addresses.toDataItem(contactId, DataCategory.ADDRESS),
            contact.events.toDataItem(contactId, DataCategory.EVENT),
            contact.notes.toDataItem(contactId, DataCategory.NOTE),
            contact.websites.toDataItem(contactId, DataCategory.WEBSITE),
            contact.groups.map {
                DbDataItem(
                    contactId = contactId,
                    category = DataCategory.GROUP.ordinal,
                    value = it.title,
                    type = -1
                )
            }
        ).flatten()
        DatabaseHolder.Db.localContactsDao().insertData(*dataItems.toTypedArray())
        contact.photo?.let { saveProfileImage(contactId, it) } ?: Unit
    }

    override suspend fun updateContact(contact: ContactData) {
        withContext(Dispatchers.IO) {
            deleteContacts(listOf(contact))
            createContact(contact)
        }
    }

    override suspend fun deleteContacts(contacts: List<ContactData>) {
        withContext(Dispatchers.IO) {
            contacts.forEach {
                with(DatabaseHolder.Db.localContactsDao()) {
                    deleteContactByID(it.contactId)
                    deleteDataByContactID(it.contactId)
                }
                deleteProfileImage(it.contactId)
            }
        }
    }

    override suspend fun setFavorite(contact: ContactData, favorite: Boolean) = withContext(Dispatchers.IO) {
        DatabaseHolder.Db.localContactsDao().setFavorite(contact.contactId, favorite)
    }

    override suspend fun getContactList(): List<ContactData> = withContext(Dispatchers.IO) {
        DatabaseHolder.Db.localContactsDao().getAll().pmap {
            val profileImage = getProfileImage(it.contact.id)
            ContactData(
                contactId = it.contact.id,
                displayName = it.contact.displayName,
                alternativeName = listOf(it.contact.surName, it.contact.firstName).joinToString(
                    ", "
                ),
                firstName = it.contact.firstName,
                surName = it.contact.surName,
                nickName = it.contact.nickName,
                title = it.contact.title,
                organization = it.contact.organization,
                favorite = it.contact.favorite,
                photo = profileImage,
                thumbnail = profileImage,
                numbers = it.dataItems.toValueWithType(DataCategory.NUMBER),
                emails = it.dataItems.toValueWithType(DataCategory.EMAIL),
                addresses = it.dataItems.toValueWithType(DataCategory.ADDRESS),
                events = it.dataItems.toValueWithType(DataCategory.EVENT),
                notes = it.dataItems.toValueWithType(DataCategory.NOTE),
                websites = it.dataItems.toValueWithType(DataCategory.WEBSITE),
                groups = it.dataItems.filter { d -> d.category == DataCategory.GROUP.ordinal }
                    .map { group -> ContactsGroup(group.value, -1) }
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

    private fun List<DbDataItem>.toValueWithType(category: DataCategory): List<ValueWithType> {
        return filter { it.category == category.ordinal }.map { ValueWithType(it.value, it.type) }
    }

    private fun List<ValueWithType>.toDataItem(contactId: Long, category: DataCategory): List<DbDataItem> {
        return map {
            DbDataItem(
                contactId = contactId,
                category = category.ordinal,
                value = it.value,
                type = it.type
            )
        }
    }

    override suspend fun loadAdvancedData(contact: ContactData): ContactData = contact
    override fun isAutoBackupEnabled(): Boolean {
        return Preferences.getBackupType() in listOf(BackupType.BOTH, BackupType.LOCAL)
    }

    override suspend fun createGroup(groupName: String): ContactsGroup {
        // Groups don't need to be created here additionally since they are just stored inside the contacts
        return ContactsGroup(groupName, -1)
    }

    override suspend fun renameGroup(group: ContactsGroup, newName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroup(group: ContactsGroup) = withContext(Dispatchers.IO) {
        DatabaseHolder.Db.localContactsDao().deleteDataByCategoryAndValue(
            DataCategory.GROUP.ordinal,
            group.title
        )
    }

    companion object {
        private const val PICTURES_DIR = "images"
    }
}
