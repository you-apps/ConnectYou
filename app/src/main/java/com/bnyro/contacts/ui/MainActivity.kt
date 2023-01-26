package com.bnyro.contacts.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bnyro.contacts.ui.screens.ContactsScreen
import com.bnyro.contacts.ui.theme.ConnectYouTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val showEditor = intent?.getStringExtra("action") == "create"

        setContent {
            ConnectYouTheme {
                ContactsScreen(showEditor)
            }
        }
    }
}
