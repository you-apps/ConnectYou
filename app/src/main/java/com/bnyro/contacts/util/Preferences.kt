package com.bnyro.contacts.util

import android.content.Context
import android.content.SharedPreferences

object Preferences {
    private const val prefFile = "preferences"
    private lateinit var preferences: SharedPreferences

    const val homeTabKey = "homeTab"

    fun init(context: Context) {
        preferences = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE)
    }

    fun getBoolean(key: String, defValue: Boolean) = preferences.getBoolean(key, defValue)
    fun getString(key: String, defValue: String) = preferences.getString(key, defValue)
    fun getInt(key: String, defValue: Int) = preferences.getInt(key, defValue)

    fun edit(action: SharedPreferences.Editor.() -> Unit) {
        preferences.edit().apply(action).apply()
    }
}
