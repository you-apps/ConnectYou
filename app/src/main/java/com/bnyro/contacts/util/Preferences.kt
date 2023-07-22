package com.bnyro.contacts.util

import android.content.Context
import android.content.SharedPreferences
import com.bnyro.contacts.enums.BackupType

object Preferences {
    private const val prefFile = "preferences"
    private lateinit var preferences: SharedPreferences

    const val homeTabKey = "homeTab"
    const val themeKey = "theme"
    const val backupDirKey = "backupDir"
    const val backupTypeKey = "backupType"
    const val sortOrderKey = "sorting"
    const val backupIntervalKey = "backupInterval"
    const val maxBackupAmountKey = "maxBackupAmount"
    const val collapseBottomBarKey = "collapseBottomBar"
    const val colorfulContactIconsKey = "colorfulContactIcons"
    const val encryptBackupsKey = "encryptBackups"
    const val encryptBackupPasswordKey = "encryptBackupsPassword"

    fun init(context: Context) {
        preferences = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE)
    }

    fun getBoolean(key: String, defValue: Boolean) = preferences.getBoolean(key, defValue)
    fun getString(key: String, defValue: String) = preferences.getString(key, defValue)
    fun getInt(key: String, defValue: Int) = preferences.getInt(key, defValue)

    fun edit(action: SharedPreferences.Editor.() -> Unit) {
        preferences.edit().apply(action).apply()
    }

    fun getBackupType(): BackupType {
        return BackupType.fromInt(getInt(backupTypeKey, BackupType.NONE.value))
    }
}
