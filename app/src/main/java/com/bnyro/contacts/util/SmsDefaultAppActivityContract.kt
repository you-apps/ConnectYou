package com.bnyro.contacts.util

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.getSystemService

class SmsDefaultAppActivityContract : ActivityResultContract<Unit, Boolean>() {
    override fun createIntent(
        context: Context,
        input: Unit
    ): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService<RoleManager>()!!

            return roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
        } else {
            return Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                .putExtra(
                    Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                    context.packageName
                )
        }
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}