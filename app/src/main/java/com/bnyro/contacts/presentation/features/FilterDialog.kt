package com.bnyro.contacts.presentation.features

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.SortOrder
import com.bnyro.contacts.domain.model.AccountType
import com.bnyro.contacts.domain.model.ContactsGroup
import com.bnyro.contacts.domain.model.FilterOptions
import com.bnyro.contacts.presentation.components.ChipSelector

@Composable
fun FilterDialog(
    initialFilters: FilterOptions,
    availableAccountTypes: List<AccountType>,
    availableGroups: List<ContactsGroup>,
    onDismissRequest: () -> Unit,
    onFilterChanged: (FilterOptions) -> Unit
) {
    var sortOrder by remember {
        mutableStateOf(initialFilters.sortOder)
    }

    var hiddenAccountNames by remember {
        mutableStateOf(initialFilters.hiddenAccountIdentifiers)
    }

    var visibleGroups by remember {
        mutableStateOf(initialFilters.visibleGroups)
    }
    
    var favoritesOnly by remember {
        mutableStateOf(initialFilters.favoritesOnly)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(text = stringResource(R.string.okay)) {
                val options = FilterOptions(sortOrder, hiddenAccountNames, visibleGroups, favoritesOnly)
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
                val sortOrders = listOf(
                    R.string.first_name,
                    R.string.last_name,
                    R.string.nick_name
                ).map {
                    stringResource(it)
                }
                ChipSelector(
                    title = stringResource(R.string.sort_order),
                    entries = sortOrders,
                    selections = listOf(sortOrders[sortOrder.ordinal]),
                    onSelectionChanged = { index, _ ->
                        sortOrder = SortOrder.fromInt(index)
                    }
                )
                if (availableAccountTypes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ChipSelector(
                        title = stringResource(R.string.account_type),
                        entries = availableAccountTypes.map { it.type },
                        selections = availableAccountTypes.filter {
                            !hiddenAccountNames.contains(it.identifier)
                        }.map { it.type },
                        onSelectionChanged = { index, newValue ->
                            val selection = availableAccountTypes[index]
                            hiddenAccountNames = if (newValue) {
                                hiddenAccountNames - selection.identifier
                            } else {
                                hiddenAccountNames + selection.identifier
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
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.favorites_only),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Checkbox(checked = favoritesOnly, onCheckedChange = { favoritesOnly = it })
                }
            }
        }
    )
}
