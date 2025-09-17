package com.bnyro.contacts.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bnyro.contacts.App
import com.bnyro.contacts.data.database.obj.SmsStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class SmsDeliveredReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val smsId = intent.getLongExtra(SmsReceiver.KEY_EXTRA_SMS_ID, -1)
            .takeIf { it != -1L } ?: return

        Log.e("sms delivered", "sms delivered")
        val smsRepo = (context.applicationContext as App).smsRepo
        runBlocking(Dispatchers.IO) {
            val smsData = smsRepo.getSms(context, smsId) ?: return@runBlocking
            smsData.status = SmsStatus.DELIVERED

            smsRepo.updateSms(context, smsData)
        }
    }
}