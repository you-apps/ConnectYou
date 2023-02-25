package com.bnyro.contacts.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.SortOrder
import com.bnyro.contacts.obj.ContactsGroup
import com.bnyro.contacts.obj.FilterOptions
import com.bnyro.contacts.ui.components.base.ChipSelector

@Composable
fun FilterDialog(
    initialFilters: FilterOptions,
    availableAccountTypes: List<String>,
    availableGroups: List<ContactsGroup>,
    onDismissRequest: () -> Unit,
    onFilterChanged: (FilterOptions) -> Unit
) {
    var sortOrder by remember {
        mutableStateOf(initialFilters.sortOder)
    }

    var hiddenAccountNames by remember {
        mutableStateOf(initialFilters.hiddenAccountNames)
    }

    var visibleGroups by remember {
        mutableStateOf(initialFilters.visibleGroups)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(text = stringResource(R.string.okay)) {
                val options = FilterOptions(sortOrder, hiddenAccountNames, visibleGroups)
                onFilterChanged.invoke(options)
                onDismissRequest.invoke()
            }
        },
        dismissButton = {
            DialogButton(text = stringResource(R.string.cancel)) {
                onDismissRequest.invoke()
            }
        },
        text = {
            Column {
                val sortOrders = listOf(R.string.first_name, R.string.last_name).map {
                    stringResource(it)
                }
                ChipSelector(
                    title = stringResource(R.string.sort_order),
                    entries = sortOrders,
                    selections = listOf(sortOrders[sortOrder.value]),
                    onSelectionChanged = { index, _ ->
                        sortOrder = SortOrder.fromInt(index)
                    }
                )
                if (availableAccountTypes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ChipSelector(
                        title = stringResource(R.string.account_type),
                        entries = availableAccountTypes,
                        selections = availableAccountTypes.filter {
                            !hiddenAccountNames.contains(it)
                        },
                        onSelectionChanged = { index, newValue ->
                            hiddenAccountNames = if (newValue) {
                                hiddenAccountNames - availableAccountTypes[index]
                            } else {
                                hiddenAccountNames + availableAccountTypes[index]
                            }
                        }
                    )
                }
                if (availableGroups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ChipSelector(
                        title = stringResource(R.string.groups),
                        entries = availableGroups.map { it.title },
                        selections = availableGroups.filter {
                            visibleGroups.contains(it)
                        }.map { it.title },
                        onSelectionChanged = { index, newValue ->
                            visibleGroups = if (newValue) {
                                visibleGroups + availableGroups[index]
                            } else {
                                visibleGroups - availableGroups[index]
                            }
                        }
                    )
                }
            }
        }
    )
}
