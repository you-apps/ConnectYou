package com.bnyro.contacts.ui.components.base

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

private val SearchBarIconOffsetX: Dp = 4.dp
private val TonalElevation = 10.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElevatedTextInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = SearchBarDefaults.inputFieldColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val focusRequester = remember { FocusRequester() }

    val elevationColor = MaterialTheme.colorScheme.surfaceColorAtElevation(TonalElevation)
    Surface(
        shape = RoundedCornerShape(36.dp),
        color = elevationColor,
        contentColor = contentColorFor(elevationColor),
        tonalElevation = TonalElevation,
        modifier = modifier
            .zIndex(1f)
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = modifier
                .height(SearchBarDefaults.InputFieldHeight)
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .semantics {
                    onClick {
                        focusRequester.requestFocus()
                        true
                    }
                },
            enabled = enabled,
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            interactionSource = interactionSource,
            decorationBox = @Composable { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = query,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon?.let { leading ->
                        {
                            Box(Modifier.offset(x = SearchBarIconOffsetX)) { leading() }
                        }
                    },
                    trailingIcon = trailingIcon?.let { trailing ->
                        {
                            Box(Modifier.offset(x = -SearchBarIconOffsetX)) { trailing() }
                        }
                    },
                    shape = SearchBarDefaults.inputFieldShape,
                    colors = colors,
                    contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(),
                    container = {}
                )
            }
        )
    }
}
