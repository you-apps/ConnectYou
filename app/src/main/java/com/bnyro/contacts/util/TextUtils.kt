package com.bnyro.contacts.util

object TextUtils {
    private const val PHONE_NUMBER_REGEX = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$"

    fun isPhoneNumber(text: String): Boolean {
        return PHONE_NUMBER_REGEX.toRegex().matches(text) || text.all { it.isDigit() || it == '-' }
    }
}
