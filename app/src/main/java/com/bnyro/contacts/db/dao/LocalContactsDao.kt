package com.bnyro.contacts.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.bnyro.contacts.db.obj.ContactWithData

@Dao
interface LocalContactsDao {
    @Transaction
    @Query("SELECT * FROM localContacts")
    fun getAll(): List<ContactWithData>
}
