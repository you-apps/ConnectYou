package com.bnyro.contacts.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
fun ContactsScreen() {
    val context = LocalContext.current
    val viewModel: ContactsModel = viewModel()
    val themeModel: ThemeModel = viewModel()
    val scope = rememberCoroutineScope()

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

    LaunchedEffect(viewModel.isLoading) {
        viewModel.initialContactId ?: return@LaunchedEffect
        viewModel.contacts.firstOrNull {
            it.contactId == viewModel.initialContactId
        }?.let {
            scope.launch {
                withContext(Dispatchers.IO) {
                    viewModel.initialContactData = viewModel.loadAdvancedContactData(it)
                }
            }
        }
    }

    val navItems = listOf(
        NavBarItem(
            stringResource(R.string.device),
            Icons.Default.Home
        ) {
            viewModel.contactsHelper = DeviceContactsHelper(context)
            viewModel.loadContacts(context)
        },
        NavBarItem(
            stringResource(R.string.local),
            Icons.Default.Storage
        ) {
            viewModel.contactsHelper = LocalContactsHelper(context)
            viewModel.loadContacts(context)
        }
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
                    if (!themeModel.collapsableBottomBar) it.padding(pV) else it
                },
            color = MaterialTheme.colorScheme.background
        ) {
            ContactsPage(
                viewModel.initialContactData,
                nestedScrollConnection.takeIf { themeModel.collapsableBottomBar },
                bottomBarOffsetHeight = with(LocalDensity.current) {
                    bottomBarHeight - bottomBarOffsetHeightPx.value.absoluteValue.toDp()
                }.takeIf { themeModel.collapsableBottomBar } ?: 0.dp
            )
        }
        viewModel.initialContactData?.let {
            SingleContactScreen(it) {
                viewModel.initialContactData = null
            }
        }
    }
}
