package com.bnyro.contacts.enums

enum class BackupType {
    NONE,
    DEVICE,
    LOCAL,
    BOTH;

    companion object {
        fun fromInt(value: Int) = BackupType.values().first { it.ordinal == value }
    }
}
