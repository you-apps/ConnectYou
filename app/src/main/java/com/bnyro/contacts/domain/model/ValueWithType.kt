package com.bnyro.contacts.domain.model

/**
 * A data class that represents a value with its type.
 *
 * @property value The value of the data.
 * @property type The type of the data. This is the column index of the **TYPE**
 *
 * for example:
 * - [Email][android.provider.ContactsContract.CommonDataKinds.Email]
 * - [Phone][android.provider.ContactsContract.CommonDataKinds.Phone]
 * - [StructuredPostal][android.provider.ContactsContract.CommonDataKinds.StructuredPostal]
 * - [Provider][android.provider.ContactsContract.CommonDataKinds.Website]
 * - [Event][android.provider.ContactsContract.CommonDataKinds.Event]
 * - [Note][android.provider.ContactsContract.CommonDataKinds.Note]
 * - [Organization][android.provider.ContactsContract.CommonDataKinds.Organization]
 */
data class ValueWithType(
    var value: String,
    var type: Int? = null
)
