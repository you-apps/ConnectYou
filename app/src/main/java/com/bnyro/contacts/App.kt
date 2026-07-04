package com.bnyro.contacts

import android.app.Application
import com.bnyro.contacts.data.database.DatabaseHolder
import com.bnyro.contacts.domain.repositories.CallLogRepository
import com.bnyro.contacts.domain.repositories.DeviceContactsRepository
import com.bnyro.contacts.domain.repositories.DeviceSmsRepo
import com.bnyro.contacts.domain.repositories.LocalContactsRepository
import com.bnyro.contacts.domain.repositories.LocalSmsRepo
import com.bnyro.contacts.domain.repositories.PhoneLookupRepository
import com.bnyro.contacts.domain.repositories.SmsRepository
import com.bnyro.contacts.util.NotificationHelper
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.util.ShortcutHelper
import com.bnyro.contacts.util.workers.BackupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    val deviceContactsRepository by lazy {
        DeviceContactsRepository(this)
    }
    val localContactsRepository by lazy {
        LocalContactsRepository(this)
    }
    val callLogRepository by lazy {
        CallLogRepository(this)
    }
    val phoneLookupRepository by lazy {
        PhoneLookupRepository(this)
    }
    lateinit var smsRepo: SmsRepository

    fun initSmsRepo() {
        smsRepo = if (Preferences.getBoolean(Preferences.storeSmsLocallyKey, false)) {
            LocalSmsRepo()
        } else {
            DeviceSmsRepo()
        }
    }

    override fun onCreate() {
        super.onCreate()

        DatabaseHolder.init(this)

        Preferences.init(this)

        BackupWorker.enqueue(this)

        NotificationHelper.createChannels(this)

        // creating shortcuts is potentially blocking/slow and thus is called from a coroutine
        // https://developer.android.com/reference/android/content/pm/ShortcutManager
        CoroutineScope(Dispatchers.IO).launch {
            ShortcutHelper.createShortcuts(this@App)
        }

        initSmsRepo()
    }
}
