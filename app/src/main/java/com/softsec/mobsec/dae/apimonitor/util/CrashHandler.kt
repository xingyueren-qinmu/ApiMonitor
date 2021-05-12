package com.softsec.mobsec.dae.apimonitor.util

import android.annotation.SuppressLint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CrashHandler : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd")

    fun init() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val exceptionLogDir = File(Config.PATH_EXCEPTION_LOG)
        if(!exceptionLogDir.exists()) {
            exceptionLogDir.mkdirs()
        }
        FileUtil.writeToFile("$thread, ${throwable.message}",
            "${Config.PATH_EXCEPTION_LOG}${sdf.format(Date())}.log")
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
