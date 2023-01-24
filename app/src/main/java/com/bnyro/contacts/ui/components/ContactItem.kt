package com.bnyro.contacts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.screens.SingleContactScreen

@Composable
fun ContactItem(contact: ContactData) {
    val shape = RoundedCornerShape(20.dp)
    var showContactScreen by remember {
        mutableStateOf(false)
    }

    ElevatedCard(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .clip(shape)
            .clickable {
                showContactScreen = true
            },
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(shape = CircleShape, color = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = (contact.displayName?.firstOrNull() ?: "").toString(),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(contact.displayName.orEmpty())
        }
    }

    if (showContactScreen) {
        SingleContactScreen(contact) {
            showContactScreen = false
        }
    }
}
