package com.bnyro.contacts.util

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.IntentActionType
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.ui.activities.MainActivity

object ShortcutHelper {
    sealed class AppShortcut(
        val action: String,
        @DrawableRes val iconRes: Int,
        @StringRes val label: Int
    ) {
        object CreateContact : AppShortcut("create", R.drawable.ic_add, R.string.create_contact)
    }
    private val shortcuts = listOf(AppShortcut.CreateContact)
    val actionTypes = listOf(
        IntentActionType.CONTACT to R.string.contact,
        IntentActionType.DIAL to R.string.dial,
        IntentActionType.SMS to R.string.message,
        IntentActionType.EMAIL to R.string.email
    )

    private fun createShortcut(
        context: Context,
        intent: Intent,
        label: String,
        id: String,
        icon: IconCompat,
        pin: Boolean = false
    ) {
        val shortcut = ShortcutInfoCompat.Builder(context, id)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(icon)
            .setIntent(intent)
            .build()

        if (!pin) {
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        } else {
            ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
        }
    }

    fun createShortcuts(context: Context) {
        ShortcutManagerCompat.getDynamicShortcuts(context).takeIf { it.isEmpty() } ?: return

        shortcuts.forEach {
            createShortcut(
                context,
                Intent(context, MainActivity::class.java).apply {
                    this.action = Intent.ACTION_VIEW
                    putExtra("action", it.action)
                },
                context.getString(it.label),
                it.action,
                IconCompat.createWithResource(context, it.iconRes)
            )
        }
    }

    fun createContactShortcut(
        context: Context,
        contact: ContactData,
        selectedData: String,
        intentActionType: IntentActionType
    ) {
        val intent = IntentHelper.getLaunchIntent(intentActionType, selectedData)

        val photo = contact.thumbnail ?: contact.photo
        val iconBitmap = if (photo != null) {
            IconCompat.createWithBitmap(photo)
        } else {
            val icon = when (intentActionType) {
                IntentActionType.DIAL -> R.drawable.ic_call
                IntentActionType.SMS -> R.drawable.ic_message
                IntentActionType.EMAIL -> R.drawable.ic_add
                IntentActionType.CONTACT -> R.drawable.ic_contact
                else -> return
            }
            IconCompat.createWithResource(context, icon)
        }

        createShortcut(
            context,
            intent,
            contact.displayName.orEmpty(),
            System.currentTimeMillis().toString(),
            iconBitmap,
            true
        )
    }
}
