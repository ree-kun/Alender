package com.example.customalarm.calendar.logic

import com.example.customalarm.calendar.CalendarTargetIdentifier
import com.example.customalarm.dialog.list.RepeatUnit
import org.threeten.bp.LocalDateTime

abstract class CalendarTargetIdentifierGenerator {

    abstract fun generate(targetDateTime: LocalDateTime): CalendarTargetIdentifier

    abstract fun repeatUnit(): RepeatUnit

    open fun registerValues(): List<String> {
        return mutableListOf()
    }

}