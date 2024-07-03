package com.bnyro.contacts.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.bnyro.contacts.data.database.obj.ContactWithData
import com.bnyro.contacts.data.database.obj.DbDataItem
import com.bnyro.contacts.data.database.obj.LocalContact

@Dao
interface LocalContactsDao {
    @Transaction
    @Query("SELECT * FROM localContacts")
    suspend fun getAll(): List<ContactWithData>

    @Insert
    suspend fun insertContact(contact: LocalContact): Long

    @Insert
    suspend fun insertData(vararg data: DbDataItem)

    @Query("UPDATE localContacts SET favorite = :favorite WHERE id = :id ")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("DELETE FROM localContacts WHERE id = :id")
    suspend fun deleteContactByID(id: Long)

    @Query("DELETE FROM valuableTypes WHERE contactId = :contactId")
    suspend fun deleteDataByContactID(contactId: Long)

    @Query("DELETE FROM valuableTypes WHERE category = :category AND value = :value")
    suspend fun deleteDataByCategoryAndValue(category: Int, value: String)
}
