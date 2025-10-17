package net.android.st069_fakecallphoneprank

import android.app.Application
import android.content.Context
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

class FakeCallApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize locale when app starts
        LocaleHelper.setLocale(this)
    }

    override fun attachBaseContext(base: Context) {
        // Apply saved locale to application context
        super.attachBaseContext(LocaleHelper.setLocale(base))
    }
}
