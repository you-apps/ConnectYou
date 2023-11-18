package com.bnyro.contacts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.bnyro.contacts.ui.activities.MainActivity
import com.bnyro.contacts.util.SmsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeleteSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val smsId = intent.getLongExtra(SmsReceiver.KEY_EXTRA_SMS_ID, -1)
        val threadId = intent.getLongExtra(SmsReceiver.KEY_EXTRA_THREAD_ID, -1)
        val notificationId = intent.getIntExtra(SmsReceiver.KEY_EXTRA_NOTIFICATION_ID, -1)

        NotificationManagerCompat.from(context).cancel(notificationId)

        CoroutineScope(Dispatchers.IO).launch {
            MainActivity.smsModel?.deleteSms(context, smsId, threadId) ?: run {
                // if the UI is not currently opened, only delete the SMS and don't touch the UI
                SmsUtil.deleteMessage(context, smsId)
            }
        }
    }
}
