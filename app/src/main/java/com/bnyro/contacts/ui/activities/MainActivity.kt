package com.bnyro.contacts.ui.activities

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Intents
import android.provider.ContactsContract.QuickContact
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ValueWithType
import com.bnyro.contacts.ui.components.dialogs.AddToContactDialog
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.SmsModel
import com.bnyro.contacts.ui.screens.MainAppContent
import com.bnyro.contacts.ui.theme.ConnectYouTheme
import com.bnyro.contacts.util.BackupHelper

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contactsModel.initialContactId = getInitialContactId()
        contactsModel.initialContactData = getInsertContactData()
        handleVcfShareAction(contactsModel)

        setContent {
            val contactsModel: ContactsModel = viewModel()
            smsModel = viewModel()

            ConnectYouTheme(themeModel.themeMode) {
                MainAppContent(contactsModel, smsModel!!)
                getInsertOrEditNumber()?.let {
                    AddToContactDialog(it)
                }
            }
        }
    }

    private fun getInsertContactData(): ContactData? {
        return when {
            intent?.action == Intent.ACTION_INSERT -> {
                val name = intent.getStringExtra(Intents.Insert.NAME)
                    ?: intent.getStringExtra(Intents.Insert.PHONETIC_NAME)
                ContactData(
                    displayName = name,
                    firstName = name?.split(" ")?.firstOrNull(),
                    surName = name?.split(" ", limit = 2)?.lastOrNull(),
                    organization = intent.getStringExtra(Intents.Insert.COMPANY),
                    numbers = listOfNotNull(
                        intent.getStringExtra(Intents.Insert.PHONE)?.let {
                            ValueWithType(it, 0)
                        }
                    ),
                    emails = listOfNotNull(
                        intent.getStringExtra(Intents.Insert.EMAIL)?.let {
                            ValueWithType(it, 0)
                        }
                    ),
                    notes = listOfNotNull(
                        intent.getStringExtra(Intents.Insert.NOTES)?.let {
                            ValueWithType(it, 0)
                        }
                    ),
                    addresses = listOfNotNull(
                        intent.getStringExtra(Intents.Insert.POSTAL)?.let {
                            ValueWithType(it, 0)
                        }
                    )
                )
            }
            intent?.getStringExtra("action") == "create" -> ContactData()
            else -> null
        }
    }

    private fun getInitialContactId(): Long? {
        return when (intent?.action) {
            Intent.ACTION_EDIT, Intent.ACTION_VIEW, QuickContact.ACTION_QUICK_CONTACT -> intent?.data?.lastPathSegment?.toLongOrNull()
            else -> null
        }
    }

    private fun getInsertOrEditNumber(): String? {
        return when (intent?.action) {
            Intent.ACTION_INSERT_OR_EDIT -> intent?.getStringExtra(Intents.Insert.PHONE)
            else -> null
        }
    }

    private fun handleVcfShareAction(contactsModel: ContactsModel) {
        if (intent.action != Intent.ACTION_VIEW || intent?.type !in BackupHelper.vCardMimeTypes) return
        contactsModel.importVcf(this, intent?.data ?: return)
    }

    override fun onDestroy() {
        super.onDestroy()
        smsModel = null
    }

    companion object {
        var smsModel: SmsModel? = null
    }
}
