package com.bnyro.contacts.enums

enum class SortOrder(val value: Int) {
    NAME(0),
    FIRSTNAME(1),
    SURNAME(2);

    companion object {
        fun fromInt(value: Int) = SortOrder.values().first { it.value == value }
    }
}
