package com.bnyro.contacts.ui.models

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bnyro.contacts.App
import com.bnyro.contacts.util.SmsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SmsModel(val app: App) : ViewModel() {
    var smsList = app.smsRepo.getSmsStream(context = app.applicationContext).stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = listOf()
    )

    var initialAddressAndBody by mutableStateOf<Pair<String, String?>?>(null)

    var currentSubscription: SubscriptionInfo? = null

    private fun updateSmsList() {
        smsList = app.smsRepo.getSmsStream(context = app.applicationContext).stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = listOf()
        )
    }

    fun deleteSms(context: Context, id: Long, threadId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            app.smsRepo.deleteSms(context, id)
        }
    }

    fun deleteThread(context: Context, threadId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            app.smsRepo.deleteThread(context, threadId)
        }
    }

    @SuppressLint("NewApi")
    fun sendSms(context: Context, address: String, body: String) {
        viewModelScope.launch(Dispatchers.IO) {
            SmsUtil.sendSms(context, address, body, currentSubscription?.subscriptionId)
        }
    }

    fun refreshLocalSmsPreference() {
        app.initSmsRepo()
        updateSmsList()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as App
                SmsModel(application)
            }
        }
    }
}
