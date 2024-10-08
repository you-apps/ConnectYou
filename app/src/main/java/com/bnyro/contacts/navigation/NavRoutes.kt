package com.bnyro.contacts.navigation

import android.net.Uri
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
import java.net.URLEncoder

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
        val address: String,
        val body: String? = null
    ) : NavRoutes() {
        companion object {
            const val address = "address"
            const val body = "body"
            private const val basePath = "connectyou://message"
            const val navAction = "com.bnyro.contacts.SMS"
            val deepLinks = listOf(navDeepLink<MessageThread>(basePath) {
                action = navAction
            })


            fun getDeepLink(number: String, bodyText: String? = null): Uri {
                return if (bodyText == null) {
                    "connectyou://message/${
                        URLEncoder.encode(
                            number,
                            "UTF-8"
                        )
                    }".toUri()
                } else {
                    "connectyou://message/${
                        URLEncoder.encode(
                            number,
                            "UTF-8"
                        )
                    }?body=${URLEncoder.encode(bodyText, "UTF-8")}".toUri()
                }
            }
        }
    }
}

@Serializable
sealed class HomeRoutes {
    @Serializable
    data class Phone(
        val phoneNumber: String? = null
    ) : HomeRoutes() {
        companion object {
            const val phoneNumber = "phoneNumber"
            private const val basePath = "connectyou://dial"
            const val navAction = "com.bnyro.contacts.DIAL"
            val deepLinks = listOf(navDeepLink<Phone>(basePath) {
                action = navAction
            })

            fun getDeepLink(number: String) = "connectyou://dial?phoneNumber=$number".toUri()
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