package com.bnyro.contacts.ui.components.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.TranslatedType
import com.bnyro.contacts.obj.ValueWithType
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.dialogs.DialogButton

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorEntry(
    state: MutableState<ValueWithType>,
    types: List<TranslatedType>,
    showDeleteAction: Boolean,
    onCreateNew: () -> Unit,
    onDelete: () -> Unit,
    moveToTop: () -> Unit = {},
    content: @Composable RowScope.() -> Unit
) {
    var showTypesDialog by remember {
        mutableStateOf(false)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f)
        ) {
            content.invoke(this)
        }
        Column(
            modifier = Modifier.width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (types.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .offset(y = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .combinedClickable(
                            onClick = {
                                showTypesDialog = true
                            },
                            onLongClick = {
                                moveToTop.invoke()
                            }
                        )
                        .padding(10.dp),
                    text = types.firstOrNull {
                        it.id == state.value.type
                    }?.title?.let { stringResource(it) }.orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row {
                ClickableIcon(
                    modifier = Modifier.offset(x = if (showDeleteAction) 5.dp else 0.dp),
                    icon = Icons.Default.Add
                ) {
                    onCreateNew.invoke()
                }
                if (showDeleteAction) {
                    ClickableIcon(
                        modifier = Modifier.offset(x = (-5).dp),
                        icon = Icons.Default.Remove
                    ) {
                        onDelete.invoke()
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(5.dp))
    }

    if (showTypesDialog) {
        AlertDialog(
            onDismissRequest = {
                showTypesDialog = false
            },
            confirmButton = {
                DialogButton(text = stringResource(R.string.cancel)) {
                    showTypesDialog = false
                }
            },
            title = {
                Text(stringResource(R.string.type))
            },
            text = {
                LazyColumn {
                    items(types) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    state.value = state.value.also { v -> v.type = it.id }
                                    showTypesDialog = false
                                }
                        ) {
                            RadioButton(selected = it.id == state.value.type, onClick = {
                                state.value = state.value.also { v -> v.type = it.id }
                                showTypesDialog = false
                            })
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(stringResource(it.title))
                        }
                    }
                }
            }
        )
    }
}
