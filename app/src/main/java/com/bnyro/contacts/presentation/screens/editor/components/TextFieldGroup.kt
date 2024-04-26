package com.bnyro.contacts.presentation.screens.editor.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.domain.model.TranslatedType
import com.bnyro.contacts.domain.model.ValueWithType

/**
 * A composable function that creates a group of text fields.
 *
 * @param items The list of already filled fields as [ValueWithType] (if any).
 * @param label The label for the text fields.
 * @param addActionText The text for the add action button.
 * @param types The list of supported dropdown items as [TranslatedType].
 * @param leadingIcon The leading icon for the text fields.
 * @param keyboardType The keyboard type for the text fields.
 */
@Composable
fun TextFieldGroup(
    items: SnapshotStateList<MutableState<ValueWithType>>,
    @StringRes label: Int,
    @StringRes addActionText: Int,
    types: List<TranslatedType> = listOf(),
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column() {
        with(items) {
            forEachIndexed { index, it ->
                TextFieldEditor(
                    label = label,
                    state = it,
                    types = types,
                    keyboardType = keyboardType,
                    imeAction = if (it == last()) ImeAction.Done else ImeAction.Next,
                    onDelete = {
                        removeAt(index)
                    },
                    showDeleteAction = size > 1,
                    leadingIcon = {
                        if (index == 0) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null
                            )
                        }
                    },
                    shape = if (index == 0) RoundedCornerShape(50, 50, 0, 0) else RectangleShape
                )
            }
            FilledTonalButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = { add(mutableStateOf(ValueWithType("", 0))) },
                    shape = RoundedCornerShape(0, 0, 50, 50)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = addActionText))
                }
        }
    }
}
