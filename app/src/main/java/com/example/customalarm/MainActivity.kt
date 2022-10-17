package com.example.customalarm

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customalarm.common.EditMode
import com.example.customalarm.data.db.AlarmSettingDao
import com.example.customalarm.data.db.AppDatabase
import com.example.customalarm.data.entity.AlarmSettingEntity
import com.example.customalarm.listcomponent.ListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var alarmSettingDao: AlarmSettingDao
    private lateinit var alarmSettingList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmSettingDao = AppDatabase.getDatabase(applicationContext).alarmSettingDao()
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
        AsyncLoad(alarmSettingDao, alarmSettingList).execute()
    }

    private class AsyncLoad(private val alarmSettingDao: AlarmSettingDao, private val alarmSettingList: RecyclerView) : AsyncTask<Void, Void, List<AlarmSettingEntity>>() {

        override fun doInBackground(vararg voids: Void): List<AlarmSettingEntity> {
            return alarmSettingDao.selectAll()
        }

        override fun onPostExecute(dataAlarms: List<AlarmSettingEntity>) {
            alarmSettingList.adapter = ListAdapter(dataAlarms)
        }
    }

}
