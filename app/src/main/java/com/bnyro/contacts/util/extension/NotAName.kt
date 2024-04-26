package com.bnyro.contacts.util.extension

fun String?.notAName(): Boolean {
    this ?: return true
    if (this.isBlank()) return true
    return this.trim().all { it.isDigit() }
}
