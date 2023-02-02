package com.bnyro.contacts.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object PermissionHelper {
    fun checkPermissions(context: Context, permission: String): Boolean {
        if (!hasPermission(context, permission)) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(permission, Manifest.permission.WRITE_CONTACTS),
                1
            )
            return false
        }
        return true
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
