package com.example.customalarm.calendar.logic

import com.example.customalarm.calendar.CalendarTargetIdentifier
import com.example.customalarm.common.Constant
import com.example.customalarm.dialog.list.Day
import com.example.customalarm.dialog.list.Day.*
import org.threeten.bp.DayOfWeek.*
import org.threeten.bp.LocalDate

class MonthlyWeekIdentifier(
    private val weekOfMonth: List<Int>,
    private val daysOfWeek: List<Day>,
) : CalendarTargetIdentifier {

    override fun isTarget(calendarDate: LocalDate): Boolean {
        return weekOfMonth.any { v -> (calendarDate.dayOfMonth - 1) in (Constant.DAY_IN_WEEK * (v - 1)) until Constant.DAY_IN_WEEK * v
                || (v == -1 && calendarDate.month != calendarDate.plusDays(Constant.DAY_IN_WEEK.toLong()).month) }
                && when (calendarDate.dayOfWeek!!) {
            SUNDAY -> { daysOfWeek.any { v -> v == Sun } }
            MONDAY -> { daysOfWeek.any { v -> v == Mon } }
            TUESDAY -> { daysOfWeek.any { v -> v == Tue } }
            WEDNESDAY -> { daysOfWeek.any { v -> v == Wed } }
            THURSDAY -> { daysOfWeek.any { v -> v == Thu } }
            FRIDAY -> { daysOfWeek.any { v -> v == Fri } }
            SATURDAY -> { daysOfWeek.any { v -> v == Sat } }
        }
    }

}