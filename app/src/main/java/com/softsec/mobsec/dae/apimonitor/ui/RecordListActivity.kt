package com.softsec.mobsec.dae.apimonitor.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.softsec.mobsec.dae.apimonitor.R
import com.softsec.mobsec.dae.apimonitor.service.MainService
import com.softsec.mobsec.dae.apimonitor.util.Config
import com.softsec.mobsec.dae.apimonitor.util.RecordInfo
import com.softsec.mobsec.dae.apimonitor.util.SharedPreferencesUtil
import kotlinx.android.synthetic.main.activity_list_record.*
import java.io.File
import java.io.FileReader
import java.io.LineNumberReader

class RecordListActivity : AppCompatActivity() {

    private lateinit var mainServiceBinder : MainService.MainServiceBinder

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, serviceBinder: IBinder?) {
            mainServiceBinder = serviceBinder as MainService.MainServiceBinder
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesUtil.context = applicationContext
        setContentView(R.layout.activity_list_record)
        bindService(Intent(this@RecordListActivity, MainService::class.java), connection, Context.BIND_AUTO_CREATE)
        initViews()
    }

    @SuppressLint("InflateParams")
    private fun initViews() {
        val viewList = ArrayList<View>()
        val viewTesting = layoutInflater.inflate(R.layout.listview_result, null)
        val viewHistory = layoutInflater.inflate(R.layout.listview_result, null)

        viewList.add(viewTesting)
        viewList.add(viewHistory)

        val testingRecordInfos = refreshViewList(Config.PATH_TESTING_LOG)
        val historyRecordInfos = refreshViewList(Config.PATH_HISTORY_LOG)


        // Init viewpager
        vp_record_listviews.adapter = object : PagerAdapter() {
            override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
                return view == `object`
            }

            override fun getCount(): Int {
                return viewList.size
            }

            override fun instantiateItem(container: ViewGroup?, position: Int): Any {
                vp_record_listviews.addView(viewList[position])
                Log.i("pv位置", "$position")
                return viewList[position]
            }

            override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
                vp_record_listviews.removeView(viewList[position])
            }
        }

        // 2 tvs combine with viewpager
        tv_record_testing.setOnClickListener { vp_record_listviews.currentItem = 0 }
        tv_record_history.setOnClickListener { vp_record_listviews.currentItem = 1 }

        vp_record_listviews.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        tv_record_testing.setBackgroundColor(Color.LTGRAY)
                        tv_record_history.setBackgroundColor(Color.WHITE)
                        refreshViewList(Config.PATH_TESTING_LOG)
                    }
                    1 -> {
                        tv_record_testing.setBackgroundColor(Color.WHITE)
                        tv_record_history.setBackgroundColor(Color.LTGRAY)
                        refreshViewList(Config.PATH_HISTORY_LOG)
                    }
                }
            }

        })

        // Init listviews
        val lvTesting = viewTesting.findViewById(R.id.lv_result) as ListView
        lvTesting.adapter = RecordListAdapter(this@RecordListActivity, testingRecordInfos)
        // short click to view record detail
        lvTesting.setOnItemClickListener { _, _, position, _ ->
            val logFile = File(testingRecordInfos[position].logAbsolutePath)
            val lnr = LineNumberReader(FileReader(logFile))
            lnr.skip(logFile.length())
            val lineNumber = lnr.lineNumber
            AlertDialog.Builder(this@RecordListActivity)
                .setTitle("选择操作")
                .setMessage("当前记录长度：$lineNumber\n")
                .setPositiveButton("删除") { _, _ ->
                    File(testingRecordInfos[position].logAbsolutePath).delete()
                    testingRecordInfos.removeAt(position)
                    lvTesting.adapter = RecordListAdapter(this@RecordListActivity, testingRecordInfos)
                }.setNeutralButton("结束Hook") { _, _ ->
                    SharedPreferencesUtil.removeAppToHook(testingRecordInfos[position].pkgName)
                    testingRecordInfos.removeAt(position)
                    lvTesting.adapter = RecordListAdapter(this@RecordListActivity, testingRecordInfos)
                }.setNegativeButton("取消", null)
                .show()
        }

        val lvHistory = viewHistory.findViewById(R.id.lv_result) as ListView
        lvHistory.adapter = RecordListAdapter(this@RecordListActivity, historyRecordInfos)
        lvHistory.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this@RecordListActivity, RecordContentActivity::class.java)
            intent.putExtra(Config.INTENT_RESULTCONTENT_LOGPATH, historyRecordInfos[position].logAbsolutePath)
            intent.putExtra(Config.INTENT_APP_NAME, historyRecordInfos[position].appName)
            startActivity(intent)
        }
        lvHistory.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(this@RecordListActivity)
                .setTitle("上传与删除")
                .setMessage("请选择操作")
                .setPositiveButton("上传记录") { _, _ ->
                    mainServiceBinder.uploadRecord(historyRecordInfos[position].logAbsolutePath)
                }.setNeutralButton("删除") { _, _ ->
                    File(historyRecordInfos[position].logAbsolutePath).delete()
                    historyRecordInfos.removeAt(position)
                    lvHistory.adapter = RecordListAdapter(this@RecordListActivity, historyRecordInfos)
                }.setNegativeButton("取消", null)
                .show()
            true
        }
    }

    private fun refreshViewList(recordFolderPath: String) : ArrayList<RecordInfo> {
        val recordFolder = File(recordFolderPath)
        val recordInfos = ArrayList<RecordInfo>()
        recordFolder.walk().filter { it.isFile }
            .forEach { recordInfos.add(getRecordInfo(it)) }
        recordInfos.sortByDescending { it.logDate }
        return recordInfos
    }

    private fun getRecordInfo(logFile: File): RecordInfo {
        val isTesting : Boolean
        var date = ""
        val pkgName =
            if (logFile.name.contains("--")) {
                isTesting = false
                date = logFile.name.split("--")[0]
                logFile.name.split("--")[1]
            } else {
                isTesting = true
                logFile.name
            }
        try {
            val ai = packageManager.getPackageInfo(pkgName, 0).applicationInfo
            return RecordInfo(
                ai.loadLabel(packageManager) as String,
                pkgName,
                ai.loadIcon(packageManager),
                logFile.absolutePath,
                isTesting,
                date
            )
        } catch (e : PackageManager.NameNotFoundException) {
            return RecordInfo(
                "",
                pkgName,
                ResourcesCompat.getDrawable(resources, R.drawable.android, null)!!,
                logFile.absolutePath,
                isTesting,
                date
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    inner class RecordListAdapter(
        val context: Context,
        private val recordList: ArrayList<RecordInfo>
    ): BaseAdapter() {

        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val holder: ViewHolder
            if (convertView == null || convertView.tag == null) {
                view = inflater.inflate(R.layout.item_result, parent, false)
                holder = ViewHolder(view)
                view.tag = holder
            } else {
                view = convertView
                holder = convertView.tag as ViewHolder
            }
            val resultInfo = getItem(position) as RecordInfo
            holder.imgAppIcon.setImageDrawable(resultInfo.appIcon)
            holder.tvAppName.text = resultInfo.appName
            holder.tvPkgName.text = resultInfo.pkgName
            holder.tvTime.text = resultInfo.logDate
            return view
        }

        override fun getItem(position: Int): Any {
            return recordList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return recordList.size
        }
    }

    inner class ViewHolder(view: View) {
        var imgAppIcon: ImageView = view.findViewById(R.id.img_resultitem_applogo)
        var tvAppName: TextView = view.findViewById(R.id.tv_resultitem_applabel)
        var tvPkgName: TextView = view.findViewById(R.id.tv_resultitem_apppkgname)
        var tvTime: TextView = view.findViewById(R.id.tv_resultitem_time)
    }
}