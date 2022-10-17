package com.example.customalarm

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import com.example.customalarm.common.EditMode.Companion.CREATE_MODE
import com.example.customalarm.common.EditMode.Companion.EDIT_MODE
import com.example.customalarm.data.db.AlarmSettingDao
import com.example.customalarm.data.db.AppDatabase
import com.example.customalarm.data.entity.AlarmSettingEntity

class InputActivity : AppCompatActivity() {

    private lateinit var alarmSettingDao: AlarmSettingDao

    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker
    private lateinit var cancelButton: Button
    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        alarmSettingDao = AppDatabase.getDatabase(applicationContext).alarmSettingDao()

        settingTimeDrum()
        settingOperationButton()

        when (intent.getIntExtra("editMode", -1)) {
            CREATE_MODE -> { /** do nothing */ }
            EDIT_MODE -> { setupEditMode() }
            else -> { /** do nothing */ }
        }
    }

    private fun settingTimeDrum() {
        // NumberPickerを取得
        hourPicker = findViewById(R.id.hourPicker)
        minutePicker = findViewById(R.id.minutePicker)

        // 配列のインデックス最小、最大を指定
        hourPicker.minValue = 0
        hourPicker.maxValue = 23

        // ドラムロール表示用の配列作成
        val minutes = arrayOf("0", "15", "30", "45")
        // 配列のインデックス最小、最大を指定
        minutePicker.minValue = 0
        minutePicker.maxValue = minutes.size - 1
        // NumberPickerに配列をセットする
        minutePicker.displayedValues = minutes
    }

    private fun settingOperationButton() {
        cancelButton = findViewById(R.id.cancelButton)
        addButton = findViewById(R.id.addButton)

        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        addButton.setOnClickListener {
            val hour = hourPicker.value
            val minute = minutePicker.value
            val editAlarmTitle = findViewById<EditText>(R.id.editAlarmTitle).text.toString()

            AsyncSave(alarmSettingDao, AlarmSettingEntity(0, editAlarmTitle)).execute()
            setResult(RESULT_OK, Intent())
            finish()
        }
    }

    private fun setupEditMode() {
        val alarmId = intent.getIntExtra("alarmId", -1)

        AsyncLoad(alarmSettingDao, alarmId, findViewById(R.id.editAlarmTitle)).execute()
    }

    private class AsyncLoad(private val alarmSettingDao: AlarmSettingDao, private val alarmId: Int, private val editAlarmTitle: EditText) : AsyncTask<Void, Void, AlarmSettingEntity>() {

        override fun doInBackground(vararg voids: Void): AlarmSettingEntity {
            return alarmSettingDao.selectById(alarmId)
        }

        override fun onPostExecute(alarmSettingEntity: AlarmSettingEntity) {
            editAlarmTitle.setText(alarmSettingEntity.title)
        }
    }

    private class AsyncSave(private val alarmSettingDao: AlarmSettingDao, private val entity: AlarmSettingEntity) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg voids: Void): Void? {
            Log.d("debug", "background")
            alarmSettingDao.saveAlarmSetting(entity)
            return null
        }
    }

}
