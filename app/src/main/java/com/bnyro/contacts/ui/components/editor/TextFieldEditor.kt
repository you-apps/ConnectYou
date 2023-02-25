package com.bnyro.contacts.ui.components.editor

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.bnyro.contacts.obj.TranslatedType
import com.bnyro.contacts.obj.ValueWithType
import com.bnyro.contacts.ui.components.base.LabeledTextField

@SuppressLint("UnrememberedMutableState")
@Composable
fun TextFieldEditor(
    @StringRes label: Int,
    state: MutableState<ValueWithType>,
    types: List<TranslatedType>,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    showDeleteAction: Boolean,
    onDelete: () -> Unit,
    moveToTop: () -> Unit = {},
    onCreateNew: () -> Unit
) {
    val textState = mutableStateOf(state.value.value)

    EditorEntry(
        state = state,
        types = types,
        onDelete = onDelete,
        onCreateNew = onCreateNew,
        showDeleteAction = showDeleteAction,
        moveToTop = moveToTop
    ) {
        LabeledTextField(
            modifier = Modifier.weight(1f),
            label = label,
            state = textState,
            keyboardType = keyboardType,
            imeAction = imeAction
        ) {
            state.value.value = it
        }
    }
}
