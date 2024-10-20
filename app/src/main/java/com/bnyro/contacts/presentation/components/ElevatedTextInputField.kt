package com.bnyro.contacts.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction

@Composable
fun ElevatedTextInputField(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    enabled: Boolean = true,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    imeAction: ImeAction = ImeAction.Default,
    singleLine: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    TextField(
        modifier = modifier
            .focusRequester(focusRequester),
        value = query,
        onValueChange = onQueryChange,
        placeholder = { placeholder?.let { Text(it) } },
        leadingIcon = {
            leadingIcon?.let { Icon(imageVector = it, contentDescription = null) }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                ClickableIcon(icon = Icons.Default.Close) {
                    onQueryChange("")
                }
            }
        },
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        singleLine = singleLine,
        enabled = enabled
    )
}
