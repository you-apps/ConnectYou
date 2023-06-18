package com.bnyro.contacts.ui.screens

import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.ContactEditor
import com.bnyro.contacts.ui.components.base.FullScreenDialog

@Composable
fun EditorScreen(
    contact: ContactData? = null,
    onClose: () -> Unit,
    isCreatingNewDeviceContact: Boolean = false,
    onSave: (ContactData) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    var keyboardHeight by remember {
        mutableStateOf(0)
    }
    val topPadding by animateDpAsState(
        targetValue = with(LocalDensity.current) {
            keyboardHeight.toDp()
        }
    )

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val insets = ViewCompat.getRootWindowInsets(view)
            keyboardHeight = if (insets?.isVisible(WindowInsetsCompat.Type.ime()) != false) {
                insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
            } else {
                0
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }

    FullScreenDialog(onClose = onClose) {
        ContactEditor(
            modifier = Modifier
                .padding(bottom = topPadding),
            contact = contact?.copy(),
            isCreatingNewDeviceContact = isCreatingNewDeviceContact,
            onSave = {
                if (it.displayName.orEmpty().isBlank()) {
                    Toast.makeText(context, R.string.empty_name, Toast.LENGTH_SHORT).show()
                    return@ContactEditor
                }
                onSave.invoke(it)
                onClose.invoke()
            }
        )
    }
}
