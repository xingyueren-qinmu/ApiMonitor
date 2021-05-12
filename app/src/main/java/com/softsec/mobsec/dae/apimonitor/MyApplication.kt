package com.softsec.mobsec.dae.apimonitor

import android.app.Application
import com.softsec.mobsec.dae.apimonitor.util.CrashHandler

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashHandler.init()
    }
}