package com.example.customalarm.calendar.logic

import com.example.customalarm.calendar.CalendarTargetIdentifier
import com.example.customalarm.common.Constant
import com.example.customalarm.dialog.list.Day
import com.example.customalarm.dialog.list.Day.*
import com.example.customalarm.dialog.list.RepeatUnit
import com.example.customalarm.dialog.list.RepeatUnit.*
import org.threeten.bp.DayOfWeek.*
import org.threeten.bp.LocalDateTime

class MonthlyWeekIdentifierGenerator(
    private val weekOfMonth: List<Int>,
    private val daysOfWeek: List<Day>,
) : CalendarTargetIdentifierGenerator() {

    override fun repeatUnit(): RepeatUnit {
        return MONTHLY_NTH_DAY
    }

    override fun generate(targetDateTime: LocalDateTime): CalendarTargetIdentifier {
        return CalendarTargetIdentifier {
            weekOfMonth.any { v -> (it.dayOfMonth - 1) in (Constant.DAY_IN_WEEK * (v - 1)) until Constant.DAY_IN_WEEK * v
                    || (v == -1 && it.month != it.plusDays(Constant.DAY_IN_WEEK.toLong()).month) }
                    && when (it.dayOfWeek!!) {
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

    override fun registerValues(): List<String> {
        val result = mutableListOf<String>()
        result.addAll(weekOfMonth.map { it.toString() })
        result.addAll(daysOfWeek.map { it.name })
        return result
    }

}