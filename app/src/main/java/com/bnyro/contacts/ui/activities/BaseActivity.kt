package com.bnyro.contacts.ui.activities

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.settings.model.ThemeModel
import com.bnyro.contacts.presentation.screens.sms.model.SmsModel
import com.bnyro.contacts.util.NotificationHelper
import com.bnyro.contacts.util.PermissionHelper

abstract class BaseActivity : FragmentActivity() {
    lateinit var themeModel: ThemeModel
    val contactsModel by viewModels<ContactsModel> {
        ContactsModel.Factory
    }
    val smsModel by viewModels<SmsModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requiredPermissions =
            smsPermissions + contactPermissions + NotificationHelper.notificationPermissions

        requestDefaultSMSApp(this)
        PermissionHelper.checkPermissions(
            this,
            requiredPermissions
        )
        val viewModelProvider = ViewModelProvider(this)
        themeModel = viewModelProvider.get()
    }

    private fun requestDefaultSMSApp(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    getSmsPermissions()
                } else {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    context.startActivity(intent)
                }
            }
        } else {
            if (Telephony.Sms.getDefaultSmsPackage(context) == context.packageName) {
                getSmsPermissions()
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                context.startActivity(intent)
            }
        }
    }

    private fun getSmsPermissions() {
        PermissionHelper.checkPermissions(this, smsPermissions)
    }

    companion object {
        private val smsPermissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
        private val contactPermissions = arrayOf(
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_SYNC_SETTINGS
        )
    }
}
