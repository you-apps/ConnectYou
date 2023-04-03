package com.bnyro.contacts.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bnyro.contacts.db.dao.LocalContactsDao
import com.bnyro.contacts.db.obj.LocalContact
import com.bnyro.contacts.db.obj.ValuableType

@Database(
    entities = [LocalContact::class, ValuableType::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localContactsDao(): LocalContactsDao
}
