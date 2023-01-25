package com.bnyro.contacts.enums

enum class SortOrder(val value: Int) {
    FIRSTNAME(0),
    SURNAME(1);

    companion object {
        fun fromInt(value: Int) = SortOrder.values().first { it.value == value }
    }
}
