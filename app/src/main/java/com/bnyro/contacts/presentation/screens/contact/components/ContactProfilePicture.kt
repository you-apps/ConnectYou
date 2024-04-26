package com.bnyro.contacts.presentation.screens.contact.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.presentation.components.ClickableIcon
import com.bnyro.contacts.presentation.components.FullScreenDialog
import com.bnyro.contacts.presentation.components.ZoomableImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactProfilePicture(
    contact: ContactData,
    onDismissRequest: () -> Unit
) {
    FullScreenDialog(onClose = onDismissRequest) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        ClickableIcon(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = R.string.okay
                        ) {
                            onDismissRequest.invoke()
                        }
                    },
                    title = {
                        Text(contact.displayName.orEmpty())
                    }
                )
            }
        ) { pV ->
            ZoomableImage(
                modifier = Modifier.padding(pV),
                bitmap = contact.photo!!
            )
        }
    }
}
