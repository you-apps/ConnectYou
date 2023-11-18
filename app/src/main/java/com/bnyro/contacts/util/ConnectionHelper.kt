package com.bnyro.contacts.util

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager

object ConnectionHelper {
    fun hasSignalForSms(context: Context): Boolean {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (telephonyManager.simState !in listOf(
                TelephonyManager.SIM_STATE_READY,
                TelephonyManager.SIM_STATE_UNKNOWN
            )
        ) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return (telephonyManager.signalStrength?.level ?: -1) > 0
        }

        return true
    }
}
