package com.bnyro.contacts.presentation.screens.editor.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.TranslatedType
import com.bnyro.contacts.domain.model.ValueWithType
import com.bnyro.contacts.presentation.components.LabeledTextField

/**
 * A composable function that renders a text field with a dropdown menu to select the type and a button to delete the text field.
 *
 * @param label The label for the text field.
 * @param state The state of the text field.
 * @param types The list of types that can be selected.
 * @param keyboardType The keyboard type for the text field.
 * @param imeAction The ime action for the text field.
 * @param showDeleteAction Whether or not to show the delete action.
 * @param leadingIcon The leading icon for the text field.
 * @param onDelete The function to call when the delete action is clicked.
 * @param shape The shape of the text field.
 */
@Composable
fun TextFieldEditor(
    @StringRes label: Int,
    state: MutableState<ValueWithType>,
    types: List<TranslatedType>,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    showDeleteAction: Boolean,
    leadingIcon: @Composable (() -> Unit)? = null,
    onDelete: () -> Unit,
    shape: Shape,
) {
    val textState = remember(state.value) { mutableStateOf(state.value.value) }
    LabeledTextField(
        label = label,
        state = textState,
        keyboardType = keyboardType,
        imeAction = imeAction,
        leadingIcon = leadingIcon,
        trailingIcon = if (showDeleteAction) ({
            IconButton(onClick = { onDelete.invoke() }) {
                Icon(
                    imageVector = Icons.Rounded.Remove, contentDescription = stringResource(
                        id = R.string.delete
                    ), tint = MaterialTheme.colorScheme.error
                )
            }
        }) else {
            null
        },
        suffix = if (types.isNotEmpty()) ({
            var expanded by remember { mutableStateOf(false) }
            Row(
                Modifier
                    .clickable {
                        expanded = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = types.firstOrNull {
                        it.id == state.value.type
                    }?.title?.let { stringResource(it) }.orEmpty()
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }

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
        }) else {
            null
        }, shape = shape
    ) {
        state.value.value = it
    }


}
