package com.bnyro.contacts.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import androidx.navigation.navDeepLink
import com.bnyro.contacts.R
import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoutes {
    @Serializable
    data object Home : NavRoutes()

    @Serializable
    data object About : NavRoutes()

    @Serializable
    data object Settings : NavRoutes()

    @Serializable
    data class MessageThread(
        val address: String?
    ) : NavRoutes()
}

@Serializable
sealed class HomeRoutes() {
    @Serializable
    data class Phone(
        val phoneNumber: String? = null
    ) : HomeRoutes() {
        companion object {
            const val phoneNumber = "phoneNumber"
            const val deepLink = "connectyou://dial/{$phoneNumber}"
            const val navAction = "com.bnyro.contacts.DIAL"
            val deepLinks = listOf(navDeepLink {
                uriPattern = deepLink
                action = navAction
            })

            fun getDeepLink(number: String) = "connectyou://dial/$number".toUri()
        }
    }

    @Serializable
    data object Contacts : HomeRoutes()

    @Serializable
    data object Messages : HomeRoutes()

    companion object {
        val all = listOf(
            HomeNavBarItem(Phone(), R.string.dial, Icons.Rounded.Phone),
            HomeNavBarItem(Contacts, R.string.contacts, Icons.Rounded.Person),
            HomeNavBarItem(Messages, R.string.messages, Icons.AutoMirrored.Rounded.Message)
        )
    }
}

data class HomeNavBarItem(
    val route: HomeRoutes,
    @StringRes val stringRes: Int,
    val icon: ImageVector
)