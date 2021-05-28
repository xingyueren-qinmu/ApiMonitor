package com.softsec.mobsec.dae.apimonitor.util

import android.annotation.SuppressLint
import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

object CrashHandler : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    @SuppressLint("SimpleDateFormat")
    private val daySDF = SimpleDateFormat("yyyy-MM-dd")
    @SuppressLint("SimpleDateFormat")
    private val secondSDF = SimpleDateFormat("HH:mm:ss")

    fun init() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val exceptionLogDir = File(Config.PATH_EXCEPTION_LOG)
        if(!exceptionLogDir.exists()) {
            exceptionLogDir.mkdirs()
        }
        FileUtil.writeToFile("${secondSDF.format(Date())}-${thread}, ${formatThrowable(throwable)}",
            "${Config.PATH_EXCEPTION_LOG}${daySDF.format(Date())}.log")
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun formatThrowable(throwable: Throwable): String {
        val sb = StringBuilder();
        sb.append(throwable.message).append('\n')
        for(st in throwable.stackTrace) {
            sb.append(st.className).append("->").append(st.methodName).append("[${st.lineNumber}]")
                .append("\n")
        }
        return sb.toString()
    }
}
