package com.bnyro.contacts.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.bnyro.contacts.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeleteSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val smsId = intent.getLongExtra(SmsReceiver.KEY_EXTRA_SMS_ID, -1)
        val notificationId = intent.getIntExtra(SmsReceiver.KEY_EXTRA_NOTIFICATION_ID, -1)

        NotificationManagerCompat.from(context).cancel(notificationId)

        CoroutineScope(Dispatchers.IO).launch {
            (context.applicationContext as App).smsRepo.deleteSms(context, smsId)
        }
    }
}
