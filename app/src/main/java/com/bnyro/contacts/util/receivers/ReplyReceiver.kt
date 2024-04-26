package com.bnyro.contacts.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.bnyro.contacts.util.SmsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val address = intent.getStringExtra(SmsReceiver.KEY_EXTRA_ADDRESS) ?: return
        val notificationId = intent.getIntExtra(SmsReceiver.KEY_EXTRA_NOTIFICATION_ID, -1)

        NotificationManagerCompat.from(context).cancel(notificationId)

        val remoteInput = RemoteInput.getResultsFromIntent(intent) ?: return

        val message = remoteInput.getCharSequence(SmsReceiver.KEY_TEXT_REPLY).toString()
        if (message.isBlank()) return

        CoroutineScope(Dispatchers.IO).launch {
            SmsUtil.sendSms(context, address, message)
        }
    }
}
