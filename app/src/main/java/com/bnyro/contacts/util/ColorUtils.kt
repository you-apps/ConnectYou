package com.bnyro.contacts.util

object ColorUtils {
    fun getRandomColor(): Int = (Math.random() * 16777215).toInt() or (0xFF shl 24)
}
