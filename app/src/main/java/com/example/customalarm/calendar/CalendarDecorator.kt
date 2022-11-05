package com.example.customalarm.calendar

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import com.example.customalarm.R
import com.example.customalarm.common.data.CalendarHelper
import com.prolificinteractive.materialcalendarview.*
import org.threeten.bp.LocalDate

class CalendarDecorator(
    private val resources: Resources,
    private val identifier: CalendarTargetIdentifier
) : DayViewDecorator {

    override fun shouldDecorate(calendar: CalendarDay): Boolean {
        val day = CalendarHelper.toLocalDate(calendar)
        return !day.isBefore(LocalDate.now()) && identifier.isTarget(day)
    }

    override fun decorate(view: DayViewFacade) {
        val res = ResourcesCompat.getDrawable(resources, R.drawable.circle_background, null)
        view.setBackgroundDrawable(res!!)
    }

}

fun interface CalendarTargetIdentifier {
    fun isTarget(day: LocalDate): Boolean
}
