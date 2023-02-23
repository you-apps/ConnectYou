package com.bnyro.contacts

import android.app.Application
import com.bnyro.contacts.db.DatabaseHolder
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.util.ShortcutHelper
import com.bnyro.contacts.workers.BackupWorker

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        ShortcutHelper.createShortcuts(this)

        DatabaseHolder.init(this)

        Preferences.init(this)

        BackupWorker.enqueue(this)
    }
}
