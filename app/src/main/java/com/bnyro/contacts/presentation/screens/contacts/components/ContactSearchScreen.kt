package com.bnyro.contacts.presentation.screens.contacts.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.FilterOptions
import com.bnyro.contacts.presentation.components.ElevatedTextInputField
import com.bnyro.contacts.presentation.components.FullScreenDialog
import com.bnyro.contacts.util.ContactsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun ContactSearchScreen(
    contacts: List<ContactData>,
    filterOptions: FilterOptions,
    onDismissRequest: () -> Unit
) {
    FullScreenDialog(onDismissRequest) {
        val focusRequester = remember {
            FocusRequester()
        }

        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
        }

        var searchQuery by remember {
            mutableStateOf("")
        }
        var visibleContacts by remember {
            mutableStateOf(contacts)
        }
        LaunchedEffect(searchQuery) {
            withContext(Dispatchers.IO) {
                visibleContacts = ContactsHelper.filter(contacts, searchQuery)
            }
        }

        Column(Modifier.fillMaxSize()) {
            ElevatedTextInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(top = 8.dp),
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                leadingIcon = Icons.Default.Search,
                placeholder = stringResource(id = R.string.search),
                imeAction = ImeAction.Done,
                focusRequester = focusRequester,
                singleLine = true
            )
            ContactsList(
                contacts = visibleContacts,
                filterOptions = filterOptions,
                scrollConnection = null,
                selectedContacts = emptyList<ContactData>().toMutableList()
            )
        }
    }
}
