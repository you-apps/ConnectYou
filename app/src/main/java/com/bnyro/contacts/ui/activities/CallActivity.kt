package com.bnyro.contacts.ui.activities

import android.os.Bundle
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
import com.bnyro.contacts.ui.models.DialerModel
import com.bnyro.contacts.ui.screens.DialerScreen
import com.bnyro.contacts.ui.theme.ConnectYouTheme

class CallActivity : BaseActivity() {
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

    companion object {
        private const val windowFlags =
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    }
}
