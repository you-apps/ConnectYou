package com.bnyro.contacts.ui.activities

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Data
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
import com.bnyro.contacts.domain.enums.SortOrder
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.presentation.screens.contacts.components.ContactItem
import com.bnyro.contacts.ui.theme.ConnectYouTheme

class PickContactActivity : BaseActivity() {

    private var specialMimeType: String? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        specialMimeType = when (intent.data) {
            Email.CONTENT_URI -> Email.CONTENT_ITEM_TYPE
            Phone.CONTENT_URI -> Phone.CONTENT_ITEM_TYPE
            else -> null
        }

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
                                items(contactsModel.contacts, key = ContactData::contactId) {
                                    ContactItem(
                                        modifier = Modifier.padding(horizontal = 10.dp),
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
            val uri = when {
                specialMimeType != null -> {
                    val contactId = getContactMimeTypeId(
                        this,
                        contact.contactId.toString(),
                        specialMimeType!!
                    )
                    Uri.withAppendedPath(Data.CONTENT_URI, contactId)
                }

                else -> ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI,
                    contact.contactId
                )
            }
            val intent = Intent().apply {
                data = uri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            setResult(Activity.RESULT_OK, intent)
        }
        finish()
    }

    private fun getContactMimeTypeId(
        context: Context,
        contactId: String,
        mimeType: String
    ): String {
        val uri = Data.CONTENT_URI
        val projection = arrayOf(Data._ID, Data.RAW_CONTACT_ID, Data.MIMETYPE)
        val selection = "${Data.MIMETYPE} = ? AND ${Data.RAW_CONTACT_ID} = ?"
        val selectionArgs = arrayOf(mimeType, contactId)

        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(Data._ID)
                return it.getString(index)
            }
        }
        return ""
    }

}
