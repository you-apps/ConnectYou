package com.bnyro.contacts.presentation.screens.sms.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.SmsThread
import com.bnyro.contacts.presentation.features.ConfirmationDialog
import com.bnyro.contacts.presentation.screens.sms.model.SmsModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SmsThreadItem(
    smsModel: SmsModel,
    thread: SmsThread,
    onClick: (address: String, contactData: ContactData?) -> Unit,
    onLongClick: (thread: SmsThread) -> Unit = {}
) {
    val context = LocalContext.current

    var showDeleteThreadDialog by remember {
        mutableStateOf(false)
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd) {
                showDeleteThreadDialog = true
            }
            return@rememberSwipeToDismissBoxState false
        }
    )

    SwipeToDismissBox(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        state = dismissState,
        backgroundContent = {},
        content = {
            val shape = RoundedCornerShape(20.dp)

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .combinedClickable(onClick = {
                        onClick.invoke(thread.address, thread.contactData)
                    }, onLongClick = {
                        onLongClick.invoke(thread)
                    }),
                shape = shape
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (thread.contactData?.thumbnail != null) {
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                bitmap = thread.contactData.thumbnail!!.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxSize(),
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                        10.dp
                                    )
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = thread.contactData?.displayName ?: thread.address,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            // safe call to avoid crashes when re-rendering
                            text = thread.smsList.lastOrNull()?.body.orEmpty(),
                            maxLines = 2,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        enableDismissFromStartToEnd = true
    )

    if (showDeleteThreadDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteThreadDialog = false },
            title = stringResource(R.string.delete_thread),
            text = stringResource(R.string.irreversible)
        ) {
            smsModel.deleteThread(context, thread.threadId)
        }
    }
}