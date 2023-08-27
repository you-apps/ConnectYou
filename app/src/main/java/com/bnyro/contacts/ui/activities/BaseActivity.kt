package com.bnyro.contacts.ui.activities

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.ThemeModel
import com.bnyro.contacts.util.PermissionHelper

abstract class BaseActivity : ComponentActivity() {
    lateinit var themeModel: ThemeModel
    val contactsModel by viewModels<ContactsModel> {
        ContactsModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionHelper.checkPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_CONTACTS
            )
        )
        val viewModelProvider = ViewModelProvider(this)
        themeModel = viewModelProvider.get()
    }
}
