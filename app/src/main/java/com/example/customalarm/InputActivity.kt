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
import com.example.customalarm.calendar.HolidayDecorator
import com.example.customalarm.common.Constant.Companion.DAY_IN_WEEK
import com.example.customalarm.common.Constant.Companion.MAX_END_OF_MONTH
import com.example.customalarm.common.Constant.Companion.MIN_END_OF_MONTH
import com.example.customalarm.common.EditMode.Companion.CREATE_MODE
import com.example.customalarm.common.EditMode.Companion.EDIT_MODE
import com.example.customalarm.common.Setting
import com.example.customalarm.data.db.AlarmSettingDao
import com.example.customalarm.data.db.AppDatabase
import com.example.customalarm.data.db.HolidayDao
import com.example.customalarm.data.entity.AlarmSettingEntity
import com.example.customalarm.data.entity.HolidayEntity
import com.example.customalarm.dialog.*
import com.example.customalarm.dialog.list.Day.*
import com.example.customalarm.dialog.list.EndOfMonth
import com.example.customalarm.dialog.list.EndOfMonth.*
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
import org.threeten.bp.DayOfWeek.*
import java.util.*

class InputActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var alarmSettingDao: AlarmSettingDao
    private lateinit var holidayDao: HolidayDao

    // TODO 仮実装。偶数の日にデコレートする。
    private val defaultDecorateTarget = CalendarTargetIdentifier { it.day % 2 == 0 }

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
        val alarmId = intent.getIntExtra("alarmId", 0)

        settingTimeDrum()
        settingInputs()
        settingOperationButton(alarmId)
        settingCalendar()

        // editModeがなくても、alarmIdの有無で判定しても同じ。
        when (editMode) {
            CREATE_MODE -> { setupCreateMode() }
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
        setTargetDateIdentifier(defaultDecorateTarget)

        scope.launch {
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
                        NO_REPEAT -> { setTargetDateIdentifier(defaultDecorateTarget) }
                        DAILY -> {
                            DailyRepeatDialogFragment(unit.text)
                                .onSubmit { pitch ->
                                    setTargetDateIdentifier {
                                        (it.date.toEpochDay() - CalendarDay.today().date.toEpochDay()) % pitch == 0L
                                    }
                                }
                                .execute(supportFragmentManager)
                        }
                        WEEKLY -> {
                            // 曜日の選択肢と、◯週ごとに繰り返す、の入力があれば、毎週でも隔週でも指定可能。
                            // 従って、フォームは１種類で良い。
                            WeeklyRepeatDialogFragment(unit.text)
                                .onSubmit { pair ->
                                    setTargetDateIdentifier {
                                        val diff = (it.date.toEpochDay() - CalendarDay.today().date.toEpochDay()) / DAY_IN_WEEK
                                        (diff % pair.first) == 0L
                                                && pair.second.firstOrNull { day ->
                                            when (day) {
                                                Sun -> { it.date.dayOfWeek == SUNDAY }
                                                Mon -> { it.date.dayOfWeek == MONDAY }
                                                Tue -> { it.date.dayOfWeek == TUESDAY }
                                                Wed -> { it.date.dayOfWeek == WEDNESDAY }
                                                Thu -> { it.date.dayOfWeek == THURSDAY }
                                                Fri -> { it.date.dayOfWeek == FRIDAY }
                                                Sat -> { it.date.dayOfWeek == SATURDAY }
                                            }
                                        } != null
                                    }
                                }
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
                                                    val normalIdentifier = { calendarDay: CalendarDay ->
                                                        list.any { v -> v.date == calendarDay.day }
                                                                || list.any { v -> v.date < 0 && calendarDay.date.isEqual(
                                                            calendarDay.date.withDayOfMonth(1)
                                                                .plusMonths(1)
                                                                .minusDays((-v.date).toLong()))
                                                        }
                                                    }

                                                    val firstAfter29th = list.find { it.date > MIN_END_OF_MONTH }
                                                    if (firstAfter29th != null) {
                                                        val buff = "${firstAfter29th.text}${if (firstAfter29th.date == MAX_END_OF_MONTH) "" else "以降"}"
                                                        val title = "月末${buff}がない場合の設定"
                                                        SingleChoiceDialogFragment(title, EndOfMonth.values())
                                                            .onSubmit { endOfMonth ->
                                                                setTargetDateIdentifier {
                                                                    normalIdentifier(it) || when (endOfMonth) {
                                                                        TO_LAST_DAY -> {
                                                                            val lastDay = it.date.plusDays(1).withDayOfMonth(1).minusDays(1)
                                                                            it.date.isEqual(lastDay) && list.any { v -> lastDay.dayOfMonth < v.date }
                                                                        }
                                                                        TO_NEXT_FIRST_DAY -> {
                                                                            val firstDay = it.date.withDayOfMonth(1)
                                                                            it.date.isEqual(firstDay) && list.any { v -> firstDay.minusDays(1).dayOfMonth < v.date }

                                                                        }
                                                                        NO_SET -> { false }
                                                                    }
                                                                }
                                                            }
                                                            .execute(supportFragmentManager)
                                                    } else {
                                                        setTargetDateIdentifier {
                                                            normalIdentifier(it)
                                                        }
                                                    }
                                                }
                                                .execute(supportFragmentManager)
                                        }
                                        // 週,曜日から設定
                                        1 -> {
                                            MonthlyWeekRepeatDialogFragment(unit.text)
                                                .onSubmit { pair ->
                                                    setTargetDateIdentifier {
                                                        pair.first.any { v -> (it.day - 1) in (DAY_IN_WEEK * (v - 1)) until DAY_IN_WEEK * v
                                                                || (v == -1 && it.date.month != it.date.plusDays(DAY_IN_WEEK.toLong()).month) }
                                                                && when (it.date.dayOfWeek!!) {
                                                            SUNDAY -> { pair.second.any { v -> v == Sun } }
                                                            MONDAY -> { pair.second.any { v -> v == Mon } }
                                                            TUESDAY -> { pair.second.any { v -> v == Tue } }
                                                            WEDNESDAY -> { pair.second.any { v -> v == Wed } }
                                                            THURSDAY -> { pair.second.any { v -> v == Thu } }
                                                            FRIDAY -> { pair.second.any { v -> v == Fri } }
                                                            SATURDAY -> { pair.second.any { v -> v == Sat } }
                                                        }
                                                    }
                                                }
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

    private fun setupCreateMode() {
        val now = Calendar.getInstance()
        hourPicker.value = now.get(Calendar.HOUR_OF_DAY)
        minutePicker.value = now.get(Calendar.MINUTE) / Setting.TIME_PITCH
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

    private fun setTargetDateIdentifier(identifier: CalendarTargetIdentifier) {
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
