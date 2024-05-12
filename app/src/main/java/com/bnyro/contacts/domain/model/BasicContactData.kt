package com.bnyro.contacts.domain.model

import android.net.Uri

/**
 * Data class containing contact name, number and thumbnail
 * @property number phone number without any formatting
 * @property name contact display name
 * @property thumbnail contact thumbnail [Uri]
 */
data class BasicContactData(
    val number: String,
    val name: String? = null,
    val thumbnail: Uri? = null
)
