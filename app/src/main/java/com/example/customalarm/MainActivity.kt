package com.example.customalarm

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customalarm.common.EditMode
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
        updateAlarmSettingList()

        // フローティングアクションボタンの設定
        val addBtn = findViewById<FloatingActionButton>(R.id.addBtn)
        addBtn.setOnClickListener {
            val i = Intent(this, InputActivity::class.java)
            i.putExtra("editMode", EditMode.CREATE_MODE)
            this.startForResult.launch(i)
        }
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.resultCode == RESULT_OK) {
            this.updateAlarmSettingList()
        }
    }

    private fun generateAlarmSettingList(): RecyclerView {
        val alarmSettingList: RecyclerView = findViewById(R.id.alarmSetting)
        alarmSettingList.setHasFixedSize(true)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        alarmSettingList.layoutManager = manager

        return alarmSettingList
    }

    private fun updateAlarmSettingList() {
        val dataAlarms = loadAlarms()
        this.setAlarmSettingList(dataAlarms)
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
}
