package com.bnyro.contacts.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bnyro.contacts.presentation.screens.calllog.CallLogsScreen
import com.bnyro.contacts.presentation.screens.contacts.ContactsPage
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.dialer.model.DialerModel
import com.bnyro.contacts.presentation.screens.settings.model.ThemeModel
import com.bnyro.contacts.presentation.screens.sms.SmsListScreen
import com.bnyro.contacts.presentation.screens.sms.model.SmsModel

@Composable
fun HomeNavHost(
    navController: NavHostController,
    onNavigate: (String) -> Unit,
    startTab: HomeRoutes,
    modifier: Modifier = Modifier,
    smsModel: SmsModel,
    contactsModel: ContactsModel,
    dialerModel: DialerModel,
    themeModel: ThemeModel
) {
    val viewModelStoreOwner: ViewModelStoreOwner = LocalViewModelStoreOwner.current!!

    NavHost(navController, startDestination = startTab.route, modifier = modifier) {
        composable(HomeRoutes.Contacts.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                ContactsPage(null,
                    onNavigate = {
                        onNavigate.invoke(it.route)
                    })
            }
        }
        composable(HomeRoutes.Phone.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                CallLogsScreen(contactsModel, dialerModel, themeModel)
            }
        }
        composable(HomeRoutes.Messages.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                SmsListScreen(
                    smsModel = smsModel,
                    contactsModel = contactsModel,
                    scrollConnection = null,
                    onNavigate = {
                        onNavigate.invoke(it.route)
                    },
                    onClickMessage = { address, contactData ->
                        smsModel.currentContactData = contactData
                        onNavigate.invoke("${NavRoutes.MessageThread.route}/$address")
                    }
                )
            }
        }
    }
}