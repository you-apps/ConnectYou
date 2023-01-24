package com.bnyro.contacts.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.obj.ContactData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsPage(contacts: List<ContactData>?) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (contacts != null) {
            Column {
                val value = remember {
                    mutableStateOf(TextFieldValue())
                }

                SearchBar(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), value)

                LazyColumn {
                    items(contacts) {
                        ContactItem(it)
                    }
                }
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
