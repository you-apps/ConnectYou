package com.bnyro.contacts.util

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.bnyro.contacts.R

object NotificationHelper {
    const val MESSAGES_CHANNEL_ID = "messages_channel"
    const val CALL_CHANNEL_ID = "call_channel"
    val notificationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        arrayOf()
    }

    fun createChannels(context: Context) {
        val messagesChannel = NotificationChannelCompat.Builder(
            MESSAGES_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(context.getString(R.string.messages))
            .setLightsEnabled(true)
            .setVibrationEnabled(true)
            .setLightColor(Color.WHITE)
            .build()
        val callChannel = NotificationChannelCompat.Builder(
            CALL_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(context.getString(R.string.calls))
            .setLightsEnabled(true)
            .setVibrationEnabled(true)
            .setLightColor(Color.WHITE)
            .build()

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.createNotificationChannelsCompat(
            listOf(
                messagesChannel,
                callChannel
            )
        )
    }
}
