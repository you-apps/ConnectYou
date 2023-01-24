package com.bnyro.contacts.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContactEntry(label: String, content: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)

    ElevatedCard(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .clip(shape)
            .clickable {
                onClick.invoke()
            }
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = content)
        }
    }
}
