package com.bnyro.contacts.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup
import android.telecom.Call
import android.telecom.InCallService
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bnyro.contacts.R
import com.bnyro.contacts.ext.stringValue
import com.bnyro.contacts.receivers.CallActionReceiver
import com.bnyro.contacts.receivers.CallActionReceiver.Companion.ACCEPT_CALL
import com.bnyro.contacts.receivers.CallActionReceiver.Companion.DECLINE_CALL
import com.bnyro.contacts.ui.activities.CallActivity
import com.bnyro.contacts.util.CallManager
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CallService : InCallService() {

    private lateinit var acceptPendingIntent: PendingIntent
    private lateinit var declinePendingIntent: PendingIntent
    val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        acceptPendingIntent = createPendingIntent(ACCEPT_CALL, 0)
        declinePendingIntent = createPendingIntent(DECLINE_CALL, 1)
    }

    private fun createPendingIntent(action: String, requestCode: Int): PendingIntent {
        return PendingIntent.getBroadcast(
            this,
            requestCode,
            Intent(this, CallActionReceiver::class.java).apply { this.action = action },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            CallManager.updateCallState(state)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callCallback)
        CallManager.setCall(call)

        scope.launch {
            val callerNumber = CallManager.callerDisplayNumber
            val (thumbnailUri, contactName) = withContext(Dispatchers.IO) {
                getContactName(callerNumber)
            }
            val contactPhoto = if (thumbnailUri != null) {
                withContext(Dispatchers.IO) {
                    ContactsHelper.getContactPhotoThumbnail(this@CallService, thumbnailUri)
                }
            } else {
                null
            }

            val notification = buildNotification(call, callerNumber, contactName, contactPhoto)
            NotificationManagerCompat.from(this@CallService)
                .notify(CALL_NOTIFICATION_ID, notification)

        }
        val intent = Intent(applicationContext, CallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun getContactName(phoneNumber: String): Pair<String?, String?> {
        val cr: ContentResolver = this.contentResolver
        val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val query =
            cr.query(
                uri,
                arrayOf(PhoneLookup.DISPLAY_NAME, PhoneLookup.PHOTO_THUMBNAIL_URI),
                null,
                null,
                null
            )

        query?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.stringValue(PhoneLookup.PHOTO_THUMBNAIL_URI) to cursor.stringValue(
                    PhoneLookup.DISPLAY_NAME
                )
            }
        }
        return null to null
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        NotificationManagerCompat.from(this).cancel(CALL_NOTIFICATION_ID)
        call.unregisterCallback(callCallback)
        CallManager.setCall(null)
    }

    private fun buildNotification(
        call: Call,
        callerNumber: String,
        callerName: String?,
        callersPhoto: Bitmap?
    ): Notification {
        val collapsedView = RemoteViews(packageName, R.layout.call_notification).apply {
            setTextViewText(
                R.id.notification_caller_name,
                callerName ?: getString(R.string.unknown_number)
            )
            setTextViewText(R.id.notification_call_status, callerNumber)
            setViewVisibility(
                R.id.notification_accept_call,
                if (call.state == Call.STATE_RINGING) View.VISIBLE else View.GONE
            )

            if (callersPhoto != null) {
                setImageViewBitmap(R.id.notification_thumbnail, callersPhoto)
            } else {
                setImageViewResource(R.id.notification_thumbnail, R.drawable.round_person)
            }
            setOnClickPendingIntent(R.id.notification_decline_call, declinePendingIntent)
            setOnClickPendingIntent(R.id.notification_accept_call, acceptPendingIntent)
        }

        val intent = Intent(applicationContext, CallActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        return NotificationCompat.Builder(this, NotificationHelper.CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_call)
            .setCategory(Notification.CATEGORY_CALL)
            .setCustomContentView(collapsedView)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSound(null)
            .setUsesChronometer(true)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .build()
    }

    companion object {
        const val CALL_NOTIFICATION_ID = 10
    }
}