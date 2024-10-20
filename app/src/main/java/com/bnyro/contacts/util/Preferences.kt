package com.bnyro.contacts.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.bnyro.contacts.domain.enums.BackupType
import com.bnyro.contacts.domain.model.AccountType

object Preferences {
    private const val prefFile = "preferences"
    private lateinit var preferences: SharedPreferences

    const val homeTabKey = "homeTab"
    const val selectedContactsRepo = "selectedContactsRepo"
    const val themeKey = "theme"
    const val backupDirKey = "backupDir"
    const val backupTypeKey = "backupType"
    const val backupNamingSchemeKey = "backupNamingScheme"
    const val sortOrderKey = "sorting"
    const val hiddenAccountsKey = "hiddenAccounts"
    const val backupIntervalKey = "backupInterval"
    const val maxBackupAmountKey = "maxBackupAmount"
    const val collapseBottomBarKey = "collapseBottomBar"
    const val colorfulContactIconsKey = "colorfulContactIcons"
    const val encryptBackupsKey = "encryptBackups"
    const val encryptBackupPasswordKey = "encryptBackupsPassword"
    const val storeSmsLocallyKey = "storeSmsLocally"
    const val lastChosenAccount = "lastChosenAccount"
    const val biometricAuthKey = "biometricAuth"
    const val favoritesOnlyKey = "favoritesOnly"

    fun init(context: Context) {
        preferences = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE)
    }

    fun getBoolean(key: String, defValue: Boolean) = preferences.getBoolean(key, defValue)
    fun getString(key: String, defValue: String) = preferences.getString(key, defValue)
    fun getInt(key: String, defValue: Int) = preferences.getInt(key, defValue)
    fun getStringSet(key: String, defValue: Set<String>) = preferences.getStringSet(key, defValue)

    fun edit(action: SharedPreferences.Editor.() -> Unit) {
        preferences.edit().apply(action).apply()
    }

    fun getBackupType(): BackupType {
        return BackupType.fromInt(getInt(backupTypeKey, BackupType.NONE.ordinal))
    }

    fun getLastChosenAccount(): AccountType {
        getString(lastChosenAccount, "")
            .takeIf { !it.isNullOrBlank() }
            ?.let {
                val split = it.split("|")
                return AccountType(split.last(), split.first())
            }

        return AccountType.androidDefault
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: Boolean): MutableState<Boolean> {
    return remember {
        mutableStatePreferenceOf(Preferences.getBoolean(key, defaultValue)) {
            Preferences.edit { putBoolean(key, it) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: Int): MutableState<Int> {
    return remember {
        mutableStatePreferenceOf(Preferences.getInt(key, defaultValue)) {
            Preferences.edit { putInt(key, it) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: String): MutableState<String> {
    return remember {
        mutableStatePreferenceOf(Preferences.getString(key, defaultValue) ?: defaultValue) {
            Preferences.edit { putString(key, it) }
        }
    }
}

inline fun <T> mutableStatePreferenceOf(
    value: T,
    crossinline onStructuralInequality: (newValue: T) -> Unit
) =
    mutableStateOf(
        value = value,
        policy = object : SnapshotMutationPolicy<T> {
            override fun equivalent(a: T, b: T): Boolean {
                if (a == b) return true
                onStructuralInequality(b)
                return false
            }
        }
    )
