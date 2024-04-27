package com.bnyro.contacts.domain.model

import android.net.Uri

data class CallerInfo(
    val rawPhoneNumber: String = "",
    val formattedPhoneNumber: String = "",
    val callerName: String? = null,
    val callerPhoto: Uri? = null
)
