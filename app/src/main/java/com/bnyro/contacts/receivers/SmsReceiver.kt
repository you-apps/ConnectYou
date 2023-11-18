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
import com.bnyro.contacts.db.obj.SmsData
import com.bnyro.contacts.enums.IntentActionType
import com.bnyro.contacts.ui.activities.MainActivity
import com.bnyro.contacts.util.IntentHelper
import com.bnyro.contacts.util.NotificationHelper
import com.bnyro.contacts.util.NotificationHelper.MESSAGES_CHANNEL_ID
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.SmsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SMS_DELIVER) return

        Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach { message ->
            val notificationId = message.timestampMillis.hashCode()
            val address = message.displayOriginatingAddress
            val body = message.displayMessageBody
            val timestamp = message.timestampMillis
            val threadId =
                runBlocking(Dispatchers.IO) { SmsUtil.getOrCreateThreadId(context, address) }
            val bareSmsData =
                SmsData(-1, address, body, timestamp, threadId, Telephony.Sms.MESSAGE_TYPE_INBOX)

            createNotification(context, notificationId, bareSmsData)
            val smsData = runBlocking(Dispatchers.IO) {
                SmsUtil.persistMessage(context, bareSmsData)
            }

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

        val replyIntent = Intent(context, ReplyReceiver::class.java)
            .putExtra(KEY_EXTRA_ADDRESS, smsData.address)
            .putExtra(KEY_EXTRA_NOTIFICATION_ID, notificationId)

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            replyIntent,
            PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_input_add,
            context.getString(R.string.reply),
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()

        val deleteIntent = Intent(context, DeleteSmsReceiver::class.java)
            .putExtra(KEY_EXTRA_NOTIFICATION_ID, notificationId)
            .putExtra(KEY_EXTRA_SMS_ID, smsData.id)
            .putExtra(KEY_EXTRA_THREAD_ID, smsData.threadId)

        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            deleteIntent,
            PendingIntent.FLAG_MUTABLE
        )

        val deleteMessageAction = NotificationCompat.Action.Builder(
            R.drawable.ic_delete,
            context.getString(R.string.delete),
            deletePendingIntent
        ).build()

        val smsThreadIntent = IntentHelper.getLaunchIntent(IntentActionType.SMS, smsData.address)

        val smsThreadPendingIntent = PendingIntent.getActivity(
            context,
            1,
            smsThreadIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_message_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentTitle(smsData.address)
            .setContentText(smsData.body)
            .setContentIntent(smsThreadPendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(smsData.body)
            )
            .setWhen(smsData.timestamp)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .addAction(replyAction)
            .addAction(deleteMessageAction)
            .build()

        if (!PermissionHelper.checkPermissions(
                context,
                NotificationHelper.notificationPermissions
            )
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        private const val SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER"
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val KEY_EXTRA_ADDRESS = "key_extra_address"
        const val KEY_EXTRA_SMS_ID = "key_extra_sms_id"
        const val KEY_EXTRA_THREAD_ID = "key_extra_thread_id"
        const val KEY_EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
