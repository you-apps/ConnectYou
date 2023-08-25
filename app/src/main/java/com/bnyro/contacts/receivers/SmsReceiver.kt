package com.bnyro.contacts.receivers

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.bnyro.contacts.R
import com.bnyro.contacts.obj.SmsData
import com.bnyro.contacts.ui.activities.MainActivity
import com.bnyro.contacts.util.NotificationHelper
import com.bnyro.contacts.util.NotificationHelper.MESSAGES_CHANNEL_ID
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.SmsUtil

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!listOf(SMS_RECEIVED, SMS_DELIVER).contains(intent.action)) return

        Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach { message ->
            val notificationId = message.timestampMillis.hashCode()
            val address = message.displayOriginatingAddress
            val body = message.displayMessageBody
            val timestamp = message.timestampMillis
            val threadId = SmsUtil.getOrCreateThreadId(context, address)
            val bareSmsData = SmsData(-1, address, body, timestamp, threadId, Telephony.Sms.MESSAGE_TYPE_INBOX)

            createNotification(context, notificationId, bareSmsData)
            val smsData = SmsUtil.persistMessage(context, bareSmsData)

            MainActivity.smsModel?.addSmsToList(smsData)
        }
    }

    @SuppressLint("MissingPermission")
    private fun createNotification(
        context: Context,
        notificationId: Int,
        smsData: SmsData
    ) {
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).build()

        val resultIntent = Intent(context, ReplyReceiver::class.java)
            .putExtra(KEY_EXTRA_ADDRESS, smsData.address)
            .putExtra(KEY_EXTRA_NOTIFICATION_ID, notificationId)

        val resultPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            resultIntent,
            PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_input_add,
            context.getString(R.string.reply),
            resultPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()

        val notification = NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_message_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentTitle(smsData.address)
            .setContentText(smsData.body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(smsData.body)
            )
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .addAction(replyAction)
            .build()

        if (!PermissionHelper.checkPermissions(
                context,
                NotificationHelper.notificationPermissions
            )
        ) return
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        private const val SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER"
        private const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val KEY_EXTRA_ADDRESS = "key_extra_address"
        const val KEY_EXTRA_NOTIFICATION_ID = "notification_id"
    }
}