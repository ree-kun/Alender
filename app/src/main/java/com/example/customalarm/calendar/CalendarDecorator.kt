package com.example.customalarm.calendar

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import com.example.customalarm.R
import com.prolificinteractive.materialcalendarview.*

class CalendarDecorator(
    private val resources: Resources,
    private val identifier: CalendarTargetIdentifier
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return identifier.isTarget(day)
    }

    override fun decorate(view: DayViewFacade) {
        val res = ResourcesCompat.getDrawable(resources, R.drawable.circle_background, null)
        view.setBackgroundDrawable(res!!)
    }

}

fun interface CalendarTargetIdentifier {
    fun isTarget(day: CalendarDay): Boolean
}
