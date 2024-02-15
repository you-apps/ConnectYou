package com.bnyro.contacts.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.SmsThread
import com.bnyro.contacts.ui.components.NothingHere
import com.bnyro.contacts.ui.components.NumberPickerDialog
import com.bnyro.contacts.ui.components.SmsThreadItem
import com.bnyro.contacts.ui.components.TopBarMoreMenu
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.SmsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsListScreen(
    smsModel: SmsModel,
    contactsModel: ContactsModel,
    scrollConnection: NestedScrollConnection?
) {
    var showNumberPicker by remember {
        mutableStateOf(false)
    }

    var smsAddress by remember {
        mutableStateOf<String?>(null)
    }

    var showSearch by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.messages))
                },
                actions = {
                    ClickableIcon(
                        icon = Icons.Default.Search,
                        contentDescription = R.string.search
                    ) {
                        showSearch = true
                    }
                    TopBarMoreMenu()
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showNumberPicker = true
                }
            ) {
                Icon(Icons.Default.Edit, null)
            }
        }) { pv ->
        val smsList by smsModel.smsList.collectAsState()
        if (smsList.isNotEmpty()) {
            val threadList = smsList.groupBy { it.threadId }
                .map { (threadId, smsList) ->
                    val address = smsList.first().address
                    SmsThread(
                        threadId = threadId,
                        contactData = contactsModel.getContactByNumber(address),
                        address = address,
                        smsList = smsList
                    )
                }
                .sortedBy { thread -> thread.smsList.maxOf { it.timestamp } }
                .reversed()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv)
                    .let { modifier ->
                        scrollConnection?.let { modifier.nestedScroll(it) } ?: modifier
                    }
            ) {
                items(threadList) { thread ->
                    SmsThreadItem(smsModel, thread)
                }
            }
            if (showSearch) {
                SmsSearchScreen(smsModel, threadList) {
                    showSearch = false
                }
            }
        } else {
            Column(Modifier.padding(pv)) {
                NothingHere()
            }
        }

        if (showNumberPicker) {
            NumberPickerDialog(
                onDismissRequest = { showNumberPicker = false },
                onNumberSelect = {
                    smsAddress = it
                }
            )
        }

        smsAddress?.let {
            val contactData = contactsModel.getContactByNumber(it)
            SmsThreadScreen(smsModel, contactData, it) {
                smsAddress = null
            }
        }

        smsModel.initialAddressAndBody?.let {
            val contactData = contactsModel.getContactByNumber(it.first)
            SmsThreadScreen(smsModel, contactData, it.first) {
                smsModel.initialAddressAndBody = null
            }
        }
    }
}
