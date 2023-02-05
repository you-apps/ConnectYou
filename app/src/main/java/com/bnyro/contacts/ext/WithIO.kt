package com.bnyro.contacts.ext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun ViewModel.withIO(block: suspend () -> Unit) = viewModelScope.launch {
    withContext(Dispatchers.IO) {
        block.invoke()
    }
}
