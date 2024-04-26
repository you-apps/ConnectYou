package com.bnyro.contacts.presentation.screens.about.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.util.ClipboardHelper
import com.bnyro.contacts.util.IntentHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutRow(
    title: String,
    summary: String? = null,
    url: String? = null,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .combinedClickable(
                onClick = {
                    if (url != null) {
                        IntentHelper.openUrl(context, url)
                    } else {
                        onClick.invoke()
                    }
                },
                onLongClick = {
                    ClipboardHelper(context).save(summary ?: title)
                }
            )
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )

        summary?.let {
            Spacer(Modifier.height(1.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
