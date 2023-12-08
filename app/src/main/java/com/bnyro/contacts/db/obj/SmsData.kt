package com.bnyro.contacts.db.obj

import androidx.compose.ui.text.AnnotatedString
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.bnyro.contacts.util.generateAnnotations

@Entity(tableName = "localSms")
data class SmsData(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo var address: String = "",
    @ColumnInfo var body: String = "",
    @ColumnInfo var timestamp: Long = 0,
    @ColumnInfo var threadId: Long = 0,
    @ColumnInfo var type: Int = 0,
    @ColumnInfo(defaultValue = "NULL") var simNumber: Int? = null,
    @Ignore val formatted: AnnotatedString = generateAnnotations(body)
)
