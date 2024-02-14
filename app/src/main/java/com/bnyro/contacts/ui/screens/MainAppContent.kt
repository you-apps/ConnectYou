package com.bnyro.contacts.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.NavBarItem
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.ui.models.ThemeModel
import com.bnyro.contacts.util.Preferences

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainAppContent(smsModel: SmsModel) {
    val themeModel: ThemeModel = viewModel()
    val contactsModel: ContactsModel = viewModel(factory = ContactsModel.Factory)

    val bottomBarHeight = 80.dp
    val bottomBarHeightPx = LocalDensity.current.run { bottomBarHeight.toPx() }
    var bottomBarOffset by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val newOffset = bottomBarOffset + available.y
                bottomBarOffset = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    val navItems = listOf(
        NavBarItem(
            stringResource(R.string.contacts),
            Icons.Rounded.People
        ),
        NavBarItem(
            stringResource(R.string.messages),
            Icons.Rounded.Message
        )
    )

    var currentPage by remember {
        mutableIntStateOf(
            smsModel.initialAddressAndBody?.let { 1 } ?: Preferences.getInt(
                Preferences.homeTabKey,
                0
            )
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = if (themeModel.collapsableBottomBar) {
                    Modifier
                        .clipToBounds()
                        .height(
                            LocalDensity.current.run {
                                (bottomBarHeightPx + bottomBarOffset).toDp()
                            }
                        )
                } else {
                    Modifier
                },
                tonalElevation = 10.dp
            ) {
                navItems.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = (index == currentPage),
                        onClick = {
                            currentPage = index
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
                .padding(pV),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(targetState = currentPage, label = "crossfade pager") { index ->
                val scrollConnectionIfEnabled = nestedScrollConnection.takeIf { themeModel.collapsableBottomBar }

                when (index) {
                    0 -> ContactsPage(scrollConnectionIfEnabled)

                    1 -> SmsListScreen(smsModel, contactsModel, scrollConnectionIfEnabled)
                }
            }
        }
    }
}
