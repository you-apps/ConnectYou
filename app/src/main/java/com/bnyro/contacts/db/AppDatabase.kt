package com.bnyro.contacts.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.bnyro.contacts.db.dao.LocalContactsDao
import com.bnyro.contacts.db.dao.LocalSmsDao
import com.bnyro.contacts.db.obj.DbDataItem
import com.bnyro.contacts.db.obj.LocalContact
import com.bnyro.contacts.db.obj.SmsData

@Database(
    entities = [LocalContact::class, DbDataItem::class, SmsData::class],
    autoMigrations = [AutoMigration(2, 3), AutoMigration(3, 4)],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localContactsDao(): LocalContactsDao
    abstract fun localSmsDao(): LocalSmsDao
}
