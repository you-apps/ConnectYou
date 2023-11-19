package com.bnyro.contacts.ui.activities

import android.os.Bundle
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

class CallActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                            // TODO: finish()
                        }
                    }
                }
            }
        }
    }
}