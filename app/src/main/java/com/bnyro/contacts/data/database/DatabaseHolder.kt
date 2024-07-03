package com.bnyro.contacts.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseHolder {
    private const val DB_NAME = "com.bnyro.contacts"
    lateinit var Db: AppDatabase

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE localContacts ADD COLUMN nickName TEXT DEFAULT NULL"
            )
            db.execSQL(
                "ALTER TABLE localContacts ADD COLUMN organization TEXT DEFAULT NULL"
            )
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE localContacts ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun init(context: Context) {
        Db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_5_6)
            .build()
    }
}
