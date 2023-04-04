package com.bnyro.contacts.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.bnyro.contacts.db.obj.ContactWithData
import com.bnyro.contacts.db.obj.DbDataItem
import com.bnyro.contacts.db.obj.LocalContact

@Dao
interface LocalContactsDao {
    @Transaction
    @Query("SELECT * FROM localContacts")
    suspend fun getAll(): List<ContactWithData>

    @Insert
    suspend fun insertContact(contact: LocalContact): Long

    @Insert
    suspend fun insertData(vararg data: DbDataItem)

    @Query("DELETE FROM localContacts WHERE id = :id")
    suspend fun deleteContactByID(id: Long)

    @Query("DELETE FROM valuableTypes WHERE contactId = :contactId")
    suspend fun deleteDataByContactID(contactId: Long)

    @Query("DELETE FROM valuableTypes WHERE category = :category AND value = :value")
    suspend fun deleteDataByCategoryAndValue(category: Int, value: String)
}
