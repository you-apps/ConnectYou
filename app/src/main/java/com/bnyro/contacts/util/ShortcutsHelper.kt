package com.bnyro.contacts.util

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.bnyro.contacts.R
import com.bnyro.contacts.ui.MainActivity

object ShortcutHelper {
    sealed class AppShortcut(
        val action: String,
        @DrawableRes val iconRes: Int,
        @StringRes val label: Int
    ) {
        object CREATE_CONTACT : AppShortcut("create", R.drawable.ic_add, R.string.create_contact)
    }
    private val shortcuts = listOf(AppShortcut.CREATE_CONTACT)

    private fun createShortcut(context: Context, action: String, label: String, icon: IconCompat) {
        val shortcut = ShortcutInfoCompat.Builder(context, action)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(icon)
            .setIntent(
                Intent(context, MainActivity::class.java).apply {
                    this.action = Intent.ACTION_VIEW
                    putExtra("action", action)
                }
            )
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    fun createShortcuts(context: Context) {
        ShortcutManagerCompat.getDynamicShortcuts(context).takeIf { it.isEmpty() } ?: return

        shortcuts.forEach {
            createShortcut(
                context,
                it.action,
                context.getString(it.label),
                IconCompat.createWithResource(context, it.iconRes)
            )
        }
    }
}
