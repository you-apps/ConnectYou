package com.bnyro.contacts.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

/**
 * A composable function that creates a labeled text field.
 *
 * @param modifier The modifier to apply to the text field.
 * @param label The resource id of the label to display above the text field.
 * @param state The mutable state of the text field's value.
 * @param keyboardType The keyboard type to use for the text field.
 * @param imeAction The ime action to use for the text field.
 * @param leadingIcon A composable function that returns the leading icon to display in the text field.
 * @param trailingIcon A composable function that returns the trailing icon to display in the text field.
 * @param interactionSource The interaction source for the text field.
 * @param suffix A composable function that returns the suffix to display in the text field.
 * @param shape The shape of the text field.
 * @param onValueChange The function to call when the value of the text field changes.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LabeledTextField(
    modifier: Modifier = Modifier,
    @StringRes label: Int,
    state: MutableState<String>,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    suffix: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(50),
    onValueChange: (String) -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = state.value,
        onValueChange = {
            state.value = it
            onValueChange(it)
        },
        label = {
            Text(text = stringResource(label))
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            onNext = { focusManager.moveFocus(FocusDirection.Next) }
        ),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        suffix = suffix,
        interactionSource = interactionSource,
        shape = shape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}
