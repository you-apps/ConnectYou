package com.bnyro.contacts.data.database.obj

import androidx.room.Embedded
import androidx.room.Relation

data class ContactWithData(
    @Embedded val contact: LocalContact = LocalContact(),
    @Relation(
        parentColumn = "id",
        entityColumn = "contactId"
    )
    val dataItems: List<DbDataItem> = listOf()
)
