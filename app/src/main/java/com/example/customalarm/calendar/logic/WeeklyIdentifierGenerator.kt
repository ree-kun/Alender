package com.example.customalarm.calendar.logic

import com.example.customalarm.calendar.CalendarTargetIdentifier
import com.example.customalarm.calendar.logic.dto.WeeklyPitch
import com.example.customalarm.common.Constant
import com.example.customalarm.dialog.list.Day
import com.example.customalarm.dialog.list.Day.*
import com.example.customalarm.dialog.list.RepeatUnit
import com.example.customalarm.dialog.list.RepeatUnit.*
import org.threeten.bp.DayOfWeek.*
import org.threeten.bp.LocalDateTime

class WeeklyIdentifierGenerator(
    private val weeklyPitch: WeeklyPitch,
    private val days: List<Day>,
) : CalendarTargetIdentifierGenerator() {

    override fun repeatUnit(): RepeatUnit {
        return WEEKLY
    }

    override fun generate(targetDateTime: LocalDateTime): CalendarTargetIdentifier {
        return CalendarTargetIdentifier {
            val diff = (it.toEpochDay() - targetDateTime.toLocalDate().toEpochDay()) / Constant.DAY_IN_WEEK
            (diff % weeklyPitch.pitch) == 0L
                    && days.firstOrNull { day ->
                when (day) {
                    Sun -> { it.dayOfWeek == SUNDAY }
                    Mon -> { it.dayOfWeek == MONDAY }
                    Tue -> { it.dayOfWeek == TUESDAY }
                    Wed -> { it.dayOfWeek == WEDNESDAY }
                    Thu -> { it.dayOfWeek == THURSDAY }
                    Fri -> { it.dayOfWeek == FRIDAY }
                    Sat -> { it.dayOfWeek == SATURDAY }
                }
            } != null
        }
    }

    override fun registerValues(): List<String> {
        val result = mutableListOf(weeklyPitch.pitch.toString())
        result.addAll(days.map { it.name })
        return result
    }

    override fun text(): String {
        return "${weeklyPitch.text} ${days.joinToString(",") { it.text }}"
    }

}