package com.example.customalarm.calendar.logic

import com.example.customalarm.calendar.CalendarTargetIdentifier
import com.example.customalarm.dialog.list.RepeatUnit
import com.example.customalarm.dialog.list.RepeatUnit.*
import org.threeten.bp.LocalDateTime

class DailyIdentifierGenerator(
    private val pitch: Int,
) : CalendarTargetIdentifierGenerator() {

    override fun repeatUnit(): RepeatUnit {
        return DAILY
    }

    override fun generate(targetDateTime: LocalDateTime): CalendarTargetIdentifier {
        return CalendarTargetIdentifier {
            (it.toEpochDay() - targetDateTime.toLocalDate().toEpochDay()) % pitch == 0L
        }
    }

    override fun registerValues(): List<String> {
        return mutableListOf(pitch.toString())
    }

}