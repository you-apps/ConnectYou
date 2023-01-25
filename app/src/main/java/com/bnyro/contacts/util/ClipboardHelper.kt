package com.bnyro.contacts.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast

class ClipboardHelper(private val context: Context) {
    val clipboard: ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun save(text: String) {
        val clip = ClipData.newPlainText(text, text)
        clipboard.setPrimaryClip(clip)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Copied to clipboard: $text", Toast.LENGTH_SHORT).show()
        }
    }
}
