package com.bnyro.contacts.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.bnyro.contacts.obj.SmsThread
import com.bnyro.contacts.ui.components.SmsThreadItem
import com.bnyro.contacts.ui.components.base.ElevatedTextInputField
import com.bnyro.contacts.ui.components.base.FullScreenDialog
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.util.ContactsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SmsSearchScreen(
    smsModel: SmsModel,
    threadList: List<SmsThread>,
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
        var visibleThreads by remember {
            mutableStateOf(threadList)
        }

        LaunchedEffect(searchQuery) {
            withContext(Dispatchers.IO) {
                val query = searchQuery.lowercase()

                visibleThreads = threadList.filter {
                    it.address.lowercase().contains(query) || it.contactData?.let { contact ->
                        ContactsHelper.matches(contact, query)
                    } ?: false || it.smsList.any { sms -> sms.body.lowercase().contains(query) }
                }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(visibleThreads) { thread ->
                    SmsThreadItem(smsModel, thread)
                }
            }
        }
    }
}