package com.softsec.mobsec.dae.apimonitor.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.softsec.mobsec.dae.apimonitor.R
import com.softsec.mobsec.dae.apimonitor.service.MainService
import com.softsec.mobsec.dae.apimonitor.util.Config
import com.softsec.mobsec.dae.apimonitor.util.SharedPreferencesUtil
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("Registered")
class MainActivity : AppCompatActivity() {

    private lateinit var serverAddress : String

    private val clickListener =  View.OnClickListener { view ->

        when(view.id) {
            btn_main_chooseapp.id -> {
                startActivity(Intent(this, ChooseAppActivity::class.java))
            }

            btn_main_clearcache.id -> {
                Toast.makeText(this, "清空缓存", Toast.LENGTH_SHORT).show()
            }

            btn_main_viewRes.id -> {
                startActivity(Intent(this, RecordListActivity::class.java))
            }

            btn_main_saveaddress.id -> {
                Log.i("saveBtn", "touch")
                val host = et_main_ip.text.toString() + ":" + et_main_port.text.toString()
                serverAddress = SharedPreferencesUtil.getString(Config.SP_SERVER_ADDRESS)
                val intent = Intent(this@MainActivity, MainService::class.java)
                intent.putExtra(Config.INTENT_DIALING_MOD, true)
                if(serverAddress != host)
                    stopService(intent)
                SharedPreferencesUtil.put(Config.SP_SERVER_ADDRESS, host).apply()
                intent.putExtra(Config.INTENT_SERVER_ADDRESS, host)
                startService(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesUtil.context = applicationContext
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        if (ActivityCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        // When the activity starts from notification, show the dialog.
        init(if (!intent.getBooleanExtra(Config.INTENT_FROM_NITIFICATION, false)) {
            true
        } else {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("停止Hook")
                .setMessage("您点击了通知，是否停止所有Hook")
                .setPositiveButton("确定") { _, _ ->
                    SharedPreferencesUtil.put(Config.SP_EX_APPS_TO_HOOK, SharedPreferencesUtil.getString(Config.SP_APPS_TO_HOOK))
                    .remove(Config.SP_APPS_TO_HOOK)
                    .apply()
                }.setNegativeButton("取消", null)
                .show()
            false
        })
    }



    private fun init(startService: Boolean) {
        btn_main_chooseapp.setOnClickListener(clickListener)
        btn_main_clearcache.setOnClickListener(clickListener)
        btn_main_viewRes.setOnClickListener(clickListener)
        btn_main_saveaddress.setOnClickListener(clickListener)
        switch_main_isdialingmod.setOnCheckedChangeListener { _, isChecked ->
            val modString = if(isChecked) "拨测模式" else "自动化检测模式"
            AlertDialog.Builder(this@MainActivity)
                .setTitle("切换模式")
                .setMessage("是否将模式切换到: $modString")
                .setPositiveButton("确定") { _, _ ->
                    SharedPreferencesUtil.put(Config.SP_IS_DIALING_MOD, isChecked).apply()
                    layout_main_server.visibility = if(isChecked) View.VISIBLE else View.INVISIBLE
                    stopService(Intent(this@MainActivity, MainService::class.java))
                    startMainService(isChecked, true)
                }.setNegativeButton("取消") { _, _ ->
                    switch_main_isdialingmod.isChecked = !isChecked
                }
                .show()
        }
        val dialingMod = SharedPreferencesUtil.getBoolean(Config.SP_IS_DIALING_MOD)
        switch_main_isdialingmod.isChecked = dialingMod
        layout_main_server.visibility = if(dialingMod) View.VISIBLE else View.INVISIBLE
        startMainService(dialingMod, startService)
    }

    private fun startMainService(dialingMod : Boolean, startService: Boolean) {
        val intent = Intent(this@MainActivity, MainService::class.java)
        // 拨测
        if(dialingMod) {
            serverAddress = SharedPreferencesUtil.getString(Config.SP_SERVER_ADDRESS)
            if (serverAddress != "" && startService) {
                et_main_ip.hint = serverAddress.split(":")[0]
                et_main_port.hint = serverAddress.split(":")[1]
            }
            intent.putExtra(Config.INTENT_SERVER_ADDRESS, serverAddress)
            intent.putExtra(Config.INTENT_DIALING_MOD, true)
        } else {
            intent.putExtra(Config.INTENT_DIALING_MOD, false)
        }
        startService(intent)
    }

}