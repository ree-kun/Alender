package com.example.customalarm.calendar.logic

import com.example.customalarm.calendar.CalendarTargetIdentifier
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class DailyIdentifier(
    private val targetDateTime: LocalDateTime,
    private val pitch: Int,
) : CalendarTargetIdentifier {

    override fun isTarget(calendarDate: LocalDate): Boolean {
        return (calendarDate.toEpochDay() - targetDateTime.toLocalDate().toEpochDay()) % pitch == 0L
    }

}