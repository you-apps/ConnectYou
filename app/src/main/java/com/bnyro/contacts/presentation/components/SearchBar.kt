package com.bnyro.contacts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.R

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    state: MutableState<TextFieldValue>,
    customActions: @Composable () -> Unit = {}
) {
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        BasicTextField(
            value = state.value,
            onValueChange = {
                state.value = it
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp), CircleShape)
                .fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            cursorBrush = Brush.linearGradient(0f to textColor, 1f to textColor),
            textStyle = TextStyle.Default.copy(color = textColor),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.value.text.isEmpty()) Text(stringResource(R.string.search))
                        innerTextField()
                    }
                    if (state.value.text.isNotEmpty()) {
                        ClickableIcon(
                            icon = Icons.Default.Close,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            state.value = TextFieldValue("")
                        }
                    } else {
                        customActions.invoke()
                    }
                }
            }
        )
    }
}
