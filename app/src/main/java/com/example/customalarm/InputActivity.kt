package com.example.customalarm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.customalarm.calendar.CalendarDecorator
import com.example.customalarm.calendar.CalendarListener
import com.example.customalarm.calendar.CalendarTargetIdentifier
import com.example.customalarm.common.Constant.Companion.MAX_END_OF_MONTH
import com.example.customalarm.common.Constant.Companion.MIN_END_OF_MONTH
import com.example.customalarm.common.EditMode.Companion.CREATE_MODE
import com.example.customalarm.common.EditMode.Companion.EDIT_MODE
import com.example.customalarm.common.Setting
import com.example.customalarm.data.db.AlarmSettingDao
import com.example.customalarm.data.db.AppDatabase
import com.example.customalarm.data.entity.AlarmSettingEntity
import com.example.customalarm.dialog.*
import com.example.customalarm.dialog.list.EndOfMonth
import com.example.customalarm.dialog.list.ListOption
import com.example.customalarm.dialog.list.RepeatUnit
import com.example.customalarm.dialog.list.RepeatUnit.*
import com.example.customalarm.util.Util
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
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
    private lateinit var calendar: MaterialCalendarView
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        alarmSettingDao = AppDatabase.getDatabase(applicationContext).alarmSettingDao()

        val editMode = intent.getIntExtra("editMode", -1)
        val alarmId = intent.getIntExtra("alarmId", 0)

        settingTimeDrum()
        settingInputs()
        settingOperationButton(alarmId)
        settingCalendar()

        // editModeがなくても、alarmIdの有無で判定しても同じ。
        when (editMode) {
            CREATE_MODE -> { /** do nothing */ }
            EDIT_MODE -> { setupEditMode(alarmId) }
            else -> { /** do nothing */ }
        }
    }

    private fun settingCalendar() {
        calendar = findViewById(R.id.calendar)
//        calendar.firstDayOfWeek = FIRST_DAY_OF_WEEK
        calendar.selectRange(CalendarDay.today(), CalendarDay.today())

        val listener = CalendarListener()
        calendar.setOnDateLongClickListener(listener)
        calendar.setOnDateChangedListener(listener)
        calendar.setOnMonthChangedListener(listener)
        // TODO 仮実装。偶数の日にデコレートする。
        setCalendarDecoration { it.day % 2 == 0 }
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
                .onSubmit { unit ->
                    when (unit) {
                        NO_REPEAT -> { setCalendarDecoration { /* TODO 仮実装。奇数の日にデコレートする。 */ it.day % 2 == 1 } }
                        DAILY -> {
                            DailyRepeatDialogFragment(unit.text)
                                .onSubmit { /* TODO */ }
                                .execute(supportFragmentManager)
                        }
                        WEEKLY -> {
                            // 曜日の選択肢と、◯週ごとに繰り返す、の入力があれば、毎週でも隔週でも指定可能。
                            // 従って、フォームは１種類で良い。
                            WeeklyRepeatDialogFragment(unit.text)
                                .onSubmit { /* TODO */ }
                                .execute(supportFragmentManager)
                        }
                        MONTHLY -> {
                            SingleChoiceDialogFragment(unit.text, arrayOf("日にちから設定", "週,曜日から設定")
                                .mapIndexed { i, it ->
                                    object : ListOption {
                                        val id = i
                                        override val text: String
                                            get() = it
                                    }
                                }
                                .toTypedArray()
                            )
                                .onSubmit { option ->
                                    when (option.id) {
                                        // 日にちから設定
                                        0 -> {
                                            MultiChoiceDialogFragment(option.text, ((1..31) + (-3..-1)).map {
                                                object : ListOption {
                                                    val date = it
                                                    override val text: String
                                                        get() = if (it > 0) "${date}日"
                                                        else when (it) {
                                                            -1 -> "最終日"
                                                            -2 -> "最終日の前日"
                                                            -3 -> "最終日の前々日"
                                                            else -> ""
                                                        }
                                                }
                                            }
                                                .toTypedArray()
                                            )
                                                .onSubmit { list ->
                                                    val firstAfter29th = list.find { it.date > MIN_END_OF_MONTH }
                                                    if (firstAfter29th != null) {
                                                        val buff = "${firstAfter29th.text}${if (firstAfter29th.date == MAX_END_OF_MONTH) "" else "以降"}"
                                                        val title = "月末${buff}がない場合の設定"
                                                        SingleChoiceDialogFragment(title, EndOfMonth.values())
                                                            .onSubmit { /* TODO */ }
                                                            .execute(supportFragmentManager)
                                                    } else {
                                                        /* TODO */
                                                    }
                                                }
                                                .execute(supportFragmentManager)
                                        }
                                        // 週,曜日から設定
                                        1 -> {
                                            MonthlyWeekRepeatDialogFragment(unit.text)
                                                .onSubmit { /* TODO */ }
                                                .execute(supportFragmentManager)
                                        }
                                    }
                                }
                                .execute(supportFragmentManager)
                        }
//                        YEARLY -> { /* TODO */ }
                    }
                }
                .execute(supportFragmentManager)
        }
    }

    private fun settingOperationButton(alarmId: Int) {
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)

        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        saveButton.setOnClickListener {
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

    private fun setCalendarDecoration(identifier: CalendarTargetIdentifier) {
        calendar.removeDecorators()
        calendar.addDecorator(CalendarDecorator(resources, identifier))
    }

    private suspend fun saveAlarmSetting(entity: AlarmSettingEntity) {
        try {
            alarmSettingDao.saveAlarmSetting(entity)
        } catch (e: Exception) {
            // Do nothing
        }
    }
}
