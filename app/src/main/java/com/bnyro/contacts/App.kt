package com.bnyro.contacts

import android.app.Application
import android.util.Log
import com.bnyro.contacts.db.DatabaseHolder
import com.bnyro.contacts.repo.DeviceContactsRepository
import com.bnyro.contacts.repo.DeviceSmsRepo
import com.bnyro.contacts.repo.LocalContactsRepository
import com.bnyro.contacts.repo.LocalSmsRepo
import com.bnyro.contacts.repo.SmsRepository
import com.bnyro.contacts.util.NotificationHelper
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.util.ShortcutHelper
import com.bnyro.contacts.workers.BackupWorker

class App : Application() {
    val deviceContactsRepository by lazy {
        DeviceContactsRepository(this)
    }
    val localContactsRepository by lazy {
        LocalContactsRepository(this)
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

        ShortcutHelper.createShortcuts(this)

        DatabaseHolder.init(this)

        Preferences.init(this)

        BackupWorker.enqueue(this)

        NotificationHelper.createChannels(this)

        initSmsRepo()

        Log.e("types", DeviceContactsRepository(this).getAccountTypes().toString())
    }
}
