package com.bnyro.contacts.services

import android.app.Service
import android.content.Intent

class HeadlessSmsSendService: Service() {
    override fun onBind(p0: Intent?) = null
}