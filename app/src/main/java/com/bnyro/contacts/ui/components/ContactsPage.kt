package com.bnyro.contacts.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bnyro.contacts.obj.ContactData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsPage(contacts: List<ContactData>?) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (contacts != null) {
            LazyColumn {
                items(contacts) {
                    ContactItem(it)
                }
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
