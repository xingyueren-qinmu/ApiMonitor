package com.softsec.mobsec.dae.apimonitor.util

import android.graphics.drawable.Drawable

data class AppInfo constructor(val appName: String, val pkgName: String, val appIcon: Drawable, var isChecked: Boolean)

data class RecordInfo constructor(val appName: String, val pkgName: String, val appIcon: Drawable, val logAbsolutePath: String, val isTesting: Boolean, val logDate: String)