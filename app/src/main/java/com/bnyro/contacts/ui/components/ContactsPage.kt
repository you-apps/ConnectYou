package com.bnyro.contacts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.obj.ContactData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsPage(contacts: List<ContactData>) {
    LazyColumn {
        items(contacts) {
            val shape = RoundedCornerShape(20.dp)
            ElevatedCard(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    .fillMaxWidth()
                    .clip(shape),
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
                            text = (it.displayName?.firstOrNull() ?: "").toString(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(it.displayName ?: "")
                }
            }
        }
    }
}
