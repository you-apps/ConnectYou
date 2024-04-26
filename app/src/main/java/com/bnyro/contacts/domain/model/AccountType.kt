package com.bnyro.contacts.domain.model

data class AccountType(
    val name: String,
    val type: String,
) {
    val identifier = "$type|$name"

    companion object {
        private const val ANDROID_ACCOUNT_TYPE = "com.android.contacts"
        private const val ANDROID_ACCOUNT_NAME = "DEVICE"

        val androidDefault = AccountType(
            ANDROID_ACCOUNT_NAME,
            ANDROID_ACCOUNT_TYPE
        )
    }
}
