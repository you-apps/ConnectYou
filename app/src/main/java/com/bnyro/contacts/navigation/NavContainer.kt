package com.bnyro.contacts.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.settings.model.ThemeModel
import com.bnyro.contacts.presentation.screens.sms.model.SmsModel

@Composable
fun NavContainer(
    initialTab: HomeRoutes,
) {
    val navController = rememberNavController()
    val smsModel: SmsModel = viewModel()
    val contactsModel: ContactsModel = viewModel(factory = ContactsModel.Factory)
    val themeModel: ThemeModel = viewModel()
    AppNavHost(
        navController,
        initialTab = initialTab,
        modifier = Modifier
            .fillMaxSize(),
        smsModel = smsModel,
        contactsModel = contactsModel,
        themeModel = themeModel
    )
}
