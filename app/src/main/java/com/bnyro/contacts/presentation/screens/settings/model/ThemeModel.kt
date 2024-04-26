package com.bnyro.contacts.presentation.screens.settings.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.contacts.domain.enums.ThemeMode
import com.bnyro.contacts.util.Preferences

class ThemeModel : ViewModel() {
    var themeMode by mutableStateOf(
        ThemeMode.fromInt(
            Preferences.getInt(Preferences.themeKey, ThemeMode.SYSTEM.ordinal)
        )
    )
    var collapsableBottomBar by mutableStateOf(
        Preferences.getBoolean(Preferences.collapseBottomBarKey, false)
    )
    var colorfulIcons by mutableStateOf(
        Preferences.getBoolean(Preferences.colorfulContactIconsKey, false)
    )
}
