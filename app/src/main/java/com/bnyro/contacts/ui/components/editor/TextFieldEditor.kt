package com.bnyro.contacts.ui.components.editor

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bnyro.contacts.obj.TranslatedType
import com.bnyro.contacts.obj.ValueWithType
import com.bnyro.contacts.ui.components.EditorEntry
import com.bnyro.contacts.ui.components.base.LabeledTextField

@Composable
fun TextFieldEditor(
    @StringRes label: Int,
    state: MutableState<ValueWithType>,
    types: List<TranslatedType>,
    onCreateNew: () -> Unit
) {
    val textState = remember {
        mutableStateOf(state.value.value)
    }

    EditorEntry(state = state, types = types, onCreateNew = onCreateNew) {
        LabeledTextField(
            modifier = Modifier.weight(1f),
            label = label,
            state = textState
        ) {
            state.value.value = it
        }
    }
}
