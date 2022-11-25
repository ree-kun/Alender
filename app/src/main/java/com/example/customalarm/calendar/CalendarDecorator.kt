package com.example.customalarm.calendar

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import com.example.customalarm.R
import com.example.customalarm.calendar.logic.CalendarTargetIdentifierGenerator
import com.example.customalarm.common.data.CalendarHelper
import com.prolificinteractive.materialcalendarview.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class CalendarDecorator(
    private val resources: Resources,
    var targetDateTime: LocalDateTime,
    var identifierGenerator: CalendarTargetIdentifierGenerator
) : DayViewDecorator {

    private val today = LocalDate.now()
    private val targetDate = targetDateTime.toLocalDate()

    private val startDate = if (today.isAfter(targetDate)) today else targetDate
    private val identifier = identifierGenerator.generate(targetDateTime)

    override fun shouldDecorate(calendar: CalendarDay): Boolean {
        val day = CalendarHelper.toLocalDate(calendar)
        return !day.isBefore(startDate) && identifier.isTarget(day)
    }

    override fun decorate(view: DayViewFacade) {
        val res = ResourcesCompat.getDrawable(resources, R.drawable.circle_background, null)
        view.setBackgroundDrawable(res!!)
    }

}

fun interface CalendarTargetIdentifier {
    fun isTarget(calendarDate: LocalDate): Boolean
}
