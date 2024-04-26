package com.bnyro.contacts.util.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bnyro.contacts.util.ClipboardHelper

class CopyTextReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.getStringExtra(SmsReceiver.KEY_EXTRA_TEXT) ?: return
        ClipboardHelper(context).save(text)
    }
}