package com.bnyro.contacts.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object PermissionHelper {
    fun checkPermissions(context: Context, permission: String): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(permission, Manifest.permission.WRITE_CONTACTS),
                1
            )
            return false
        }
        return true
    }
}
