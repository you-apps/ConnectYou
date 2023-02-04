package com.bnyro.contacts.enums

enum class HelperType(val value: Int) {
    DEVICE(0),
    LOCAL(1);

    companion object {
        fun fromInt(value: Int) = HelperType.values().first { it.value == value }
    }
}
