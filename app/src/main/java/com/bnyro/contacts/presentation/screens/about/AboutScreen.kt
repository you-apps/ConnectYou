package com.bnyro.contacts.presentation.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.BuildConfig
import com.bnyro.contacts.R
import com.bnyro.contacts.presentation.components.ClickableIcon
import com.bnyro.contacts.presentation.screens.about.components.AboutRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBackPress: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.about))
                },
                navigationIcon = {
                    ClickableIcon(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = R.string.okay
                    ) {
                        onBackPress.invoke()
                    }
                }
            )
        }
    ) { pV ->
        Column(
            modifier = Modifier.padding(pV),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
            Spacer(Modifier.height(10.dp))
            AboutRow(
                title = stringResource(R.string.version),
                summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            )
            AboutRow(
                title = "GitHub",
                url = "https://github.com/you-apps/ConnectYou/"
            )
            AboutRow(
                title = stringResource(R.string.author),
                summary = "You Apps",
                url = "https://github.com/you-apps/"
            )
            AboutRow(
                title = stringResource(R.string.translation),
                summary = "Weblate",
                url = "https://hosted.weblate.org/projects/you-apps/connect-you/"
            )
            AboutRow(
                title = stringResource(R.string.license),
                summary = "GPL-3.0",
                url = "https://www.gnu.org/licenses/gpl-3.0.html"
            )
        }
    }

}
