package com.bnyro.contacts.enums

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromInt(value: Int) = ThemeMode.values().first { it.ordinal == value }
    }
}
