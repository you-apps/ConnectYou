package com.bnyro.contacts.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.ext.removeLastChar

val phoneNumberInputs = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")

@Composable
fun NumberInput(
    initialNumber: String,
    onNumberChange: (String) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.Center
    ) {
        items(phoneNumberInputs) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val interactionSource = remember {
                    MutableInteractionSource()
                }
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(interactionSource, indication = null) {
                            onNumberChange(initialNumber + it)
                        }
                        .padding(20.dp),
                    text = it,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {}
        item {}
        item {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = { onNumberChange(initialNumber.removeLastChar()) }
                ) {
                    Icon(Icons.Default.Backspace, null)
                }
            }
        }
    }
}