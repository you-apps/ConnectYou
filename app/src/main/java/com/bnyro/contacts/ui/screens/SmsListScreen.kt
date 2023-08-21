package com.bnyro.contacts.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.util.SmsUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsListScreen(smsModel: SmsModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        smsModel.fetchSmsList(context)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(smsModel.smsList.entries.toList()) { (threadId, smsList) ->
            val dismissState = rememberDismissState(
                confirmValueChange = {
                    if (it == DismissValue.DismissedToEnd) {
                        SmsUtil.deleteThread(context, threadId)
                        return@rememberDismissState true
                    }
                    return@rememberDismissState false
                }
            )

            SwipeToDismiss(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                state = dismissState,
                background = {},
                dismissContent = {
                    ElevatedCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    modifier = Modifier
                                        .padding(15.dp)
                                        .fillMaxSize(),
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Text(
                                    text = smsList.last().address,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = smsList.last().body,
                                    maxLines = 2,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                directions = setOf(DismissDirection.StartToEnd)
            )
        }
    }
}