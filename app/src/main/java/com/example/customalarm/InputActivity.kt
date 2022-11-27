package com.example.customalarm

import android.content.Intent
import android.os.Bundle
import android.view.View.*
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.customalarm.calendar.CalendarDecorator
import com.example.customalarm.calendar.HolidayDecorator
import com.example.customalarm.calendar.logic.*
import com.example.customalarm.calendar.logic.dto.MonthlyDay
import com.example.customalarm.common.Constant.Companion.MAX_END_OF_MONTH
import com.example.customalarm.common.Constant.Companion.MIN_END_OF_MONTH
import com.example.customalarm.common.EditMode.Companion.CREATE_MODE
import com.example.customalarm.common.EditMode.Companion.EDIT_MODE
import com.example.customalarm.common.Setting.Companion.TIME_PITCH
import com.example.customalarm.common.data.CalendarHelper
import com.example.customalarm.data.db.AlarmSettingDao
import com.example.customalarm.data.db.AppDatabase
import com.example.customalarm.data.db.HolidayDao
import com.example.customalarm.data.entity.AlarmSettingEntity
import com.example.customalarm.data.entity.HolidayEntity
import com.example.customalarm.dialog.*
import com.example.customalarm.dialog.list.EndOfMonth
import com.example.customalarm.dialog.list.RepeatUnit
import com.example.customalarm.dialog.list.RepeatUnit.*
import com.example.customalarm.util.Util
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.*
import org.threeten.bp.LocalDateTime

class InputActivity : AppCompatActivity() {

    private lateinit var alarmSettingDao: AlarmSettingDao
    private lateinit var holidayDao: HolidayDao

    private var currentDecorator: CalendarDecorator? = null

    private lateinit var entity: AlarmSettingEntity

    private lateinit var holidays: List<HolidayEntity>

    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker
    private lateinit var alarmRepeat: TextView
    private lateinit var calendar: MaterialCalendarView
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        alarmSettingDao = AppDatabase.getDatabase(applicationContext).alarmSettingDao()
        holidayDao = AppDatabase.getDatabase(applicationContext).holidayDao()

        val mode = intent.getIntExtra("editMode", -1)
        when (mode) {
            CREATE_MODE -> { setupCreateMode() }
            EDIT_MODE -> { setupEditMode() }
            else -> { /** do nothing */ }
        }

