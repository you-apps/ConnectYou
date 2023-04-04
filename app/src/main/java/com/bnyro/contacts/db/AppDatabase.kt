package com.bnyro.contacts.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bnyro.contacts.db.dao.LocalContactsDao
import com.bnyro.contacts.db.obj.DbDataItem
import com.bnyro.contacts.db.obj.LocalContact

@Database(
    entities = [LocalContact::class, DbDataItem::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localContactsDao(): LocalContactsDao
}
