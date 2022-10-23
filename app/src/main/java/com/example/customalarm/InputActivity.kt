package com.example.customalarm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.customalarm.common.EditMode.Companion.CREATE_MODE
import com.example.customalarm.common.EditMode.Companion.EDIT_MODE
import com.example.customalarm.common.Setting
import com.example.customalarm.data.db.AlarmSettingDao
import com.example.customalarm.data.db.AppDatabase
import com.example.customalarm.data.entity.AlarmSettingEntity
import com.example.customalarm.dialog.ListSelectDialogFragment
import com.example.customalarm.dialog.WeeklyRepeatDialogFragment
import com.example.customalarm.dialog.list.RepeatUnit
import com.example.customalarm.dialog.list.RepeatUnit.*
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
    private lateinit var alarmRepeat: TextView
    private lateinit var cancelButton: Button
    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        alarmSettingDao = AppDatabase.getDatabase(applicationContext).alarmSettingDao()

        val editMode = intent.getIntExtra("editMode", -1)
        val alarmId = intent.getIntExtra("alarmId", 0)

        settingTimeDrum()
        settingInputs()
        settingOperationButton(alarmId)

        // editModeがなくても、alarmIdの有無で判定しても同じ。
        when (editMode) {
            CREATE_MODE -> { /** do nothing */ }
            EDIT_MODE -> { setupEditMode(alarmId) }
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
        val minutes = Array(60 / Setting.TIME_PITCH) {
            (it * Setting.TIME_PITCH).toString().padStart(2, '0')
        }
        // 配列のインデックス最小、最大を指定
        minutePicker.minValue = 0
        minutePicker.maxValue = minutes.size - 1
        // NumberPickerに配列をセットする
        minutePicker.displayedValues = minutes
    }

    private fun settingInputs() {
        alarmRepeat = findViewById(R.id.alarmRepeat)

        alarmRepeat.setOnClickListener {
            ListSelectDialogFragment("繰り返し設定", RepeatUnit.values())
                .onSelected { unit ->
                    when (unit) {
                        NO_REPEAT -> { /* TODO */ }
                        DAILY -> { /* TODO */ }
                        WEEKLY -> {
                            // 曜日の選択肢と、◯週ごとに繰り返す、の入力があれば、毎週でも隔週でも指定可能。
                            // 従って、フォームは１種類で良い。
                            WeeklyRepeatDialogFragment("曜日指定")
                                .onSubmit { /* TODO */ }
                                .show(supportFragmentManager, "曜日指定")
                        }
                        MONTHLY -> { /* TODO */ }
                        YEARLY -> { /* TODO */ }
                    }
                }
                .show(supportFragmentManager, "繰り返し設定")
        }
    }

    private fun settingOperationButton(alarmId: Int) {
        cancelButton = findViewById(R.id.cancelButton)
        addButton = findViewById(R.id.addButton)

        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        addButton.setOnClickListener {
            val hour = hourPicker.value
            val minute = minutePicker.value * Setting.TIME_PITCH
            val time = "$hour:$minute"
            val editAlarmTitle = findViewById<EditText>(R.id.editAlarmTitle).text.toString()

            val entity = AlarmSettingEntity(alarmId, editAlarmTitle, time)
            scope.launch {
                saveAlarmSetting(entity)
            }
            Util.scheduleAlarm(applicationContext, entity)
            setResult(RESULT_OK, Intent())
            finish()
        }
    }

    private fun setupEditMode(alarmId: Int) {
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
