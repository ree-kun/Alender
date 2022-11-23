package com.example.customalarm.calendar.logic

import com.example.customalarm.calendar.CalendarTargetIdentifier
import com.example.customalarm.common.Constant
import com.example.customalarm.dialog.list.Day
import org.threeten.bp.DayOfWeek.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class WeeklyIdentifier(
    private val targetDateTime: LocalDateTime,
    private val pitch: Int,
    private val days: List<Day>,
) : CalendarTargetIdentifier {

    override fun isTarget(calendarDate: LocalDate): Boolean {
        val diff = (calendarDate.toEpochDay() - targetDateTime.toLocalDate().toEpochDay()) / Constant.DAY_IN_WEEK
        return (diff % pitch) == 0L
                && days.firstOrNull { day ->
            when (day) {
                Day.Sun -> { calendarDate.dayOfWeek == SUNDAY }
                Day.Mon -> { calendarDate.dayOfWeek == MONDAY }
                Day.Tue -> { calendarDate.dayOfWeek == TUESDAY }
                Day.Wed -> { calendarDate.dayOfWeek == WEDNESDAY }
                Day.Thu -> { calendarDate.dayOfWeek == THURSDAY }
                Day.Fri -> { calendarDate.dayOfWeek == FRIDAY }
                Day.Sat -> { calendarDate.dayOfWeek == SATURDAY }
            }
        } != null
    }

}