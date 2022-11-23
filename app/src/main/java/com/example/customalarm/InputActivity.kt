package com.example.customalarm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.customalarm.calendar.CalendarDecorator
import com.example.customalarm.calendar.CalendarTargetIdentifier
import com.example.customalarm.calendar.CalendarTargetIdentifierGenerator
import com.example.customalarm.calendar.HolidayDecorator
import com.example.customalarm.calendar.logic.DailyIdentifier
import com.example.customalarm.calendar.logic.MonthlyDayIdentifier
import com.example.customalarm.calendar.logic.MonthlyWeekIdentifier
import com.example.customalarm.calendar.logic.WeeklyIdentifier
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
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class InputActivity : AppCompatActivity() {

    private lateinit var alarmSettingDao: AlarmSettingDao
    private lateinit var holidayDao: HolidayDao

    private lateinit var generator: CalendarTargetIdentifierGenerator
    private var currentDecorator: CalendarDecorator? = null
    private val defaultDecorateTarget = { targetDateTime: LocalDateTime ->
        val now = LocalDateTime.now()
        var targetDate = targetDateTime.toLocalDate()

        if (now.isAfter(targetDateTime))
            targetDate = targetDate.plusDays(1)

        CalendarTargetIdentifier { it.isEqual(targetDate) }
    }

    private lateinit var targetDate: LocalDate
    private lateinit var targetTime: LocalTime

    private lateinit var holidays: List<HolidayEntity>

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
        holidayDao = AppDatabase.getDatabase(applicationContext).holidayDao()

        val editMode = intent.getIntExtra("editMode", -1)
        val alarmId = intent.getLongExtra("alarmId", 0)
        // editModeがなくても、alarmIdの有無で判定しても同じ。
        when (editMode) {
            CREATE_MODE -> { setupCreateMode() }
            EDIT_MODE -> { setupEditMode(alarmId) }
            else -> { /** do nothing */ }
        }

        settingTimeDrum()
        settingInputs()
        settingOperationButton(alarmId)
        settingCalendar()
    }

    private fun settingCalendar() {
        calendar = findViewById(R.id.calendar)
//        calendar.firstDayOfWeek = FIRST_DAY_OF_WEEK
        val calendarDay = CalendarHelper.toCalendarDay(targetDate)
        calendar.selectRange(calendarDay, calendarDay)

        calendar.setOnDateChangedListener { _, date, _ ->
            targetDate = CalendarHelper.toLocalDate(date)
            refreshTargetDate()
        }

        runBlocking {
            holidays = holidayDao.selectAll()
            calendar.addDecorator(HolidayDecorator(resources, holidays))
        }

        setTargetDateIdentifier(defaultDecorateTarget)
    }

    private fun settingTimeDrum() {
        // NumberPickerを取得
        hourPicker = findViewById(R.id.hourPicker)
        minutePicker = findViewById(R.id.minutePicker)

        // 配列のインデックス最小、最大を指定
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        hourPicker.value = targetTime.hour

        hourPicker.setOnScrollListener { _, scrollState ->
            if (SCROLL_STATE_IDLE == scrollState) {
                // スクロール後、カレンダー上の通知日を更新する
                targetTime = targetTime.withHour(hourPicker.value)
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
        minutePicker.value = targetTime.minute / TIME_PITCH

        minutePicker.setOnScrollListener { _, scrollState ->
            if (SCROLL_STATE_IDLE == scrollState) {
                // スクロール後、カレンダー上の通知日を更新する
                targetTime = targetTime.withMinute(minutePicker.value * TIME_PITCH)
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
                            setTargetDateIdentifier(defaultDecorateTarget)
                        }
                        DAILY -> {
                            DailyRepeatDialogFragment(unit.text)
                                .onSubmit { pitch ->
                                    setTargetDateIdentifier { targetDateTime ->
                                        DailyIdentifier(targetDateTime, pitch)
                                    }
                                }
                                .execute(supportFragmentManager)
                        }
                        WEEKLY -> {
                            // 曜日の選択肢と、◯週ごとに繰り返す、の入力があれば、毎週でも隔週でも指定可能。
                            // 従って、フォームは１種類で良い。
                            WeeklyRepeatDialogFragment(unit.text)
                                .onSubmit { pair ->
                                    setTargetDateIdentifier { targetDateTime ->
                                        WeeklyIdentifier(targetDateTime, pair.first, pair.second)
                                    }
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
                                                setTargetDateIdentifier {
                                                    MonthlyDayIdentifier(list, endOfMonth)
                                                }
                                            }
                                            .execute(supportFragmentManager)
                                    } else {
                                        setTargetDateIdentifier {
                                            MonthlyDayIdentifier(list)
                                        }
                                    }
                                }
                                .execute(supportFragmentManager)
                        }
                        MONTHLY_NTH_DAY -> {
                            MonthlyWeekRepeatDialogFragment(unit.text)
                                .onSubmit { pair ->
                                    setTargetDateIdentifier {
                                        MonthlyWeekIdentifier(pair.first, pair.second)
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

    private fun settingOperationButton(alarmId: Long) {
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)

        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        saveButton.setOnClickListener {
            val editAlarmTitle = findViewById<EditText>(R.id.editAlarmTitle).text.toString()

            val entity = AlarmSettingEntity(alarmId, editAlarmTitle, targetDate, targetTime)
            runBlocking {
                saveAlarmSetting(entity)
                Util.scheduleAlarm(applicationContext, entity)
            }
            setResult(RESULT_OK, Intent())
            finish()
        }
    }

    private fun setupCreateMode() {
        val now = LocalDateTime.now()
        targetDate = now.toLocalDate()
        targetTime = now.toLocalTime()
    }

    private fun setupEditMode(alarmId: Long) {
        runBlocking {
            val alarmSettingEntity = alarmSettingDao.selectById(alarmId)

            findViewById<EditText>(R.id.editAlarmTitle).setText(alarmSettingEntity.title)
            targetDate = alarmSettingEntity.startDate
            targetTime = alarmSettingEntity.time
        }
    }

    private fun setTargetDateIdentifier(generator: CalendarTargetIdentifierGenerator) {
        this.generator = generator
        refreshTargetDate()
    }

    private fun refreshTargetDate() {
        if (currentDecorator != null) calendar.removeDecorator(currentDecorator)
        currentDecorator = CalendarDecorator(resources, targetDate.atTime(targetTime), generator)
        calendar.addDecorator(currentDecorator)
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
