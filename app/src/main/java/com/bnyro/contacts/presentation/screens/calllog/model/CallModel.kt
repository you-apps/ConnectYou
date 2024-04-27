package com.bnyro.contacts.presentation.screens.calllog.model

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bnyro.contacts.App
import com.bnyro.contacts.domain.model.CallLogEntry
import com.bnyro.contacts.navigation.HomeRoutes
import kotlinx.coroutines.launch

class CallModel(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {
    val callLogRepository = (application as App).callLogRepository

    val initialPhoneNumber = savedStateHandle.get<String>(HomeRoutes.Phone.phoneNumber)

    var callLogs by mutableStateOf<List<CallLogEntry>>(emptyList(), policy = neverEqualPolicy())
        private set

    init {
        viewModelScope.launch {
            callLogs = callLogRepository.getCallLog()
        }
    }

    fun requestDefaultDialerApp(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
        val isAlreadyDefaultDialer =
            context.packageName.equals(telecomManager!!.defaultDialerPackage)
        if (!isAlreadyDefaultDialer) {
            val intent: Intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    context.packageName
                )
            context.startActivity(intent)
        }
    }

    companion object {
        val phonePerms = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
        )
    }
}