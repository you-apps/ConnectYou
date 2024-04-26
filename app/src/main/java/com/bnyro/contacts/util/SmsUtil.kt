package com.bnyro.contacts.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bnyro.contacts.App
import com.bnyro.contacts.R
import com.bnyro.contacts.data.database.obj.SmsData
import java.lang.Character.UnicodeBlock
import java.util.Calendar

object SmsUtil {
    const val MAX_CHAR_LIMIT = 160
    private const val MAX_CHAR_LIMIT_WITH_UNICODE = 70

    private fun getSmsManager(subscriptionId: Int?): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && subscriptionId != null) {
            @Suppress("DEPRECATION")
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
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
            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_LONG).show()
            return
        }

        getSmsManager(subscriptionId)
            .sendTextMessage(address, null, body, null, null)

        val smsRepo = (context.applicationContext as App).smsRepo
        val timestamp = Calendar.getInstance().timeInMillis
        val threadId =
            smsRepo.getOrCreateThreadId(context, address)

        val smsData = SmsData(
            0,
            address,
            body,
            timestamp,
            threadId,
            Telephony.Sms.MESSAGE_TYPE_SENT
        )
        smsRepo.persistSms(context, smsData)
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
