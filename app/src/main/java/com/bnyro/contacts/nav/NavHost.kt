package com.bnyro.contacts.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.DialerModel
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.ui.models.ThemeModel
import com.bnyro.contacts.ui.screens.AboutScreen
import com.bnyro.contacts.ui.screens.CallLogsScreen
import com.bnyro.contacts.ui.screens.ContactsPage
import com.bnyro.contacts.ui.screens.SettingsScreen
import com.bnyro.contacts.ui.screens.SmsListScreen
import com.bnyro.contacts.ui.screens.SmsThreadScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: NavRoutes,
    modifier: Modifier = Modifier
) {
    val smsModel: SmsModel = viewModel(factory = SmsModel.Factory)
    val contactsModel: ContactsModel = viewModel(factory = ContactsModel.Factory)
    val dialerModel: DialerModel = viewModel()
    val themeModel: ThemeModel = viewModel()

    val viewModelStoreOwner: ViewModelStoreOwner = LocalViewModelStoreOwner.current!!

    NavHost(navController, startDestination = startDestination.route, modifier = modifier) {
        composable(NavRoutes.About.route) {
            AboutScreen {
                navController.popBackStack()
            }
        }
        composable(NavRoutes.Settings.route) {
            SettingsScreen(themeModel = themeModel, smsModel = smsModel) {
                navController.popBackStack()
            }
        }
        composable(NavRoutes.Contacts.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                ContactsPage(null,
                    onNavigate = {
                        navController.navigate(it.route)
                    })
            }
        }
        composable(NavRoutes.Phone.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                CallLogsScreen(contactsModel, dialerModel, themeModel)
            }
        }
        composable(NavRoutes.Messages.route) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                SmsListScreen(
                    smsModel = smsModel,
                    contactsModel = contactsModel,
                    scrollConnection = null,
                    onNavigate = {
                        navController.navigate(it.route)
                    },
                    onClickMessage = { address, contactData ->
                        smsModel.currentContactData = contactData
                        navController.navigate("${NavRoutes.MessageThread.route}/$address")
                    }
                )
            }
        }
        composable(
            "${NavRoutes.MessageThread.route}/{address}",
            listOf(navArgument("address") { type = NavType.StringType })
        ) {
            val address = it.arguments?.getString("address")
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                SmsThreadScreen(
                    smsModel = smsModel,
                    contactsModel = contactsModel,
                    contactsData = remember { smsModel.currentContactData },
                    address = address.orEmpty()
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}
