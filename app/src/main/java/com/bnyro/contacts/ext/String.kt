package com.bnyro.contacts.ext

fun String.removeLastChar(): String {
    return if (isEmpty()) this
    else substring(0, length - 1)
}
