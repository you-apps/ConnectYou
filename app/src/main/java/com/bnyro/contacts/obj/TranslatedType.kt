package com.bnyro.contacts.obj

import androidx.annotation.StringRes
import ezvcard.parameter.VCardParameter

data class TranslatedType(
    val id: Int,
    @StringRes val title: Int,
    val vcardType: VCardParameter? = null
)
