package com.bnyro.contacts.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.NavBarItem
import com.bnyro.contacts.ui.components.ContactsPage
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.util.DeviceContactsHelper
import com.bnyro.contacts.util.LocalContactsHelper
import com.bnyro.contacts.util.Preferences
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    showEditorDefault: Boolean,
    initialContact: Long?
) {
    val context = LocalContext.current
    val viewModel: ContactsModel = viewModel()
    var visibleContact by remember {
        mutableStateOf<ContactData?>(null)
    }
    val scope = rememberCoroutineScope()
    val collapseBottomBar = Preferences.getBoolean(Preferences.collapseBottomBarKey, false)

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
        viewModel.loadContacts(context)
    }

    LaunchedEffect(viewModel.contacts) {
        initialContact ?: return@LaunchedEffect
        viewModel.contacts?.firstOrNull {
            it.contactId == initialContact
        }?.let {
            scope.launch {
                withContext(Dispatchers.IO) {
                    visibleContact = viewModel.loadAdvancedContactData(it)
                }
            }
        }
    }

    val navItems = listOf(
        NavBarItem(
            stringResource(R.string.device),
            Icons.Default.Home
        ) {
            viewModel.contacts = null
            viewModel.contactsHelper = DeviceContactsHelper(context)
            viewModel.loadContacts(context)
        },
        NavBarItem(
            stringResource(R.string.local),
            Icons.Default.Storage
        ) {
            viewModel.contacts = null
            viewModel.contactsHelper = LocalContactsHelper(context)
            viewModel.loadContacts(context)
        }
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = if (collapseBottomBar) {
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
                var selected by remember {
                    mutableStateOf(Preferences.getInt(Preferences.homeTabKey, 0))
                }
                navItems.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = index == selected,
                        onClick = {
                            if (selected == index) return@NavigationBarItem
                            selected = index
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
                    if (!collapseBottomBar) it.padding(pV) else it
                },
            color = MaterialTheme.colorScheme.background
        ) {
            ContactsPage(
                viewModel.contacts,
                showEditorDefault,
                nestedScrollConnection.takeIf { collapseBottomBar }
            )
        }
        visibleContact?.let {
            SingleContactScreen(it) {
                visibleContact = null
            }
        }
    }
}
