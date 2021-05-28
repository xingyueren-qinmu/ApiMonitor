package com.softsec.mobsec.dae.apimonitor.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import com.softsec.mobsec.dae.apimonitor.R
import com.softsec.mobsec.dae.apimonitor.ui.MainActivity
import com.softsec.mobsec.dae.apimonitor.util.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Call
import okhttp3.MultipartBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.lang.StringBuilder
import java.util.*
import kotlin.concurrent.schedule

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("Registered")
class MainService : Service() {

    private lateinit var serverAddress : String
    private val binder = MainServiceBinder()


    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SharedPreferencesUtil.context = applicationContext
        createForregroundNotification()
        if (intent != null) {
            if(intent.getBooleanExtra(Config.INTENT_DIALING_MOD, true))  {
                serverAddress = intent.getStringExtra(Config.INTENT_SERVER_ADDRESS)
                connectServer()
            }
            else {
                Timer().schedule(0, 5000) {
                    copyLog()
                }
            }

        } else {
            Log.e("[MainService]", "Intent Null")
        }
        if (!File(Config.PATH_TESTING_LOG).exists()) File(Config.PATH_TESTING_LOG).mkdirs()
        if (!File(Config.PATH_HISTORY_LOG).exists()) File(Config.PATH_HISTORY_LOG).mkdirs()
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createForregroundNotification() {
        val builder = NotificationCompat.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID)
        val mainActivityIntent = Intent(this@MainService, MainActivity::class.java)
        mainActivityIntent.putExtra(Config.INTENT_FROM_NITIFICATION, true)
        val notification = builder
            .setContentIntent(PendingIntent.getActivity(this, 0, mainActivityIntent, 0))
            .setContentTitle("DAEAM")
            .setContentText("点击结束所有Hook")
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
        startForeground(Config.DAEAM_FGNOTIFICATION_ID, notification)
    }

    @SuppressLint("CommitPrefEdits")
    private fun connectServer() {


        Timer().schedule(5000, 10000) {

            // Requst tasks from server.
            val sb = StringBuffer()
            HttpUtil.sendGetRequest(
                sb.append("http://").append(serverAddress).append(Config.HTTP_PATH_MOBSEC)
                    .append("?").append(Config.HTTP_ARG_CONDITION).append("waiting").toString(),
                object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("connection", e.message)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseData = response.body?.string()
                        Log.i("test", (responseData is String).toString())
                    }

                })

            copyLog()
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun copyLog() {
        // When no app to hook, stop copying log files.
        val appsToHookString = SharedPreferencesUtil.getString(Config.SP_APPS_TO_HOOK)
        if (appsToHookString != "") {
            val apps = appsToHookString.split(";").toMutableList()
            for (app in apps) {
                val appLogFile = File(SharedPreferencesUtil.getString(app + Config.SP_TARGET_APP_LOG_DIR))
                if (appLogFile.exists()) {
                    if (!File(Config.PATH_TESTING_LOG).exists()) {
                        File(Config.PATH_TESTING_LOG).mkdirs()
                        File(Config.PATH_HISTORY_LOG).mkdirs()
                    }
                    Util.execRootCmd("cp ${appLogFile.absolutePath} ${Config.PATH_TESTING_LOG + app}")
                }
            }
        }
        val exApps = SharedPreferencesUtil.getString(Config.SP_EX_APPS_TO_HOOK)
        if(exApps != "") {
            val apps = exApps.split(";").toMutableList()
            for (app in apps) {
                if(app != "") {
                    val appTestingLogFile = File(Config.PATH_TESTING_LOG + app)
                    val historyFile = File(Config.PATH_HISTORY_LOG + "apimonitor@" + Util.getDate() + "--" + app)
                    if (appTestingLogFile.exists()) {
                        Util.execRootCmd("cp ${appTestingLogFile.absolutePath} ${historyFile.absolutePath}")
                        if(historyFile.exists()) {
                            FileUtil.writeToFile("}", historyFile.absolutePath)
                        }
                        appTestingLogFile.delete()
                    }
                    val appLogFile = File(SharedPreferencesUtil.getString(app + Config.SP_TARGET_APP_LOG_DIR))
                    if (appLogFile.exists()) {
                        appLogFile.delete()
                        appLogFile.parentFile.delete()
                    }
                    SharedPreferencesUtil.clearAppInfos(app)
                    Config.MOD_DAE_TESTING = if(Config.MOD_DAE_TESTING_PKGNAME == app) {
                        Config.MOD_DAE_TESTING_PKGNAME = ""
                        sendBroadcast(
                            Intent(Config.INTENTFILTER_BC_DAE_TEST_RESULT)
                                .setComponent(Config.INTENT_BC_COMPONENT)
                                .putExtra(Config.INTENT_BC_RES_PATH, historyFile.absolutePath)
                        )
                        false
                    } else Config.MOD_DAE_TESTING
                }
                SharedPreferencesUtil.remove(Config.SP_EX_APPS_TO_HOOK)
            }
        }
    }

//    private fun parseLog(file : File) {
//        val tmp = File(file.parent + "/tmp")
//        synchronized(tmp) {
//            var size = -1
//            tmp.writeText("{")
//            val reader = file.bufferedReader()
//            var buffer = CharArray(4 * 1024)
//            while(reader.read(buffer).also { size = it } != -1) {
//                var string = String(buffer).substring(0, size)
//                if(size < 4 * 1024) {
//                    string = string.substring(0, string.lastIndexOf(","))
//                }
//                tmp.appendText(string)
//            }
//            reader.close()
//            tmp.appendText("}")
//            tmp.copyTo(file, overwrite = true, bufferSize = 4 * 1024)
//        }
//    }


    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }


    inner class MainServiceBinder : Binder() {

        fun uploadRecord(recordPath: String) {
            val sb = StringBuilder()
            val bodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            val body = File(recordPath).asRequestBody("text/plain; charset=utf-8".toMediaType())
            bodyBuilder.addFormDataPart("record", File(recordPath).name, body)
            HttpUtil.sendPostRequest(
                sb.append("http://").append(serverAddress).append(Config.HTTP_PATH_IOVSEC)
                    .append(Config.HTTP_PATH_UPLOAD).toString(),
                bodyBuilder.build(),
                object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("connection", e.message)
                    }
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful)
                            Handler(Looper.getMainLooper()).post { Toast.makeText(applicationContext, "上传成功", Toast.LENGTH_SHORT).show() }
                    }
                })
        }
    }

}