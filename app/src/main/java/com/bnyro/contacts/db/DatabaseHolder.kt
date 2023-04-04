package com.bnyro.contacts.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseHolder {
    private const val DB_NAME = "com.bnyro.contacts"
    lateinit var Db: AppDatabase

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE localContacts ADD COLUMN nickName TEXT DEFAULT NULL"
            )
            database.execSQL(
                "ALTER TABLE localContacts ADD COLUMN organization TEXT DEFAULT NULL"
            )
        }
    }

    fun init(context: Context) {
        Db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}
