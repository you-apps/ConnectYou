package com.bnyro.contacts.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

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
}
