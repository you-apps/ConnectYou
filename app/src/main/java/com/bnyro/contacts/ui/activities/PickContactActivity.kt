package com.bnyro.contacts.ui.activities

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.ContactsContract
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.SortOrder
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.ContactItem
import com.bnyro.contacts.ui.theme.ConnectYouTheme

class PickContactActivity : BaseActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contactsModel.loadContacts(this)

        setContent {
            ConnectYouTheme(themeModel.themeMode) {
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                Scaffold(
                    topBar = {
                        LargeTopAppBar(
                            title = { Text(stringResource(R.string.pick_contact)) },
                            scrollBehavior = scrollBehavior
                        )
                    }
                ) { pV ->
                    Surface(
                        modifier = Modifier
                            .padding(pV)
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                items(contactsModel.contacts) {
                                    ContactItem(
                                        contact = it,
                                        sortOrder = SortOrder.FIRSTNAME,
                                        selected = false,
                                        onSinglePress = {
                                            sendResult(it)
                                            true
                                        },
                                        onLongPress = {}
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                OutlinedButton(onClick = { sendResult(null) }) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendResult(contact: ContactData?) {
        if (contact == null) {
            setResult(Activity.RESULT_CANCELED, Intent())
        } else {
            val intent = Intent().apply {
                data = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI,
                    contact.contactId
                )
            }
            setResult(Activity.RESULT_OK, intent)
        }
        finish()
    }
}