package com.softsec.mobsec.dae.apimonitor.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.softsec.mobsec.dae.apimonitor.R
import com.softsec.mobsec.dae.apimonitor.util.Config
import kotlinx.android.synthetic.main.activity_content_record.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class RecordContentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_record)
        val logAbsolutePath = intent.getStringExtra(Config.INTENT_RESULTCONTENT_LOGPATH)
        readLogFile(logAbsolutePath)
    }

    private fun readLogFile(path : String) {
        val logFile = File(path)
        tv_record_filename.text = logFile.name
        val br = BufferedReader(FileReader(logFile))
        val sb = StringBuffer()
        var line : String
        while (true) {
            line = br.readLine() ?: break
            sb.append(line)
        }
        tv_record_content.text = toPrettyFormat(sb.toString())
    }

    private fun toPrettyFormat(json : String) : String {
        val jo = JsonParser.parseString(json)
        return GsonBuilder().setPrettyPrinting().create().toJson(jo)
    }
}