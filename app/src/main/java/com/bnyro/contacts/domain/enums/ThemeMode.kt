package com.bnyro.contacts.domain.enums

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED;

    companion object {
        fun fromInt(value: Int) = ThemeMode.values().first { it.ordinal == value }
    }
}
