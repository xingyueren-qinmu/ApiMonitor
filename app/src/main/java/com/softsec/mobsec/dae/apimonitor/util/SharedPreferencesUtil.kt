package com.softsec.mobsec.dae.apimonitor.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtil {

    companion object {
        lateinit var context : Context
        lateinit var sp : SharedPreferences

        fun addAppToHook(pkgName : String, writePerm : Boolean, logDir : String, dataDir : String) {
            if(!::sp.isInitialized) sp = context.getSharedPreferences(Config.SP_NAME, Context.MODE_PRIVATE)
            sp.edit()
                .putString(Config.SP_APPS_TO_HOOK, sp.getString(Config.SP_APPS_TO_HOOK, "")!! + pkgName + ";")
                .putBoolean(pkgName + Config.SP_HAS_W_PERMISSION, writePerm)
                .putString(pkgName + Config.SP_TARGET_APP_LOG_DIR, logDir + pkgName)
                .putString(pkgName + Config.SP_TARGET_APP_DIR, dataDir)
                .apply()

        }

        @SuppressLint("CommitPrefEdits")
        fun put(key : String, value : Any) : SharedPreferences.Editor{
            if(!::sp.isInitialized) sp = context.getSharedPreferences(Config.SP_NAME, Context.MODE_PRIVATE)
            when(value) {
                is String -> sp.edit().putString(key, value)
                is Boolean -> sp.edit().putBoolean(key, value)
                is Int -> sp.edit().putInt(key, value)
            }
            return sp.edit()
        }

        fun getString(key : String) : String {
            if(!::sp.isInitialized) sp = context.getSharedPreferences(Config.SP_NAME, Context.MODE_PRIVATE)
            return sp.getString(key, "")!!
        }

        fun getBoolean(key : String) : Boolean {
            if(!::sp.isInitialized) sp = context.getSharedPreferences(Config.SP_NAME, Context.MODE_PRIVATE)
            return sp.getBoolean(key, false)
        }

        fun remove(key : String) : SharedPreferences.Editor {
            if(!::sp.isInitialized) sp = context.getSharedPreferences(Config.SP_NAME, Context.MODE_PRIVATE)
            return sp.edit().remove(key)
        }

        fun clearAppInfos(pkgName : String) {
            if(!::sp.isInitialized) sp = context.getSharedPreferences(Config.SP_NAME, Context.MODE_PRIVATE)
            sp.edit()
                .remove(pkgName + Config.SP_TARGET_APP_DIR)
                .remove(pkgName + Config.SP_TARGET_APP_LOG_DIR)
                .remove(pkgName + Config.SP_HAS_W_PERMISSION)
                .apply()
        }

        fun removeAppToHook(pkgName : String) {
            if(!::sp.isInitialized) sp = context.getSharedPreferences(Config.SP_NAME, Context.MODE_PRIVATE)
            sp.edit()
                .putString(Config.SP_EX_APPS_TO_HOOK, sp.getString(Config.SP_EX_APPS_TO_HOOK, "")!! + pkgName + "")
                .putString(Config.SP_APPS_TO_HOOK, sp.getString(Config.SP_APPS_TO_HOOK, "")!!
                    .replace("$pkgName;", ""))
                .apply()
        }
    }
}