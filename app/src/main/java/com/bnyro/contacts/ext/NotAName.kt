package com.bnyro.contacts.ext

fun String?.notAName(): Boolean {
    this ?: return true
    if (this.isBlank()) return true
    return this.trim().all { it.isDigit() }
}
