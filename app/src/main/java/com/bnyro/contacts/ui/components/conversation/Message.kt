package com.bnyro.contacts.ui.components.conversation

import android.provider.Telephony
import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.db.obj.SmsData
import com.bnyro.contacts.ui.components.dialogs.ConfirmationDialog
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.util.generateAnnotations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.Messages(
    messages: List<SmsData>,
    scrollState: LazyListState,
    smsModel: SmsModel
) {
    LaunchedEffect(Unit) {
        scrollState.scrollToItem(messages.size + 5)
    }

    val timestamped = messages.groupBy {
        DateUtils.getRelativeDateTimeString(
            LocalContext.current,
            it.timestamp,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.WEEK_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_ALL
        ).split(", ").first()
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        timestamped.forEach { timestamp ->
            item {
                DayHeader(
                    timestamp.key
                )
            }
            items(items = timestamp.value) { smsData ->
                val isUserMe = smsData.type in listOf(
                    Telephony.Sms.MESSAGE_TYPE_DRAFT,
                    Telephony.Sms.MESSAGE_TYPE_SENT,
                    Telephony.Sms.MESSAGE_TYPE_OUTBOX
                )
                var showDeleteSmsDialog by remember {
                    mutableStateOf(false)
                }

                val state = rememberDismissState(
                    confirmValueChange = {
                        if (it == DismissValue.DismissedToEnd) {
                            showDeleteSmsDialog = true
                        }
                        return@rememberDismissState false
                    }
                )
                SwipeToDismiss(
                    state = state,
                    background = {},
                    dismissContent = {
                        Message(
                            msg = smsData,
                            isUserMe = isUserMe
                        )
                    }
                )
                if (showDeleteSmsDialog) {
                    val context = LocalContext.current
                    ConfirmationDialog(
                        onDismissRequest = { showDeleteSmsDialog = false },
                        title = stringResource(R.string.delete_message),
                        text = stringResource(R.string.irreversible)
                    ) {
                        smsModel.deleteSms(context, smsData.id, smsData.threadId)
                    }
                }
            }
        }
    }
}

@Composable
fun Message(
    msg: SmsData,
    isUserMe: Boolean
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        ChatItemBubble(msg, isUserMe)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

private val leftChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
private val rightChatBubbleShape = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)

@Composable
fun DayHeader(dayString: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(16.dp)
    ) {
        DayHeaderLine()
        Text(
            text = dayString,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DayHeaderLine()
    }
}

@Composable
private fun RowScope.DayHeaderLine() {
    Divider(
        modifier = Modifier
            .weight(1f)
            .align(Alignment.CenterVertically),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

@Composable
fun ChatItemBubble(
    message: SmsData,
    isUserMe: Boolean
) {
    val backgroundBubbleColor = if (isUserMe) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(100.dp)
    } else {
        MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
    }

    val textColor =
        MaterialTheme.colorScheme.primary

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUserMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Surface(
            modifier = if (isUserMe) {
                Modifier.padding(start = 40.dp)
            } else {
                Modifier.padding(
                    end = 40.dp
                )
            },
            color = backgroundBubbleColor,
            shape = if (isUserMe) rightChatBubbleShape else leftChatBubbleShape
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ClickableMessage(
                    smsData = message
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.align(if (isUserMe) Alignment.Start else Alignment.End)) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        text = DateUtils.getRelativeDateTimeString(
                            LocalContext.current,
                            message.timestamp,
                            DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.WEEK_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL
                        ).split(", ")[1]
                    )
                    message.simNumber?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            text = "SIM $it"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClickableMessage(
    smsData: SmsData
) {
    SelectionContainer {
        val uriHandler = LocalUriHandler.current
        val primary = MaterialTheme.colorScheme.primary

        val formatted = remember {
            generateAnnotations(smsData.body, primary)
        }

        ClickableText(
            text = formatted,
            style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
            onClick = { offset ->
                val annotation =
                    formatted.getStringAnnotations(offset, offset).firstOrNull()
                annotation?.let {
                    uriHandler.openUri(it.item)
                }
            }
        )
    }
}
