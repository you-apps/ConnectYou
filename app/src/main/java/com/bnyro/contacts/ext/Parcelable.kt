package com.bnyro.contacts.ext

import android.content.Intent
import androidx.core.content.IntentCompat

inline fun <reified T> Intent.parcelable(key: String): T? {
    return IntentCompat.getParcelableExtra(this, key, T::class.java)
}