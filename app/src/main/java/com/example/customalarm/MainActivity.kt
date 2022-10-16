package com.example.customalarm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customalarm.entity.AlarmSetting
import com.example.customalarm.listcomponent.ListAdapter
import com.example.customalarm.util.DatabaseHelper
import com.example.customalarm.util.DatabaseHelper.Companion.TABLE_NAME
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var helper: DatabaseHelper
    private lateinit var alarmSettingList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        helper = DatabaseHelper.getInstance(this)
        alarmSettingList = generateAlarmSettingList()

        // フローティングアクションボタンの設定
        val addBtn = findViewById<FloatingActionButton>(R.id.addBtn)
        addBtn.setOnClickListener {
            val i = Intent(this, InputActivity::class.java)
            startActivityForResult(i, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("debug", "onActivityResult")

        val dataAlarms = loadAlarms()
        Log.d("debug", dataAlarms.toString())
        this.updateAlarmSettingList(dataAlarms)
    }

    private fun generateAlarmSettingList(): RecyclerView {
        val alarmSettingList: RecyclerView = findViewById(R.id.alarmSetting)
        alarmSettingList.setHasFixedSize(true)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        alarmSettingList.layoutManager = manager

        return alarmSettingList
    }

    private fun setAlarmSettingList(data: ArrayList<AlarmSetting>) {
        alarmSettingList.adapter = ListAdapter(data)
    }

    private fun loadAlarms(): ArrayList<AlarmSetting> {
        val data = ArrayList<AlarmSetting>()

        helper.readableDatabase.use { db ->
            val cols = arrayOf("id", "title")
            val cs = db.query(
                TABLE_NAME, cols, null, null,
                null, null, "id", null
            )
            var eol = cs.moveToFirst()
            while (eol) {
                val item = AlarmSetting()
                item.setId(cs.getInt(0))
                item.setTitle(cs.getString(1))
                data.add(item)
                eol = cs.moveToNext()
            }
        }
        return data
    }

    private fun updateAlarmSettingList(data: ArrayList<AlarmSetting>) {
        this.setAlarmSettingList(data)
    }
}
