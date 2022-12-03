package com.example.customalarm.calendar.logic

import com.example.customalarm.calendar.CalendarTargetIdentifier
import com.example.customalarm.dialog.list.RepeatUnit
import com.example.customalarm.dialog.list.RepeatUnit.*
import org.threeten.bp.LocalDateTime

class NoRepeatIdentifierGenerator : CalendarTargetIdentifierGenerator() {

    override fun repeatUnit(): RepeatUnit {
        return NO_REPEAT
    }

    override fun generate(targetDateTime: LocalDateTime): CalendarTargetIdentifier {
        val now = LocalDateTime.now()
        var targetDate = targetDateTime.toLocalDate()

        if (now.isAfter(targetDateTime))
            targetDate = targetDate.plusDays(1)

        return CalendarTargetIdentifier { it.isEqual(targetDate) }
    }

}