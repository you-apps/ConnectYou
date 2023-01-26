package com.bnyro.contacts.ui.components.base

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledTextField(
    modifier: Modifier = Modifier,
    @StringRes label: Int,
    state: MutableState<String>,
    onValueChange: (String) -> Unit = {}
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp),
        value = state.value,
        onValueChange = {
            state.value = it
            onValueChange(it)
        },
        label = {
            Text(text = stringResource(label))
        }
    )
}
