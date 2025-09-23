package com.bnyro.contacts.ui.activities

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.Intents
import android.provider.ContactsContract.QuickContact
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.navigation.HomeRoutes
import com.bnyro.contacts.navigation.NavContainer
import com.bnyro.contacts.navigation.NavRoutes
import com.bnyro.contacts.presentation.features.AddToContactDialog
import com.bnyro.contacts.presentation.features.ConfirmImportContactsDialog
import com.bnyro.contacts.presentation.screens.contact.SingleContactScreen
import com.bnyro.contacts.ui.theme.ConnectYouTheme
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.BiometricAuthUtil
import com.bnyro.contacts.util.ContactsHelper
import com.bnyro.contacts.util.IntentHelper
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.util.extension.parcelable
import com.bnyro.contacts.util.rememberPreference
import java.net.URLDecoder

class MainActivity : BaseActivity() {
    private val smsSendIntents = listOf(
        Intent.ACTION_VIEW,
        Intent.ACTION_SEND,
        Intent.ACTION_SENDTO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        contactsModel.initialInsertContactData = getInsertContactData()

        setContent {
            ConnectYouTheme(themeModel.themeMode) {
                val context = LocalContext.current

                var authSuccess by rememberSaveable {
                    mutableStateOf(!Preferences.getBoolean(Preferences.biometricAuthKey, false))
                }

                if (authSuccess) {
                    val enabledTabs by rememberPreference(
                        Preferences.enabledTabsKey,
                        HomeRoutes.all.indices.toList()
                    )
                    val initialTabIndex by rememberPreference(
                        Preferences.homeTabKey,
                        HomeRoutes.all.indexOfFirst { it.route is HomeRoutes.Contacts })

                    NavContainer(
                        enabledTabs = HomeRoutes.all.filterIndexed { index, _ ->
                            enabledTabs.contains(index)
                        },
                        initialTab = (HomeRoutes.all.getOrNull(initialTabIndex.takeIf {
                            enabledTabs.contains(it)
                        } ?: -1) ?: HomeRoutes.all[enabledTabs.first()]).route
                    )

                    var initialContactId by remember {
                        mutableStateOf(getInitialContactId())
                    }
                    contactsModel.contacts.find { it.contactId == initialContactId }
                        ?.let { contact ->
                            LaunchedEffect(Unit) {
                                contactsModel.loadAdvancedContactData(contact)
                            }

                            SingleContactScreen(contact, contactsModel) {
                                initialContactId = null
                            }
                        }

                    var insertOrEditNumber by remember {
                        mutableStateOf(getInsertOrEditNumber())
                    }
                    insertOrEditNumber?.let {
                        AddToContactDialog(it) {
                            insertOrEditNumber = null
                        }
                    }

                    var sharedVcfUri by remember {
                        mutableStateOf(getSharedVcfUri())
                    }
                    sharedVcfUri?.let {
                        ConfirmImportContactsDialog(contactsModel, it) {
                            sharedVcfUri = null
                        }
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
        processIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        processIntent(intent)
        super.onNewIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        val initialSmsAddressAndBody = getInitialSmsAddressAndBody()
        if (initialSmsAddressAndBody != null) {
            openSMSThread(initialSmsAddressAndBody.first, initialSmsAddressAndBody.second)
        }

        var number: String? = null
        if (intent.action == Intent.ACTION_INSERT_OR_EDIT) {
            number = intent.getStringExtra(Intents.Insert.PHONE)
        } else if (intent.action == Intent.ACTION_DIAL || intent.action == Intent.ACTION_VIEW) {
            if (intent.data?.scheme == "tel") {
                number = intent.data?.schemeSpecificPart
            }
        }
        if (number != null) openDialPad(number)

        var address: String? = null
        if (intent.action == Intent.ACTION_INSERT_OR_EDIT) {
            address = intent.getStringExtra(Intents.Insert.PHONE)
        } else if (intent.action == Intent.ACTION_DIAL || intent.action == Intent.ACTION_VIEW) {
            if (intent.data?.scheme == "sms") {
                address = intent.data?.schemeSpecificPart
            }
        }
        if (address != null) openSMSThread(address, null)
    }

    private fun openDialPad(number: String) {
        val deepLinkIntent = Intent(
            HomeRoutes.Phone.navAction,
            HomeRoutes.Phone.getDeepLink(number),
            this,
            MainActivity::class.java
        )

        val deepLinkPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(
                10,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        deepLinkPendingIntent?.send()
    }

    private fun openSMSThread(address: String, body: String?) {
        val deepLinkIntent = Intent(
            NavRoutes.MessageThread.navAction,
            NavRoutes.MessageThread.getDeepLink(address, body),
            this,
            MainActivity::class.java
        )

        val deepLinkPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(
                11,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        deepLinkPendingIntent?.send()
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

        val address = intent?.data?.schemeSpecificPart
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
