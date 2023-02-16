package com.bnyro.contacts.enums

enum class BackupType(val value: Int) {
    NONE(0),
    DEVICE(1),
    LOCAL(2),
    BOTH(3);

    companion object {
        fun fromInt(value: Int) = BackupType.values().first { it.value == value }
    }
}
