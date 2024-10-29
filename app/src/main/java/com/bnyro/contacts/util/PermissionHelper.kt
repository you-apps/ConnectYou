package com.bnyro.contacts.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.Telephony
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService

object PermissionHelper {
    fun checkPermissions(activity: Activity, permissions: Array<String>): Boolean {
        if (permissions.isEmpty()) return true

        if (!hasPermission(activity, *permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, 1)
            return false
        }

        return true
    }

    fun hasPermission(context: Context, vararg permissions: String): Boolean {
        return permissions.all { permission ->
            ActivityCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun canBlockNumbers(context: Context): Boolean {
        if (!BlockedNumberContract.canCurrentUserBlockNumbers(context)) return false

        return Telephony.Sms.getDefaultSmsPackage(context) == context.packageName ||
                context.getSystemService<TelecomManager>()?.defaultDialerPackage == context.packageName
    }
}
