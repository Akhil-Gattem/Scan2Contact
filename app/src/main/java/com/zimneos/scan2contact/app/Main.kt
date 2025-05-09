package com.zimneos.scan2contact.app

import android.app.Application
import com.zimneos.scan2contact.local.DatabaseProvider

class Main : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseProvider.getDatabase(this)
    }
}