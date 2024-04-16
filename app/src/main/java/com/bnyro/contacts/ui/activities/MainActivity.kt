package com.bnyro.contacts.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.Intents
import android.provider.ContactsContract.QuickContact
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.bnyro.contacts.ext.parcelable
import com.bnyro.contacts.nav.NavContainer
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.ConfirmImportContactsDialog
import com.bnyro.contacts.ui.components.dialogs.AddToContactDialog
import com.bnyro.contacts.ui.theme.ConnectYouTheme
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.BiometricAuthUtil
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.util.IntentHelper
import java.net.URLDecoder

class MainActivity : BaseActivity() {
    private val smsSendIntents = listOf(
        Intent.ACTION_VIEW,
        Intent.ACTION_SEND,
        Intent.ACTION_SENDTO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contactsModel.initialContactId = getInitialContactId()
        contactsModel.initialContactData = getInsertContactData()

        smsModel.initialAddressAndBody = getInitialSmsAddressAndBody()

        val initialTabIndex = smsModel.initialAddressAndBody?.let { 1 }
            ?: Preferences.getInt(Preferences.homeTabKey, 0)
        setContent {
            ConnectYouTheme(themeModel.themeMode) {
                val context = LocalContext.current

                var authSuccess by rememberSaveable {
                    mutableStateOf(!Preferences.getBoolean(Preferences.biometricAuthKey, false))
                }

                if (authSuccess) {
                    NavContainer(initialTabIndex)
                    getInsertOrEditNumber()?.let {
                        AddToContactDialog(it)
                    }
                    getSharedVcfUri()?.let {
                        ConfirmImportContactsDialog(contactsModel, it)
                    }
                }

                LaunchedEffect(Unit) {
                    if (authSuccess) return@LaunchedEffect

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        BiometricAuthUtil.requestAuth(context) { success ->
                            if (success) authSuccess = true
                            else finish()
                        }
                    }
                }
            }
        }
    }

    private fun getInsertContactData(): ContactData? {
        return when {
            intent?.action == Intent.ACTION_INSERT -> {
                IntentHelper.extractContactFromIntent(intent)
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

    private fun getInitialSmsAddressAndBody(): Pair<String, String?>? {
        if (intent?.action !in smsSendIntents || intent?.type in BackupHelper.vCardMimeTypes) return null

        val address = intent?.dataString
            ?.split(":")
            ?.lastOrNull()
            // the number is url encoded and hence must be decoded first
            ?.let { URLDecoder.decode(it, "UTF-8") }
            ?: return null
        val body = intent?.getStringExtra(Intent.EXTRA_TEXT)

        return ContactsHelper.normalizePhoneNumber(address) to body
    }

    private fun getSharedVcfUri(): Uri? {
        if (intent?.type !in BackupHelper.vCardMimeTypes) return null

        val uri = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> intent.parcelable<Uri>(Intent.EXTRA_STREAM)
            else -> null
        }

        return uri
    }
}
