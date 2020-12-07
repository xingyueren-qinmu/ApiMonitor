package com.softsec.mobsec.dae.apimonitor.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.softsec.mobsec.dae.apimonitor.util.Config
import com.softsec.mobsec.dae.apimonitor.util.SharedPreferencesUtil

class DaeBCReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if((intent != null) and (context != null) ) {
            val fromStr = intent!!.getStringExtra(Config.INTENT_BC_FROM)
            Log.i("from", fromStr)
            if(fromStr?.equals(Config.DAE_PKGNAME) == true)  {
                val pkgName = intent.getStringExtra(Config.INTENT_DAE_BC_PKGNAME)
                val testIntent = intent.getBooleanExtra(Config.INTENT_DAE_BC_TEST_START, true)
                val appsToHookStr = SharedPreferencesUtil.getString(Config.SP_APPS_TO_HOOK)
                Log.i("pkgName", pkgName)
                Log.i("testIntent", testIntent.toString())
                if(testIntent) {
                    if(appsToHookStr.contains(pkgName)) return
                    val logDir = context!!.packageManager.getPackageInfo(pkgName, 0).applicationInfo.dataDir + Config.PATH_TARGET_APP_LOG
                    val dataDir = context.packageManager.getPackageInfo(pkgName, 0).applicationInfo.dataDir
                    SharedPreferencesUtil.context = context
                    SharedPreferencesUtil.addAppToHook(pkgName, false, logDir, dataDir)
                    SharedPreferencesUtil.put(Config.SP_APPS_TO_HOOK, "$appsToHookStr$pkgName;")
                    Config.MOD_DAE_TESTING = true
                    Config.MOD_DAE_TESTING_PKGNAME = pkgName
                } else {
                    if(!appsToHookStr.contains(pkgName)) return
                    SharedPreferencesUtil.removeAppToHook(pkgName)
                    Config.MOD_DAE_TESTING = false
                }
            } else return

        }
    }

}