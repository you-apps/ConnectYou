package com.bnyro.contacts.enums

enum class SortOrder {
    FIRSTNAME,
    LASTNAME,
    NICKNAME;

    companion object {
        fun fromInt(value: Int) = SortOrder.values().first { it.ordinal == value }
    }
}
