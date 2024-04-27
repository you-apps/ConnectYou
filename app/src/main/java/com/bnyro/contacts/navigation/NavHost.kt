package com.bnyro.contacts.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bnyro.contacts.presentation.screens.about.AboutScreen
import com.bnyro.contacts.presentation.screens.calllog.model.CallModel
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.settings.SettingsScreen
import com.bnyro.contacts.presentation.screens.settings.model.ThemeModel
import com.bnyro.contacts.presentation.screens.sms.SmsThreadScreen
import com.bnyro.contacts.presentation.screens.sms.model.SmsModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialTab: HomeRoutes,
    smsModel: SmsModel,
    contactsModel: ContactsModel,
    callModel: CallModel,
    themeModel: ThemeModel
) {
    val viewModelStoreOwner: ViewModelStoreOwner = LocalViewModelStoreOwner.current!!

    NavHost(navController, startDestination = NavRoutes.Home.route, modifier = modifier) {
        composable(NavRoutes.Home.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    initialOffset = { it / 4 }
                ) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up,
                    targetOffset = { it / 4 }) + fadeOut()
            }) {
            HomeNavContainer(
                initialTab = initialTab,
                onNavigate = {
                    navController.navigate(it)
                },
                smsModel = smsModel,
                contactsModel = contactsModel,
                callModel = callModel,
                themeModel = themeModel
            )
        }
        composable(NavRoutes.About.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    initialOffset = { it / 4 }) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    targetOffset = { it / 4 }) + fadeOut()
            }) {
            AboutScreen {
                navController.popBackStack()
            }
        }
        composable(NavRoutes.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    initialOffset = { it / 4 }) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    targetOffset = { it / 4 }) + fadeOut()
            }) {
            SettingsScreen(themeModel = themeModel, smsModel = smsModel) {
                navController.popBackStack()
            }
        }
        composable(
            "${NavRoutes.MessageThread.route}/{address}",
            listOf(navArgument("address") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    initialOffset = { it / 4 }) + fadeIn()
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    targetOffset = { it / 4 }) + fadeOut()
            }
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
