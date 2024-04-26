package com.bnyro.contacts.presentation.screens.editor.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.TranslatedType
import com.bnyro.contacts.domain.model.ValueWithType
import com.bnyro.contacts.presentation.features.DialogButton
import com.bnyro.contacts.util.CalendarUtils
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerEditor(
    @StringRes label: Int,
    state: MutableState<ValueWithType>,
    types: List<TranslatedType>,
    showDeleteAction: Boolean,
    onDelete: () -> Unit,
    shape: Shape,
) {
    val datePickerOffset = remember {
        TimeZone.getDefault().rawOffset
    }

    var showPicker by remember {
        mutableStateOf(false)
    }
    val datePickerState = rememberDatePickerState(
        runCatching {
            CalendarUtils.dateToMillis(state.value.value)?.plus(datePickerOffset)
        }.getOrDefault(null)
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        state.value.value = datePickerState.selectedDateMillis?.let {
            CalendarUtils.millisToDate(it.minus(datePickerOffset), CalendarUtils.isoDateFormat)
        }.orEmpty()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .defaultMinSize(
                minWidth = TextFieldDefaults.MinWidth,
                minHeight = TextFieldDefaults.MinHeight
            )
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            imageVector = Icons.Outlined.Event,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = stringResource(label),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedButton(
                onClick = { showPicker = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text(text = datePickerState.selectedDateMillis
                    ?.let { CalendarUtils.millisToDate(it.minus(datePickerOffset)) }
                    ?.takeIf { state.value.value.isNotEmpty() }
                    ?: stringResource(R.string.date))
            }
        }

        if (types.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }
            Row(
                Modifier
                    .padding(horizontal = 8.dp)
                    .clickable {
                        expanded = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = types.firstOrNull {
                        it.id == state.value.type
                    }?.title?.let { stringResource(it) }.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    types.forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = it.title)) },
                            onClick = {
                                state.value = state.value.also { v -> v.type = it.id }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        if (showDeleteAction) {
            IconButton(onClick = { onDelete.invoke() }) {
                Icon(
                    imageVector = Icons.Rounded.Remove, contentDescription = stringResource(
                        id = R.string.delete
                    ), tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = {
                showPicker = false
            },
            dismissButton = {
                DialogButton(text = stringResource(R.string.reset)) {
                    state.value.value = ""
                    showPicker = false
                }
            },
            confirmButton = {
                DialogButton(text = stringResource(R.string.okay)) {
                    showPicker = false
                }
            },
            content = {
                DatePicker(
                    state = datePickerState,
                    title = {}
                )
            }
        )
    }
}
