package com.example.customalarm.calendar.logic

import com.example.customalarm.calendar.CalendarTargetIdentifier
import com.example.customalarm.calendar.logic.dto.MonthlyDay
import com.example.customalarm.dialog.list.EndOfMonth
import com.example.customalarm.dialog.list.RepeatUnit
import com.example.customalarm.dialog.list.RepeatUnit.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class MonthlyDayIdentifierGenerator(
    private val monthlyDays: List<MonthlyDay>,
    private val endOfMonth: EndOfMonth? = null,
) : CalendarTargetIdentifierGenerator() {

    private val checker = if (endOfMonth == null) this::checkNormally else this::checkEndOfMonth

    override fun repeatUnit(): RepeatUnit {
        return MONTHLY_DAY
    }

    override fun generate(targetDateTime: LocalDateTime): CalendarTargetIdentifier {
        return CalendarTargetIdentifier { checker(it) }
    }

    private fun checkNormally(calendarDate: LocalDate): Boolean {
       return monthlyDays.any { v -> v.dayOfMonth == calendarDate.dayOfMonth }
                || monthlyDays.any { v -> v.dayOfMonth < 0 && calendarDate.isEqual(
            calendarDate.withDayOfMonth(1)
                .plusMonths(1)
                .minusDays((-v.dayOfMonth).toLong()))
        }
    }

    private fun checkEndOfMonth(calendarDate: LocalDate): Boolean {
        return this.checkNormally(calendarDate) || when (endOfMonth!!) {
            EndOfMonth.TO_LAST_DAY -> {
                val lastDateOfMonth = calendarDate.withDayOfMonth(1).plusMonths(1).minusDays(1)
                calendarDate.isEqual(lastDateOfMonth) && monthlyDays.any { v -> lastDateOfMonth.dayOfMonth < v.dayOfMonth }
            }
            EndOfMonth.TO_NEXT_FIRST_DAY -> {
                val firstDayOfMonth = calendarDate.withDayOfMonth(1)
                calendarDate.isEqual(firstDayOfMonth) && monthlyDays.any { v -> firstDayOfMonth.minusDays(1).dayOfMonth < v.dayOfMonth }
            }
            EndOfMonth.NO_SET -> { false }
        }
    }

    override fun registerValues(): List<String> {
        val result = mutableListOf<String>()
        result.addAll(monthlyDays.map { it.dayOfMonth.toString() })
        result.add(endOfMonth?.name ?: "")
        return result
    }

}