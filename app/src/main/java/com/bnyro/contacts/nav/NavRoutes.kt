package com.bnyro.contacts.nav

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.bnyro.contacts.R

sealed class NavRoutes(
    val route: String,
    @StringRes val stringRes: Int? = null,
    val icon: ImageVector? = null
) {
    object About : NavRoutes("about", null, null)
    object Settings : NavRoutes("settings", null, null)
    object Contacts : NavRoutes("contacts", R.string.contacts, Icons.Rounded.Person)
    object Messages : NavRoutes("messages", R.string.messages, Icons.Rounded.Message)
    object MessageThread : NavRoutes("message_thread", null, null)
}

val allRoutes = listOf(
    NavRoutes.About,
    NavRoutes.Settings,
    NavRoutes.Contacts,
    NavRoutes.Messages,
    NavRoutes.MessageThread,
)