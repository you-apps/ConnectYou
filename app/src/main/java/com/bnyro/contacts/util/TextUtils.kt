package com.bnyro.contacts.util

object TextUtils {
    private val VALID_NUMBER_CHARS = listOf(' ', '-', '+')

    fun isPhoneNumber(text: String): Boolean {
        return !text.isBlank() && text.all {
            it.isDigit() || VALID_NUMBER_CHARS.contains(it)
        }
    }
}
