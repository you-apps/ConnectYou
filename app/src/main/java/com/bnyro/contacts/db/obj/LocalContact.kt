package com.bnyro.contacts.db.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "localContacts")
data class LocalContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo val displayName: String? = null,
    @ColumnInfo val firstName: String? = null,
    @ColumnInfo val surName: String? = null,
    @ColumnInfo val nickName: String? = null,
    @ColumnInfo val organization: String? = null
)
