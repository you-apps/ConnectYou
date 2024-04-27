package com.bnyro.contacts.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.ui.graphics.vector.ImageVector
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
    object Phone : HomeRoutes("phone", R.string.dial, Icons.Rounded.Phone)
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