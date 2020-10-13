package com.softsec.mobsec.dae.apimonitor.util

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class HttpUtil {



    companion object {
        fun sendGetRequest(address: String, callback: Callback) {
            OkHttpClient().newCall(Request.Builder().url(address).build()).enqueue(callback)
        }

        fun sendPostRequest(address: String, body: RequestBody, callback: Callback) {
            OkHttpClient().newCall(Request.Builder().post(body).url(address).build()).enqueue(callback)
        }
    }
}