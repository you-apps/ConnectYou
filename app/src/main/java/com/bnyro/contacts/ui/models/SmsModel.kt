package com.bnyro.contacts.ui.models

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.contacts.obj.SmsData
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.SmsUtil

class SmsModel: ViewModel() {
    private val smsPermissions = arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)

    var smsList by mutableStateOf(listOf<SmsData>())
    var smsGroups by mutableStateOf(mapOf<Long, List<SmsData>>())

    fun fetchSmsList(context: Context) {
        requestDefaultSMSApp(context)

        if (!PermissionHelper.checkPermissions(context, smsPermissions)) return

        smsList = SmsUtil.getSmsList(context)
        smsGroups = smsList.groupBy { it.threadId }
    }

    fun sendSms(context: Context, address: String, body: String) {
        val sms = SmsUtil.sendSms(context, address, body)
        smsList += sms
        smsGroups = smsList.groupBy { it.threadId }
    }

    private fun requestDefaultSMSApp(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    getSmsPermissions(context)
                } else {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    context.startActivity(intent)
                }
            }
        } else {
            if (Telephony.Sms.getDefaultSmsPackage(context) == context.packageName) {
                getSmsPermissions(context)
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                context.startActivity(intent)
            }
        }
    }

    private fun getSmsPermissions(context: Context) {
        PermissionHelper.checkPermissions(context, smsPermissions)
    }
}