        settingTimeDrum()
        settingInputs()
        settingOperationButton(mode)
        settingCalendar()
        refresh()
    }

    private fun settingCalendar() {
        calendar = findViewById(R.id.calendar)
//        calendar.firstDayOfWeek = FIRST_DAY_OF_WEEK
        val calendarDay = CalendarHelper.toCalendarDay(entity.startDate)
        calendar.selectRange(calendarDay, calendarDay)

        calendar.setOnDateChangedListener { _, date, _ ->
            entity.startDate = CalendarHelper.toLocalDate(date)
            refreshTargetDate()
        }

        runBlocking {
            holidays = holidayDao.selectAll()
            calendar.addDecorator(HolidayDecorator(resources, holidays))
        }
    }

    private fun settingTimeDrum() {
        // NumberPickerを取得
        hourPicker = findViewById(R.id.hourPicker)
        minutePicker = findViewById(R.id.minutePicker)

        // 配列のインデックス最小、最大を指定
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        hourPicker.value = entity.time.hour

        hourPicker.setOnScrollListener { _, scrollState ->
            if (SCROLL_STATE_IDLE == scrollState) {
                // スクロール後、カレンダー上の通知日を更新する
                entity.time = entity.time.withHour(hourPicker.value)
                refreshTargetDate()
            }
        }

        // ドラムロール表示用の配列作成
        val minutes = Array(60 / TIME_PITCH) {
            (it * TIME_PITCH).toString().padStart(2, '0')
        }
        // 配列のインデックス最小、最大を指定
        minutePicker.minValue = 0
        minutePicker.maxValue = minutes.size - 1
        // NumberPickerに配列をセットする
        minutePicker.displayedValues = minutes
        minutePicker.value = entity.time.minute / TIME_PITCH

        minutePicker.setOnScrollListener { _, scrollState ->
            if (SCROLL_STATE_IDLE == scrollState) {
                // スクロール後、カレンダー上の通知日を更新する
                entity.time = entity.time.withMinute(minutePicker.value * TIME_PITCH)
                refreshTargetDate()
            }
        }
    }

    private fun settingInputs() {
        alarmRepeat = findViewById(R.id.alarmRepeat)

        alarmRepeat.setOnClickListener {
            ListSelectDialogFragment("繰り返し設定", RepeatUnit.values())
                .onSubmit { unit ->
                    when (unit) {
                        NO_REPEAT -> {
                            setTargetDateIdentifier(NoRepeatIdentifierGenerator())
                        }
                        DAILY -> {
                            DailyRepeatDialogFragment(unit.text)
                                .onSubmit { pitch ->
                                    setTargetDateIdentifier(DailyIdentifierGenerator(pitch))
                                }
                                .execute(supportFragmentManager)
                        }
                        WEEKLY -> {
                            // 曜日の選択肢と、◯週ごとに繰り返す、の入力があれば、毎週でも隔週でも指定可能。
                            // 従って、フォームは１種類で良い。
                            WeeklyRepeatDialogFragment(unit.text)
                                .onSubmit { pair ->
                                    setTargetDateIdentifier(WeeklyIdentifierGenerator(pair.first, pair.second))
                                }
                                .execute(supportFragmentManager)
                        }
                        MONTHLY_DAY -> {
                            MultiChoiceDialogFragment(unit.text, ((1..31) + (-3..-1)).map {
                                MonthlyDay(it)
                            }
                                .toTypedArray()
                            )
                                .onSubmit { list ->
                                    val firstAfter29th = list.find { it.dayOfMonth > MIN_END_OF_MONTH }
                                    if (firstAfter29th != null) {
                                        val buff = "${firstAfter29th.text}${if (firstAfter29th.dayOfMonth == MAX_END_OF_MONTH) "" else "以降"}"
                                        val title = "月末${buff}がない場合の設定"
                                        SingleChoiceDialogFragment(title, EndOfMonth.values())
                                            .onSubmit { endOfMonth ->
                                                setTargetDateIdentifier(MonthlyDayIdentifierGenerator(list, endOfMonth))
                                            }
                                            .execute(supportFragmentManager)
                                    } else {
                                        setTargetDateIdentifier(MonthlyDayIdentifierGenerator(list))
                                    }
                                }
                                .execute(supportFragmentManager)
                        }
                        MONTHLY_NTH_DAY -> {
                            MonthlyWeekRepeatDialogFragment(unit.text)
                                .onSubmit { pair ->
                                    setTargetDateIdentifier(MonthlyWeekIdentifierGenerator(pair.first, pair.second))
                                }
                                .execute(supportFragmentManager)
                        }
//                        YEARLY -> { /* TODO */ }
                    }
                }
                .execute(supportFragmentManager)
        }
    }

    private fun settingOperationButton(mode: Int) {
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)

        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        saveButton.setOnClickListener {
            entity.title = findViewById<EditText>(R.id.editAlarmTitle).text.toString()

            runBlocking {
                saveAlarmSetting(entity)
                Util.scheduleAlarm(applicationContext, entity)
            }
            setResult(RESULT_OK, Intent())
            finish()
        }

        if (mode == CREATE_MODE) {
            deleteButton.visibility = GONE
        } else {
            deleteButton.setOnClickListener {
                runBlocking {
                    alarmSettingDao.removeAlarmSetting(entity)
                    // TODO 設定済みの通知をキャンセルする
                }
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun setupCreateMode() {
        val now = LocalDateTime.now()
        entity = AlarmSettingEntity(0, "", now.toLocalDate(), now.toLocalTime(), NoRepeatIdentifierGenerator())
    }

    private fun setupEditMode() {
        val alarmId = intent.getLongExtra("alarmId", 0)
        runBlocking {
            val alarmSettingEntity = alarmSettingDao.selectById(alarmId)

            findViewById<EditText>(R.id.editAlarmTitle).setText(alarmSettingEntity.title)
            entity = alarmSettingEntity
        }
    }

    private fun setTargetDateIdentifier(generator: CalendarTargetIdentifierGenerator) {
        entity.generator = generator
        refresh()
    }

    private fun refresh() {
        refreshTargetDate()
        refreshRepeatText()
    }

    private fun refreshTargetDate() {
        if (currentDecorator != null) calendar.removeDecorator(currentDecorator)
        currentDecorator = CalendarDecorator(resources, entity.startDate.atTime(entity.time), entity.generator)
        calendar.addDecorator(currentDecorator)
    }

    private fun refreshRepeatText() {
        alarmRepeat.text = entity.generator.text()
    }

    private suspend fun saveAlarmSetting(entity: AlarmSettingEntity) {
        try {
            val id = alarmSettingDao.saveAlarmSetting(entity)
            entity.id = id
        } catch (e: Exception) {
            // Do nothing
        }
    }
}
