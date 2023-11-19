package com.bnyro.contacts.ui.models

import android.Manifest
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import androidx.lifecycle.ViewModel

class DialerModel: ViewModel() {
    var initialPhoneNumber: String? = null

    fun requestDefaultDialerApp(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
        val isAlreadyDefaultDialer =
            context.packageName.equals(telecomManager!!.defaultDialerPackage)
        if (!isAlreadyDefaultDialer) {
            val intent: Intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    context.packageName
                )
            context.startActivity(intent)
        }
    }

    companion object {
        val phonePerms = arrayOf(Manifest.permission.CALL_PHONE)
    }
}