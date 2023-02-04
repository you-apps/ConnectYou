package com.bnyro.contacts.obj

import androidx.compose.ui.graphics.vector.ImageVector

data class NavBarItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
