package com.bnyro.contacts.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.NavBarItem
import com.bnyro.contacts.ui.components.ContactsPage
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.ui.models.ThemeModel
import com.bnyro.contacts.util.DeviceContactsHelper
import com.bnyro.contacts.util.LocalContactsHelper
import com.bnyro.contacts.util.Preferences
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainAppContent() {
    val context = LocalContext.current
    val contactsModel: ContactsModel = viewModel()
    val smsModel: SmsModel = viewModel()
    val themeModel: ThemeModel = viewModel()
    val scope = rememberCoroutineScope()

    var selectedTab by remember {
        mutableIntStateOf(Preferences.getInt(Preferences.homeTabKey, 0))
    }

    val bottomBarHeight = 80.dp
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.roundToPx().toFloat() }
    val bottomBarOffsetHeightPx = remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val newOffset = bottomBarOffsetHeightPx.value + available.y
                bottomBarOffsetHeightPx.value = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(Unit) {
        contactsModel.loadContacts(context)
    }

    LaunchedEffect(contactsModel.isLoading) {
        contactsModel.initialContactId ?: return@LaunchedEffect
        contactsModel.contacts.firstOrNull {
            it.contactId == contactsModel.initialContactId
        }?.let {
            scope.launch {
                withContext(Dispatchers.IO) {
                    contactsModel.initialContactData = contactsModel.loadAdvancedContactData(it)
                }
            }
        }
    }

    val navItems = listOf(
        NavBarItem(
            stringResource(R.string.device),
            Icons.Default.Home
        ) {
            contactsModel.contactsHelper = DeviceContactsHelper(context)
            contactsModel.loadContacts(context)
        },
        NavBarItem(
            stringResource(R.string.local),
            Icons.Default.Storage
        ) {
            contactsModel.contactsHelper = LocalContactsHelper(context)
            contactsModel.loadContacts(context)
        },
        NavBarItem(
            stringResource(R.string.messages),
            Icons.Default.Message
        ) {}
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = if (themeModel.collapsableBottomBar) {
                    Modifier
                        .height(bottomBarHeight)
                        .offset {
                            IntOffset(x = 0, y = -bottomBarOffsetHeightPx.value.roundToInt())
                        }
                } else {
                    Modifier
                },
                tonalElevation = 10.dp
            ) {
                navItems.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = index == selectedTab,
                        onClick = {
                            if (selectedTab == index) return@NavigationBarItem
                            selectedTab = index
                            navItem.onClick()
                        },
                        icon = {
                            Icon(navItem.icon, null)
                        },
                        label = {
                            Text(navItem.label)
                        }
                    )
                }
            }
        }
    ) { pV ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .let {
                    if (!themeModel.collapsableBottomBar) it.padding(pV) else it
                },
            color = MaterialTheme.colorScheme.background
        ) {
            if (selectedTab == 2) {
                SmsListScreen(smsModel, contactsModel)
            } else {
                ContactsPage(
                    contactsModel.initialContactData,
                    nestedScrollConnection.takeIf { themeModel.collapsableBottomBar },
                    bottomBarOffsetHeight = with(LocalDensity.current) {
                        bottomBarHeight - bottomBarOffsetHeightPx.value.absoluteValue.toDp()
                    }.takeIf { themeModel.collapsableBottomBar } ?: 0.dp
                )
            }
        }
        contactsModel.initialContactData?.let {
            SingleContactScreen(it) {
                contactsModel.initialContactData = null
            }
        }
    }
}
