package com.bnyro.contacts.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.ui.components.ContactsPage
import com.bnyro.contacts.ui.models.ContactsModel

@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val contactsModel: ContactsModel = viewModel()

    LaunchedEffect(Unit) {
        contactsModel.loadContacts(context)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ContactsPage(contactsModel.contacts)
    }
}
