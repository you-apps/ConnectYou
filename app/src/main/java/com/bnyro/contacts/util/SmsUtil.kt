package com.bnyro.contacts.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bnyro.contacts.R
import com.bnyro.contacts.db.obj.SmsData
import com.bnyro.contacts.repo.DeviceSmsRepo
import com.bnyro.contacts.repo.LocalSmsRepo
import com.bnyro.contacts.repo.SmsRepository
import java.lang.Character.UnicodeBlock
import java.util.Calendar

object SmsUtil {
    const val MAX_CHAR_LIMIT = 160
    private const val MAX_CHAR_LIMIT_WITH_UNICODE = 70

    lateinit var smsRepo: SmsRepository

    fun initSmsRepo() {
        smsRepo = if (Preferences.getBoolean(Preferences.storeSmsLocallyKey, false)) {
            LocalSmsRepo()
        } else {
            DeviceSmsRepo()
        }
    }

    suspend fun getSmsList(context: Context) = smsRepo.getSmsList(context)

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
    ): SmsData? {
        if (!ConnectionHelper.hasSignalForSms(context)) {
            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_LONG).show()
            return null
        }

        getSmsManager(subscriptionId)
            .sendTextMessage(address, null, body, null, null)

        val timestamp = Calendar.getInstance().timeInMillis
        val threadId = getOrCreateThreadId(context, address)

        val smsData = SmsData(
            -1,
            address,
            body,
            timestamp,
            threadId,
            Telephony.Sms.MESSAGE_TYPE_SENT
        )
        persistMessage(context, smsData)

        return smsData
    }

    suspend fun deleteMessage(context: Context, id: Long) = smsRepo.deleteSms(context, id)

    suspend fun deleteThread(context: Context, threadId: Long) = smsRepo.deleteThread(
        context,
        threadId
    )

    suspend fun persistMessage(context: Context, smsData: SmsData) = smsRepo.persistSms(
        context,
        smsData
    )

    suspend fun getOrCreateThreadId(context: Context, address: String) =
        smsRepo.getOrCreateThreadId(
            context,
            address
        )

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
