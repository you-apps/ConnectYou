package com.bnyro.contacts.util.extension

fun String.removeLastChar(): String {
    return if (isEmpty()) this
    else substring(0, length - 1)
}
