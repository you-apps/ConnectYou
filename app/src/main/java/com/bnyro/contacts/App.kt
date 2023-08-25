package com.bnyro.contacts

import android.app.Application
import com.bnyro.contacts.db.DatabaseHolder
import com.bnyro.contacts.util.DeviceContactsRepository
import com.bnyro.contacts.util.LocalContactsRepository
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

    override fun onCreate() {
        super.onCreate()

        ShortcutHelper.createShortcuts(this)

        DatabaseHolder.init(this)

        Preferences.init(this)

        BackupWorker.enqueue(this)

        NotificationHelper.createChannels(this)
    }
}
