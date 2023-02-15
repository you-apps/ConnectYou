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
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.ThemeModel
import com.bnyro.contacts.ui.screens.ContactsScreen
import com.bnyro.contacts.ui.theme.ConnectYouTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeModel: ThemeModel = ViewModelProvider(this).get()
        val contactsModel: ContactsModel = ViewModelProvider(this).get()

        contactsModel.init(this)

        setContent {
            ConnectYouTheme(
                darkTheme = when (themeModel.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    else -> isSystemInDarkTheme()
                }
            ) {
                ContactsScreen(shouldShowEditor(), getInitialContactId())
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
}
