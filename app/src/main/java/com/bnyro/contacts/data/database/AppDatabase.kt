package com.bnyro.contacts.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.bnyro.contacts.data.database.dao.LocalContactsDao
import com.bnyro.contacts.data.database.dao.LocalSmsDao
import com.bnyro.contacts.data.database.obj.DbDataItem
import com.bnyro.contacts.data.database.obj.LocalContact
import com.bnyro.contacts.data.database.obj.SmsData

@Database(
    entities = [LocalContact::class, DbDataItem::class, SmsData::class],
    autoMigrations = [
        AutoMigration(2, 3),
        AutoMigration(3, 4),
        AutoMigration(4, 5),
        AutoMigration(6, 7),
        AutoMigration(7, 8)
    ],
    version = 8
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localContactsDao(): LocalContactsDao
    abstract fun localSmsDao(): LocalSmsDao
}
