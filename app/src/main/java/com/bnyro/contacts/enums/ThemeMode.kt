package com.bnyro.contacts.enums

enum class ThemeMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromInt(value: Int) = ThemeMode.values().first { it.value == value }
    }
}
