package com.bnyro.contacts.domain.model

import android.util.Log

sealed interface AccountType {
    fun displayName(): String
    fun displayType(): String

    data class AccountColumns(
        val accountName: String?,
        val accountType: String?,
    )

    fun toAccountColumns(): AccountColumns {
        return when (this) {
            is DeviceAccountType -> AccountColumns(null, null)
            is RealAccountType -> AccountColumns(accountName = this.name, accountType = this.type)
        }
    }

    fun toPreferencesString(): String {
        val (name, type) = when (this) {
            is DeviceAccountType -> Pair(
                DeviceAccountType.LEGACY_NAME,
                DeviceAccountType.LEGACY_TYPE
            )
            is RealAccountType -> Pair(this.name, this.type)
        }
        return "${type}|${name}"
    }

    companion object {
        fun fromAccountColumns(accountName: String?, accountType: String?): AccountType? {
            return if (accountName == null && accountType == null) {
                DeviceAccountType
            } else if (accountName != null && accountType != null) {
                fromNameAndType(accountName, accountType)
            } else {
                Log.e("AccountType", "Raw contact has partial account: name=($accountName),type=($accountType)")
                null
            }
        }

        fun fromPreferencesString(identifier: String): AccountType? {
            val sections = identifier.split('|')
            if (sections.size != 2) return null
            val type = sections[0]
            val name = sections[1]
            return fromNameAndType(name, type)
        }

        private fun fromNameAndType(name: String, type: String): AccountType {
            return if (type == DeviceAccountType.LEGACY_TYPE
                    && name == DeviceAccountType.LEGACY_NAME
            ) {
                // This is for contacts saved before the fix for #477
                DeviceAccountType
            } else {
                RealAccountType(name, type)
            }
        }
    }
}

data object DeviceAccountType: AccountType {
    /* The legacy signifiers here are very important.  We must always
      be able to deserialize objects from previous versions of the app -
      to do that we must know these exact values. */
    internal const val LEGACY_TYPE: String = "com.android.contacts"
    internal const val LEGACY_NAME: String = "DEVICE"

    override fun displayName(): String {
        // TODO: retrieve a localised value instead
        // e.g. Through context.getString(R.string.device)
        return "Device"
    }

    override fun displayType(): String {
        // TODO choose new value
        return "com.android.contacts"
    }
}

data class RealAccountType(
    val name: String,
    val type: String,
): AccountType {
    override fun displayName(): String {
        return name
    }

    override fun displayType(): String {
        return type
    }
}
