package com.bnyro.contacts.presentation.screens.sms.model

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.telephony.SubscriptionInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bnyro.contacts.App
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.util.SmsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SmsModel(application: Application) : AndroidViewModel(application) {
    val app = application as App
    var smsList = app.smsRepo.getSmsStream(context = app.applicationContext).stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = listOf()
    )

    var currentContactData: ContactData? = null

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
}
