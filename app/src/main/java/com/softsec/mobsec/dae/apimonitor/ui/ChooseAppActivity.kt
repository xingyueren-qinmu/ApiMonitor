package com.softsec.mobsec.dae.apimonitor.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.softsec.mobsec.dae.apimonitor.R
import com.softsec.mobsec.dae.apimonitor.util.AppInfo
import com.softsec.mobsec.dae.apimonitor.util.Config
import com.softsec.mobsec.dae.apimonitor.util.SharedPreferencesUtil
import com.softsec.mobsec.dae.apimonitor.util.Util
import kotlinx.android.synthetic.main.activity_chooseapp.*
import java.io.File
import java.lang.StringBuilder
import java.util.Collections.sort

class ChooseAppActivity : AppCompatActivity() {

    private var isClosing : Boolean = false
    private lateinit var adapter : ChooseAppListAdapter
    private val appsToMonitorSet = mutableSetOf<String>()
    private lateinit var appList : List<AppInfo>
    private val removedApp = mutableSetOf<String>()


    // Todo 清除sp中的冗余项
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesUtil.context = applicationContext
        setContentView(R.layout.activity_chooseapp)
        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val tempStr  = SharedPreferencesUtil.getString(Config.SP_APPS_TO_HOOK)
        if(tempStr != "")
            for(pkgNme in tempStr.split(";")) appsToMonitorSet.add(pkgNme)
        initView()
    }

    @SuppressLint("CommitPrefEdits")
    private fun initView() {
        Thread {
            appList = queryFilterAppInfo()
            runOnUiThread {
                adapter = ChooseAppListAdapter(this@ChooseAppActivity, appList)
                lv_chooseapp_list.adapter = adapter
            }
        }.start()


        btn_chooseapp_save.setOnClickListener {
            val sb = StringBuilder()
            for (pkgName in appsToMonitorSet) {
                if (pkgName == "") continue
                // Check write permission and save log path.
                var canWrite = false
                try {
                    val perms = packageManager.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS).requestedPermissions
                    for (perm in perms)
                        if (perm.contains("android.permission.WRITE_EXTERNAL_STORAGE")) {
                            canWrite = true
                            break
                        }
                } catch(e: PackageManager.NameNotFoundException) {
                    removedApp.add(pkgName)
                    continue
                }
                sb.append(pkgName).append(";")
                val writePerm = canWrite && Build.VERSION.SDK_INT < 23
                val logDir = if (writePerm)
                    Config.PATH_TESTING_LOG
                else
                    packageManager.getPackageInfo(pkgName, 0).applicationInfo.dataDir + Config.PATH_TARGET_APP_LOG
                val logFile = File(logDir + pkgName)
                if (logFile.exists()) {
                    logFile.delete()
                    logFile.parentFile.delete()
                }
                SharedPreferencesUtil.addAppToHook(pkgName, writePerm, logDir,
                    packageManager.getPackageInfo(pkgName, 0).applicationInfo.dataDir)
                Log.i("logPath", logDir + pkgName)
            }
            SharedPreferencesUtil.remove(Config.SP_APPS_TO_HOOK)
            SharedPreferencesUtil.put(Config.SP_APPS_TO_HOOK, sb.toString())
            sb.clear()
            for (pkgName in removedApp){
                sb.append(pkgName).append(";")
            }
            SharedPreferencesUtil.put(Config.SP_EX_APPS_TO_HOOK, sb.toString())
            Toast.makeText(this@ChooseAppActivity, "已保存", Toast.LENGTH_SHORT).show()

            // 关闭所有被检应用
            for(pkgName in appsToMonitorSet) {
                if(pkgName != "") {
                    Util.execRootCmd("am force-stop $pkgName")
                }
            }
            onBackPressed()
        }

        btn_chooseapp_cancel.setOnClickListener {
            println("cancel")
        }

        lv_chooseapp_list.setOnItemClickListener { adapterView, view, position, l ->
            val cb : CheckBox = view.findViewById(R.id.cb_appchosen)
            val appInfo = appList[position]
            cb.isChecked = !cb.isChecked
            appList[position].isChecked = cb.isChecked
            if (cb.isChecked && !appsToMonitorSet.contains(appInfo.pkgName)){
                appsToMonitorSet.add(appInfo.pkgName)
                if(removedApp.contains(appInfo.pkgName))
                    removedApp.remove(appInfo.pkgName)
            } else {
                if (!cb.isChecked && appsToMonitorSet.contains(appInfo.pkgName)) {
                    appsToMonitorSet.remove(appInfo.pkgName)
                    removedApp.add(appInfo.pkgName)
                }
            }
        }

        cb_chooseapp_chooseall.visibility = View.INVISIBLE
    }

    private fun queryFilterAppInfo() : List<AppInfo> {
        // 加一个转圈圈
        val listApplications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            packageManager.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES)
        } else {
            packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)
        }

        sort(listApplications, ApplicationInfo.DisplayNameComparator(packageManager))
        val appInfoList: MutableList<AppInfo> = mutableListOf()
        for (applicationInfo in listApplications) {
            val appInfo = AppInfo(
                appName = applicationInfo.loadLabel(packageManager) as String,
                pkgName =  applicationInfo.packageName,
                appIcon = applicationInfo.loadIcon(packageManager),
                isChecked = appsToMonitorSet.contains(applicationInfo.packageName),
                firstInstallTime = packageManager.getPackageInfo(applicationInfo.packageName, 0).firstInstallTime
            )
            appInfoList.add(appInfo)
            appInfoList.sortByDescending { it.firstInstallTime }
        }
        return appInfoList
    }

    inner class ChooseAppListAdapter(context: Context, apps: List<AppInfo>): BaseAdapter() {

        private var appList : List<AppInfo> = apps
        private var inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view : View
            val holder : ViewHolder
            if (isClosing || convertView == null || convertView.tag == null) {
                view = inflater.inflate(R.layout.item_chooseapp, parent, false)
                holder = ViewHolder(view)
                view.tag = holder
            } else {
                view = convertView
                holder = convertView.tag as ViewHolder
            }
            val appInfo = getItem(position) as AppInfo
            holder.imgAppIcon.setImageDrawable(appInfo.appIcon)
            holder.tvAppName.text = appInfo.appName
            holder.tvPkgName.text = appInfo.pkgName
            holder.cbAppChosen.isChecked = (appInfo.isChecked)
            holder.cbAppChosen.visibility = view.visibility
            return view
        }

        override fun getItem(position: Int): Any {
            return appList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return appList.size
        }

    }

    inner class ViewHolder(view: View) {
        var imgAppIcon: ImageView = view.findViewById(R.id.img_appicon)
        var tvAppName: TextView = view.findViewById(R.id.tv_appname)
        var tvPkgName: TextView = view.findViewById(R.id.tv_pkgkname)
        var cbAppChosen: CheckBox = view.findViewById(R.id.cb_appchosen)
    }
}