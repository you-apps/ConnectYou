package com.bnyro.contacts.domain.model

import androidx.annotation.StringRes
import ezvcard.parameter.VCardParameter

data class TranslatedType(
    val id: Int,
    @StringRes val title: Int,
    val vcardType: VCardParameter? = null
)
