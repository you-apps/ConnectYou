package com.bnyro.contacts.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.SortOrder
import com.bnyro.contacts.obj.FilterOptions
import com.bnyro.contacts.ui.components.base.ChipSelector

@Composable
fun FilterDialog(
    initialFilters: FilterOptions,
    onDismissRequest: () -> Unit,
    onFilterChanged: (FilterOptions) -> Unit
) {
    var sortOrder by remember {
        mutableStateOf(initialFilters.sortOder)
    }

    var hiddenAccountNames by remember {
        mutableStateOf(initialFilters.hiddenAccountNames)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(text = stringResource(R.string.okay)) {
                val options = FilterOptions(sortOrder, hiddenAccountNames)
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
                ChipSelector(
                    title = stringResource(R.string.sort_order),
                    entries = listOf(R.string.first_name, R.string.last_name).map { stringResource(it) },
                    selections = listOf(sortOrder.value),
                    onSelectionChanged = { index, _ ->
                        sortOrder = SortOrder.fromInt(index)
                    }
                )
            }
        }
    )
}
