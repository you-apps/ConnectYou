package com.bnyro.contacts.ext

fun String?.notAName(): Boolean {
    this ?: return false
    if (this.isBlank()) return false
    return !this.trim().all { it.isDigit() }
}
