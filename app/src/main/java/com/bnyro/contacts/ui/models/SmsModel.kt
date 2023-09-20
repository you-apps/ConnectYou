package com.bnyro.contacts.ui.models

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bnyro.contacts.obj.SmsData
import com.bnyro.contacts.util.NotificationHelper
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.SmsUtil

class SmsModel: ViewModel() {
    val smsList = mutableStateListOf<SmsData>()
    val smsGroups = mutableStateMapOf<Long, MutableList<SmsData>>()

    var initialAddressAndBody by mutableStateOf<Pair<String, String?>?>(null)

    fun fetchSmsList(context: Context) {
        val requiredPermissions = smsPermissions + NotificationHelper.notificationPermissions
        if (!PermissionHelper.checkPermissions(context, requiredPermissions)) return

        requestDefaultSMSApp(context)

        val tempSmsList = SmsUtil.getSmsList(context)

        smsList.clear()
        smsGroups.clear()

        smsList.addAll(tempSmsList)
        val groups = smsList.groupBy { it.threadId }
            .map { (threadId, smsList) -> threadId to smsList.toMutableList() }
        smsGroups.putAll(groups)
    }

    fun addSmsToList(sms: SmsData) {
        smsList.add(sms)
        if (smsGroups.containsKey(sms.threadId)) {
            smsGroups[sms.threadId]?.add(sms)
        } else {
            smsGroups[sms.threadId] = mutableListOf(sms)
        }
    }

    fun deleteSms(context: Context, sms: SmsData) {
        SmsUtil.deleteMessage(context, sms.id)
        smsList.removeAll { it.id == sms.id }
        smsGroups[sms.threadId]?.removeAll { it.id == sms.id }
    }

    fun deleteThread(context: Context, threadId: Long) {
        SmsUtil.deleteThread(context, threadId)
        smsList.removeAll { it.threadId == threadId }
        smsGroups.remove(threadId)
    }

    fun sendSms(context: Context, address: String, body: String) {
        val sms = SmsUtil.sendSms(context, address, body) ?: return
        addSmsToList(sms)
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

    companion object {
        private val smsPermissions = arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
    }
}