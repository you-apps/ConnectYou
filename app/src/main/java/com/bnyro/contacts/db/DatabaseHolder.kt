package com.bnyro.contacts.db

import android.content.Context
import androidx.room.Room

object DatabaseHolder {
    private const val DB_NAME = "com.bnyro.contacts"
    lateinit var Db: AppDatabase

    fun init(context: Context) {
        Db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        ).build()
    }
}
