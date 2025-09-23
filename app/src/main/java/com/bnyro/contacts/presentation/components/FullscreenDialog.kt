package com.bnyro.contacts.presentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FullScreenDialog(onClose: () -> Unit, content: @Composable () -> Unit) {
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        onDismissRequest = onClose
    ) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        LaunchedEffect(dialogWindowProvider) {
            dialogWindowProvider.window.setDimAmount(0f)
        }

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            content.invoke()
        }
    }
}
