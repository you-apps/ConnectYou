package com.bnyro.contacts.presentation.screens.sms

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
import androidx.compose.runtime.LaunchedEffect
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
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.SmsThread
import com.bnyro.contacts.navigation.NavRoutes
import com.bnyro.contacts.presentation.components.ClickableIcon
import com.bnyro.contacts.presentation.components.NothingHere
import com.bnyro.contacts.presentation.components.TopBarMoreMenu
import com.bnyro.contacts.presentation.features.NumberPickerDialog
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.sms.components.SmsSearchScreen
import com.bnyro.contacts.presentation.screens.sms.components.SmsThreadItem
import com.bnyro.contacts.presentation.screens.sms.model.SmsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsListScreen(
    smsModel: SmsModel,
    contactsModel: ContactsModel,
    scrollConnection: NestedScrollConnection?,
    onNavigate: (NavRoutes) -> Unit,
    onClickMessage: (address: String, contactData: ContactData?) -> Unit
) {
    var showNumberPicker by remember {
        mutableStateOf(false)
    }
    var showSearch by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        smsModel.initialAddressAndBody?.let {
            onClickMessage(it.first, null)
        }
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
                    TopBarMoreMenu(options = listOf(
                        stringResource(R.string.settings),
                        stringResource(R.string.about)
                    ),
                        onOptionClick = { index ->
                            when (index) {
                                0 -> {
                                    onNavigate.invoke(NavRoutes.Settings)
                                }

                                1 -> {
                                    onNavigate.invoke(NavRoutes.About)
                                }
                            }
                        })
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
                    SmsThreadItem(smsModel, thread, onClick = onClickMessage)
                }
            }
            if (showSearch) {
                SmsSearchScreen(smsModel, threadList, { showSearch = false }, onClickMessage)
            }
        } else {
            Column(Modifier.padding(pv)) {
                NothingHere()
            }
        }

        if (showNumberPicker) {
            NumberPickerDialog(
                contactsModel,
                onDismissRequest = { showNumberPicker = false },
                onNumberSelect = onClickMessage
            )
        }
    }
}
