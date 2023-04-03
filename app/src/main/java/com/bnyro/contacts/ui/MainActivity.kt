package com.bnyro.contacts.ui

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.QuickContact
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bnyro.contacts.enums.ThemeMode
import com.bnyro.contacts.ui.components.dialogs.AddToContactDialog
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.ThemeModel
import com.bnyro.contacts.ui.screens.ContactsScreen
import com.bnyro.contacts.ui.theme.ConnectYouTheme
import com.bnyro.contacts.util.BackupHelper

class MainActivity : ComponentActivity() {
    private val phoneNumberExtra = "phone"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeModel: ThemeModel = ViewModelProvider(this).get()
        val contactsModel: ContactsModel = ViewModelProvider(this).get()

        contactsModel.init(this)

        handleVcfShareAction(contactsModel)

        setContent {
            ConnectYouTheme(
                darkTheme = when (themeModel.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    else -> isSystemInDarkTheme()
                }
            ) {
                ContactsScreen(shouldShowEditor(), getInitialContactId())
                updatedContactNumber()?.let {
                    AddToContactDialog(it)
                }
            }
        }
    }

    private fun shouldShowEditor(): Boolean {
        return when (intent?.action) {
            Intent.ACTION_INSERT -> true
            else -> intent?.getStringExtra("action") == "create"
        }
    }

    private fun getInitialContactId(): Long? {
        return when (intent?.action) {
            Intent.ACTION_EDIT, Intent.ACTION_VIEW, QuickContact.ACTION_QUICK_CONTACT -> intent?.data?.lastPathSegment?.toLongOrNull()
            else -> null
        }
    }

    private fun updatedContactNumber(): String? {
        return when (intent?.action) {
            Intent.ACTION_INSERT_OR_EDIT -> intent?.getStringExtra(phoneNumberExtra)
            else -> null
        }
    }

    private fun handleVcfShareAction(contactsModel: ContactsModel) {
        if (intent.action != Intent.ACTION_VIEW || intent?.type !in BackupHelper.vCardMimeTypes) return
        contactsModel.importVcf(this, intent?.data ?: return)
    }
}
