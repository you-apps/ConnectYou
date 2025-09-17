package com.bnyro.contacts.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.PendingIntentCompat
import com.bnyro.contacts.App
import com.bnyro.contacts.R
import com.bnyro.contacts.data.database.obj.SmsData
import com.bnyro.contacts.data.database.obj.SmsStatus
import com.bnyro.contacts.util.extension.toast
import com.bnyro.contacts.util.receivers.SmsDeliveredReceiver
import com.bnyro.contacts.util.receivers.SmsReceiver
import com.bnyro.contacts.util.receivers.SmsSentReceiver
import java.lang.Character.UnicodeBlock
import java.util.Calendar

object SmsUtil {
    const val MAX_CHAR_LIMIT = 160
    private const val MAX_CHAR_LIMIT_WITH_UNICODE = 70

    private fun getSmsManager(subscriptionId: Int?): SmsManager {
        return if (subscriptionId != null) {
            @Suppress("DEPRECATION")
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }

    @SuppressLint("MissingPermission")
    fun getSubscriptions(context: Context): List<SubscriptionInfo> {
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager?
        return subscriptionManager!!.activeSubscriptionInfoList
    }

    suspend fun sendSms(
        context: Context,
        address: String,
        body: String,
        subscriptionId: Int? = null
    ) {
        Log.d("Send SMS", body)
        if (!ConnectionHelper.hasSignalForSms(context)) {
            context.toast(R.string.connection_error, Toast.LENGTH_LONG)
            return
        }

        val smsRepo = (context.applicationContext as App).smsRepo
        val timestamp = Calendar.getInstance().timeInMillis
        val threadId = smsRepo.getOrCreateThreadId(context, address)

        val smsData = SmsData(
            0,
            address,
            body,
            timestamp,
            threadId,
            Telephony.Sms.MESSAGE_TYPE_SENT,
            status = SmsStatus.NONE
        )
        smsData.id = smsRepo.persistSms(context, smsData)

        sendSmsInternal(context, smsData, subscriptionId)
    }

    suspend fun resendSms(context: Context, smsData: SmsData, subscriptionId: Int?) {
        Log.d("Resend SMS", smsData.body)
        if (!ConnectionHelper.hasSignalForSms(context)) {
            context.toast(R.string.connection_error, Toast.LENGTH_LONG)
            return
        }

        // update the sent timestamp of the sms
        val smsRepo = (context.applicationContext as App).smsRepo
        smsData.timestamp = Calendar.getInstance().timeInMillis
        smsRepo.updateSms(context, smsData)

        // retry sending the SMS
        sendSmsInternal(context, smsData, subscriptionId)
    }

    private fun sendSmsInternal(
        context: Context,
        smsData: SmsData,
        subscriptionId: Int? = null
    ) {
        val sentIntent = PendingIntentCompat.getBroadcast(
            context,
            ((smsData.id + 2) % Int.MAX_VALUE).toInt(),
            Intent(context, SmsSentReceiver::class.java)
                .putExtra(SmsReceiver.KEY_EXTRA_SMS_ID, smsData.id),
            PendingIntent.FLAG_UPDATE_CURRENT,
            true
        )
        val deliveredIntent = PendingIntentCompat.getBroadcast(
            context,
            ((smsData.id + 3) % Int.MAX_VALUE).toInt(),
            Intent(context, SmsDeliveredReceiver::class.java)
                .putExtra(SmsReceiver.KEY_EXTRA_SMS_ID, smsData.id),
            PendingIntent.FLAG_UPDATE_CURRENT,
            true
        )
        getSmsManager(subscriptionId)
            .sendTextMessage(smsData.address, null, smsData.body, sentIntent, deliveredIntent)
    }

    fun isShortEnoughForSms(text: String): Boolean {
        if (text.length > MAX_CHAR_LIMIT) return false

        // text messages containing one or more unicode chars are limited to 70 characters
        if (text.any { c -> UnicodeBlock.of(c) != UnicodeBlock.BASIC_LATIN }) {
            return text.length < MAX_CHAR_LIMIT_WITH_UNICODE
        }

        return true
    }

    fun splitSmsText(text: String): List<String> {
        var currentIndex = 0
        val splits = mutableListOf<String>()

        while (currentIndex < text.length) {
            var fullPart =
                text.substring(currentIndex, minOf(currentIndex + MAX_CHAR_LIMIT, text.length))

            if (isShortEnoughForSms(fullPart)) {
                currentIndex += MAX_CHAR_LIMIT
            } else {
                fullPart = text.substring(
                    currentIndex,
                    minOf(currentIndex + MAX_CHAR_LIMIT_WITH_UNICODE, text.length)
                )
                currentIndex += MAX_CHAR_LIMIT_WITH_UNICODE
            }

            splits.add(fullPart)
        }

        return splits
    }
}
