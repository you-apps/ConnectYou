package com.bnyro.contacts.ui.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bnyro.contacts.presentation.screens.dialer.DialerScreen
import com.bnyro.contacts.presentation.screens.dialer.model.DialerModel
import com.bnyro.contacts.ui.theme.ConnectYouTheme
import com.bnyro.contacts.util.services.CallService

class CallActivity : BaseActivity() {

    lateinit var callService: CallService

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = (service as CallService.LocalBinder)
            callService = binder.getService()
            dialerModel.playDtmfTone = callService::playDtmfTone
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            dialerModel.playDtmfTone = {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.addFlags(windowFlags)

        val dialerModel: DialerModel = ViewModelProvider(this).get()

        setContent {
            ConnectYouTheme {
                Scaffold { pV ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(pV)
                    ) {
                        DialerScreen(contactsModel = contactsModel, dialerModel = dialerModel) {
                            this@CallActivity.finish()
                        }
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

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    companion object {
        private const val windowFlags =
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    }
}
