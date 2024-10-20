package com.bnyro.contacts.util.extension

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.toast(@StringRes text: Int) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}

fun Context.toast(text: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
