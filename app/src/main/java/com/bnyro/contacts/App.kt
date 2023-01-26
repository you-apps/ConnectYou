package com.bnyro.contacts

import android.app.Application
import com.bnyro.contacts.util.ShortcutHelper

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        ShortcutHelper.createShortcuts(this)
    }
}
