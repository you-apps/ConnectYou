package com.bnyro.contacts.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import androidx.navigation.navDeepLink
import com.bnyro.contacts.R

sealed class NavRoutes(
    val route: String
) {
    object Home : NavRoutes("home")
    object About : NavRoutes("about")
    object Settings : NavRoutes("settings")
    object MessageThread : NavRoutes("message_thread")
}

sealed class HomeRoutes(
    val route: String,
    @StringRes val stringRes: Int,
    val icon: ImageVector
) {
    object Phone : HomeRoutes("phone", R.string.dial, Icons.Rounded.Phone) {
        val phoneNumber = "phoneNumber"
        val deepLink = "connectyou://dial/{$phoneNumber}"
        val navAction = "com.bnyro.contacts.DIAL"
        val deepLinks = listOf(navDeepLink {
            uriPattern = deepLink
            action = navAction
        })

        fun getDeepLink(number: String) = "connectyou://dial/$number".toUri()
    }

    object Contacts : HomeRoutes("contacts", R.string.contacts, Icons.Rounded.Person)
    object Messages : HomeRoutes("messages", R.string.messages, Icons.Rounded.Message)

    companion object {
        val all = listOf(
            Phone,
            Contacts,
            Messages
        )
    }
}