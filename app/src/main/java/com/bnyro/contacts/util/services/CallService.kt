package com.bnyro.contacts.util.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Binder
import android.os.IBinder
import android.telecom.Call
import android.telecom.InCallService
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bnyro.contacts.App
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.CallerInfo
import com.bnyro.contacts.presentation.screens.dialer.model.state.CallState
import com.bnyro.contacts.ui.activities.CallActivity
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.NotificationHelper
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CallService : InCallService() {

    private val binder = LocalBinder()

    private val scope = CoroutineScope(Dispatchers.Main)

    private var currentCallState = Call.STATE_DISCONNECTED
    private var callerInfo = CallerInfo()

    var onUpdateState: (CallState) -> Unit = {}

    var onCallerInfoUpdate: (CallerInfo) -> Unit = {}

    private val callActionReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra(ACTION_EXTRA_KEY)) {
                ACCEPT_CALL ->
                    acceptCall()

                DECLINE_CALL -> cancelCall()
            }
        }
    }

    override fun onCreate() {
        ContextCompat.registerReceiver(
            this,
            callActionReciever,
            IntentFilter(CALL_INTENT_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        )
        super.onCreate()
    }

    override fun onDestroy() {
        unregisterReceiver(callActionReciever)
        super.onDestroy()
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            currentCallState = state
            updateState(state)
            if (state == Call.STATE_DISCONNECTED) {
                val closeCallAlertIntent = Intent(CallActivity.CALL_ALERT_CLOSE_ACTION).apply {
                    putExtra(CallActivity.ACTION_EXTRA_KEY, CallActivity.CLOSE_ACTION)
                    putExtra(CallActivity.ACTION_EXTRA_REASON,
                        call.details?.disconnectCause?.description?.takeIf { it.isNotBlank() }
                            ?.toString()
                    )
                    `package` = packageName
                }
                sendBroadcast(closeCallAlertIntent)
            }
        }
    }

    fun updateState(state: Int = currentCallState) {
        when (state) {
            Call.STATE_RINGING, Call.STATE_PULLING_CALL -> onUpdateState.invoke(CallState.Incoming)
            Call.STATE_DIALING -> onUpdateState.invoke(CallState.Outgoing)
            Call.STATE_ACTIVE -> onUpdateState.invoke(CallState.InCall)
            Call.STATE_DISCONNECTED -> onUpdateState.invoke(CallState.Disconnected)
            Call.STATE_CONNECTING -> onUpdateState.invoke(CallState.Connecting)
            Call.STATE_DISCONNECTING -> onUpdateState.invoke(CallState.Disconnecting)
            else -> {}
        }
    }

    fun updateCallerInfo() {
        onCallerInfoUpdate(callerInfo)
    }

    @SuppressLint("MissingPermission")
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        val intent = Intent(applicationContext, CallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)

        call.registerCallback(callCallback)

        val callerNumber = call.details.gatewayInfo?.originalAddress?.schemeSpecificPart
            ?: call.details.handle.schemeSpecificPart

        currentCallState = call.state
        updateState(call.state)

        val formattedPhoneNumber = formatPhoneNumber(callerNumber)
        callerInfo = CallerInfo(
            rawPhoneNumber = callerNumber,
            formattedPhoneNumber = formattedPhoneNumber
        )
        updateCallerInfo()

        scope.launch {
            val phoneLookupRepository =
                (this@CallService.applicationContext as App).phoneLookupRepository
            val contactData = phoneLookupRepository.getContactByNumber(callerNumber)
            val contactPhoto = if (contactData.thumbnail != null) {
                withContext(Dispatchers.IO) {
                    ContactsHelper.getContactPhotoThumbnail(this@CallService, contactData.thumbnail)
                }
            } else {
                null
            }

            val notification = buildNotification(call, callerNumber, contactData.name, contactPhoto)
            NotificationManagerCompat.from(this@CallService)
                .notify(CALL_NOTIFICATION_ID, notification)

            callerInfo = CallerInfo(
                callerName = contactData.name,
                rawPhoneNumber = callerNumber,
                callerPhoto = contactData.thumbnail,
                formattedPhoneNumber = formattedPhoneNumber
            )
            updateCallerInfo()
        }
    }

    private fun formatPhoneNumber(number: String): String {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val phoneNumber = runCatching { phoneUtil.parse(number, null) }
            .getOrElse { return number }
        return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        NotificationManagerCompat.from(this).cancel(CALL_NOTIFICATION_ID)
        call.unregisterCallback(callCallback)

        currentCallState = Call.STATE_DISCONNECTED
    }

    private fun buildNotification(
        call: Call,
        callerNumber: String,
        callerName: String?,
        callersPhoto: Bitmap?
    ): Notification {
        val acceptPendingIntent =
            getPendingIntent(Intent(CALL_INTENT_ACTION).putExtra(ACTION_EXTRA_KEY, ACCEPT_CALL), 1)
        val declinePendingIntent =
            getPendingIntent(Intent(CALL_INTENT_ACTION).putExtra(ACTION_EXTRA_KEY, DECLINE_CALL), 2)

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

    val currentCall: Call?
        get() = calls.firstOrNull()

    fun playDtmfTone(digit: Char) {
        scope.launch {
            currentCall?.playDtmfTone(digit)
            delay(200)
            currentCall?.stopDtmfTone()
        }
    }

    fun cancelCall() {
        if (currentCall == null) return

        if (currentCallState == Call.STATE_RINGING) {
            rejectCall()
        } else {
            disconnectCall()
        }
    }

    fun acceptCall() {
        currentCall?.let { it.answer(it.details.videoState) }
    }

    private fun rejectCall() {
        currentCall?.reject(false, "")
    }

    private fun disconnectCall() {
        currentCall?.disconnect()
    }

    override fun onBind(intent: Intent): IBinder? {
        if (intent.action == CUSTOM_BIND_ACTION) {
            return binder;
        }
        return super.onBind(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@CallService
    }

    private fun getPendingIntent(intent: Intent, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    companion object {
        const val CALL_INTENT_ACTION = "com.bnyro.contacts.CALL_ACTION"
        const val ACTION_EXTRA_KEY = "call_action"
        const val ACCEPT_CALL = "ACCEPT"
        const val DECLINE_CALL = "DECLINE"
        const val CALL_NOTIFICATION_ID = 10
        const val CUSTOM_BIND_ACTION = "custom_bind"
    }
}