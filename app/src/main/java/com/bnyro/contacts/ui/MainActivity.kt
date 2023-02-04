package com.bnyro.contacts.ui

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.QuickContact
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.screens.ContactsScreen
import com.bnyro.contacts.ui.theme.ConnectYouTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contactsModel: ContactsModel = ViewModelProvider(this).get()
        contactsModel.init(this)

        setContent {
            ConnectYouTheme {
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
