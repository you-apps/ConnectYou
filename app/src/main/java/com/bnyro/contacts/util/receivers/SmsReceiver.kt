package com.bnyro.contacts.util.receivers

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
import com.bnyro.contacts.App
import com.bnyro.contacts.R
import com.bnyro.contacts.data.database.obj.SmsData
import com.bnyro.contacts.domain.enums.IntentActionType
import com.bnyro.contacts.util.IntentHelper
import com.bnyro.contacts.util.NotificationHelper
import com.bnyro.contacts.util.NotificationHelper.MESSAGES_CHANNEL_ID
import com.bnyro.contacts.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SMS_DELIVER) return

        Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach { message ->
            val address = message.displayOriginatingAddress
            val body = message.displayMessageBody
            val timestamp = message.timestampMillis

            val threadId =
                runBlocking(Dispatchers.IO) {
                    (context.applicationContext as App).smsRepo.getOrCreateThreadId(
                        context,
                        address
                    )
                }
            val bareSmsData =
                SmsData(0, address, body, timestamp, threadId, Telephony.Sms.MESSAGE_TYPE_INBOX)

            createNotification(context, bareSmsData.hashCode(), bareSmsData)
            runBlocking(Dispatchers.IO) {
                (context.applicationContext as App).smsRepo.persistSms(context, bareSmsData)
            }
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
            notificationId + 1,
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
            notificationId + 2,
            deleteIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val deleteMessageAction = NotificationCompat.Action.Builder(
            R.drawable.ic_delete,
            context.getString(R.string.delete),
            deletePendingIntent
        ).build()

        val smsThreadIntent = IntentHelper.getLaunchIntent(IntentActionType.SMS, smsData.address)

        val smsThreadPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 3,
            smsThreadIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
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

        verificationCodeRegex.find(smsData.body)?.let { code ->
            val copyIntent = Intent(context, CopyTextReceiver::class.java)
                .putExtra(KEY_EXTRA_TEXT, code.value)

            val copyPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 4,
                copyIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val copyMessageAction = NotificationCompat.Action.Builder(
                R.drawable.ic_delete,
                "${context.getString(R.string.copy)} ${code.value}",
                copyPendingIntent
            ).build()

            builder.addAction(copyMessageAction)
        }

        NotificationHelper.notificationPermissions.firstOrNull()?.let {
            if (!PermissionHelper.hasPermission(context, it)) return
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    companion object {
        private const val SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER"
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val KEY_EXTRA_ADDRESS = "key_extra_address"
        const val KEY_EXTRA_SMS_ID = "key_extra_sms_id"
        const val KEY_EXTRA_THREAD_ID = "key_extra_thread_id"
        const val KEY_EXTRA_NOTIFICATION_ID = "notification_id"
        const val KEY_EXTRA_TEXT = "key_extra_text"

        val verificationCodeRegex = Regex("(?<!\\d)\\d{6}(?!\\d)")
    }
}
