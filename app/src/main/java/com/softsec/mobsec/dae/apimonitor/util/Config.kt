package com.softsec.mobsec.dae.apimonitor.util

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.Environment

class Config {
    companion object {

        const val DAEAM_PKGNAME = "com.softsec.mobsec.dae.apimonitor"
        const val DAE_PKGNAME = "com.softsec.mobsec.dae"

        // SharedPreferences
        const val SP_NAME = "DAEAM_SP"
        const val SP_APPS_TO_HOOK = "SP_APPS_TO_HOOK_KEY"
        const val SP_EX_APPS_TO_HOOK = "SP_EX_APPS_TO_HOOK"
        const val SP_SERVER_ADDRESS = "SP_SERVER_ADDRESS_KEY"
        const val SP_HAS_W_PERMISSION = "_HAS_W_PERMISSION_KEY"
        const val SP_TARGET_APP_LOG_DIR = "_LOG_DIR"
        const val SP_TARGET_APP_DIR = "_APP_DIR"
        const val SP_IS_DIALING_MOD = "SP_IS_DIALING_MOD"

        // Intent
        const val INTENT_SERVER_ADDRESS = "INTENT_SERVER_ADDRESS"
        const val INTENT_RESULTCONTENT_LOGPATH = "INTENT_RESULTCONTENT_LOGPATH"
        const val INTENT_APP_NAME = "INTENT_APP_NAME"
        const val INTENT_FROM_NITIFICATION = "INTENT_FROM_NOTIFICATION"
        const val INTENT_DIALING_MOD = "INTENT_DIALING_MOD"
        const val INTENT_BC_FROM = "INTENT_BC_FROM"
        const val INTENT_DAE_BC_PKGNAME = "INTENT_DAE_BC_PKGNAME"
        const val INTENT_DAE_BC_TEST_START = "INTENT_DAE_BC_TEST_START"
        const val INTENT_BC_RES_PATH = "INTENT_BC_APIMONITOR_RES_PATH"
        const val INTENTFILTER_BC_DAE_TEST_RESULT = "ACTION_DAE_APIMONITOR_RES"
        const val INTENTFILTER_BC_DAE_TEST_ACK = "ACTION_DAE_APIMONITOR_ACK"
        val INTENT_BC_COMPONENT = ComponentName("com.softsec.mobsec.dae", "com.softsec.mobsec.dae.ApiMonitorReceiver")



        // File Paths
        @SuppressLint("SdCardPath")
        const val PATH_DAEAM_INTERNAL = "/data/data/$DAEAM_PKGNAME"
        private val PATH_DAEAM_DATA = "${Environment.getExternalStorageDirectory().absolutePath}/Android/data/$DAEAM_PKGNAME"
        val PATH_TESTING_LOG = "$PATH_DAEAM_DATA/testing/"
        val PATH_HISTORY_LOG = "$PATH_DAEAM_DATA/history/"
        const val PATH_TARGET_APP_LOG = "/DAEAM_testing/"
        const val PATH_SP = "/shared_prefs/"

        const val DAEAM_FGNOTIFICATION_ID = 2527

        // HTTP
        const val HTTP_ARG_CONDITION = "cond="
        const val HTTP_PATH_UPLOAD = "/upload"
        const val HTTP_PATH_MOBSEC = "/mobsec"
        const val HTTP_PATH_IOVSEC = "/iovsec"

        var MOD_DAE_TESTING = false
        var MOD_DAE_TESTING_PKGNAME = ""
    }


}