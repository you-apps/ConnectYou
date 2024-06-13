package com.bnyro.contacts.ui.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bnyro.contacts.R
import com.bnyro.contacts.presentation.screens.dialer.DialerScreen
import com.bnyro.contacts.presentation.screens.dialer.model.DialerModel
import com.bnyro.contacts.ui.theme.ConnectYouTheme
import com.bnyro.contacts.util.services.CallService

class CallActivity : ComponentActivity() {

    lateinit var callService: CallService
    val dialerModel: DialerModel by viewModels()

    val powerManager by lazy { getSystemService(PowerManager::class.java) }

    val wakeLock by lazy {
        powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            this::class.simpleName
        )
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = (service as CallService.LocalBinder)
            callService = binder.getService()

            dialerModel.playDtmfTone = callService::playDtmfTone
            dialerModel.acceptCall = callService::acceptCall
            dialerModel.cancelCall = callService::cancelCall

            callService.onUpdateState = dialerModel::onState
            callService.onCallerInfoUpdate = dialerModel::onCallerInfoUpdate

            callService.updateState()
            callService.updateCallerInfo()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            dialerModel.playDtmfTone = {}
            dialerModel.acceptCall = {}
            dialerModel.cancelCall = {}

            callService.onUpdateState = {}
            callService.onCallerInfoUpdate = {}
        }
    }

    private val closeAlertReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getStringExtra(ACTION_EXTRA_KEY) == CLOSE_ACTION) {
                val reason = intent.getStringExtra(ACTION_EXTRA_REASON)
                if (reason != null) {
                    showAlert(getString(R.string.call_ended), reason)
                } else {
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        if (!wakeLock.isHeld) {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        }

        window.addFlags(windowFlags)

        ContextCompat.registerReceiver(
            this, closeAlertReciever, IntentFilter(
                CALL_ALERT_CLOSE_ACTION
            ), ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val dialerModel: DialerModel = ViewModelProvider(this).get()

        setContent {
            ConnectYouTheme {
                Scaffold { pV ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(pV)
                    ) {
                        DialerScreen(dialerModel = dialerModel)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, CallService::class.java).setAction(CallService.CUSTOM_BIND_ACTION)
            .also { intent ->
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
    }

    override fun onDestroy() {
        unregisterReceiver(closeAlertReciever)
        super.onDestroy()
    }

    override fun onResume() {
        if (!wakeLock.isHeld) {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        }
        super.onResume()
    }

    override fun onPause() {
        wakeLock.release()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .create()
            .show()
    }

    companion object {
        const val CALL_ALERT_CLOSE_ACTION = "com.bnyro.contacts.ALARM_ALERT_CLOSE_ACTION"
        const val ACTION_EXTRA_KEY = "action"
        const val CLOSE_ACTION = "CLOSE"
        const val ACTION_EXTRA_REASON = "reason"
        private const val windowFlags =
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    }
}
