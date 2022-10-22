package com.example.customalarm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import com.example.customalarm.common.EditMode.Companion.CREATE_MODE
import com.example.customalarm.common.EditMode.Companion.EDIT_MODE
import com.example.customalarm.data.db.AlarmSettingDao
import com.example.customalarm.data.db.AppDatabase
import com.example.customalarm.data.entity.AlarmSettingEntity
import com.example.customalarm.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InputActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.Default)

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

        // editModeがなくても、alarmIdの有無で判定しても同じ。
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
            val minute = minutePicker.value * 15
            val time = "$hour:$minute"
            val editAlarmTitle = findViewById<EditText>(R.id.editAlarmTitle).text.toString()

            var alarmId = -1
            when (intent.getIntExtra("editMode", -1)) {
                CREATE_MODE -> { alarmId = 0 }
                EDIT_MODE -> { alarmId = intent.getIntExtra("alarmId", -1) }
                else -> { /** do nothing */ }
            }
            val entity = AlarmSettingEntity(alarmId, editAlarmTitle, time)
            scope.launch {
                saveAlarmSetting(entity)
            }
            Util.scheduleAlarm(applicationContext, entity)
            setResult(RESULT_OK, Intent())
            finish()
        }
    }

    private fun setupEditMode() {
        val alarmId = intent.getIntExtra("alarmId", -1)

        scope.launch {
            showAlarmSetting(alarmId)
        }
    }

    private suspend fun showAlarmSetting(alarmId: Int) {
        try {
            val alarmSettingEntity = alarmSettingDao.selectById(alarmId)

            withContext(Dispatchers.Main) {
                hourPicker.value = alarmSettingEntity.time.split(":")[0].toInt()
                minutePicker.value = alarmSettingEntity.time.split(":")[1].toInt()
                findViewById<EditText>(R.id.editAlarmTitle).setText(alarmSettingEntity.title)
            }
        } catch (e: Exception) {
            // Do nothing
        }
    }

    private suspend fun saveAlarmSetting(entity: AlarmSettingEntity) {
        try {
            alarmSettingDao.saveAlarmSetting(entity)
        } catch (e: Exception) {
            // Do nothing
        }
    }

}
