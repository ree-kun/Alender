package com.example.customalarm.common.data

import com.prolificinteractive.materialcalendarview.CalendarDay
import org.threeten.bp.LocalDate

class CalendarHelper {

    companion object {

        fun toLocalDate(calendarDay: CalendarDay): LocalDate {
            return LocalDate.ofEpochDay(calendarDay.date.toEpochDay())
        }

    }

}
