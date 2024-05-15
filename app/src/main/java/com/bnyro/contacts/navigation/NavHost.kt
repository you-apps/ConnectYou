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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bnyro.contacts.presentation.screens.about.AboutScreen
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
    themeModel: ThemeModel
) {
    val viewModelStoreOwner: ViewModelStoreOwner = LocalViewModelStoreOwner.current!!

    NavHost(navController, startDestination = NavRoutes.Home, modifier = modifier) {
        composable<NavRoutes.Home>(
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
                themeModel = themeModel
            )
        }
        composable<NavRoutes.About>(
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
        composable<NavRoutes.Settings>(
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
        composable<NavRoutes.MessageThread>(
            deepLinks = NavRoutes.MessageThread.deepLinks,
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
            val thread = it.toRoute<NavRoutes.MessageThread>()
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                SmsThreadScreen(
                    smsModel = smsModel,
                    contactsModel = contactsModel,
                    contactsData = remember { smsModel.currentContactData },
                    address = thread.address,
                    initialText = thread.body.orEmpty()
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}
