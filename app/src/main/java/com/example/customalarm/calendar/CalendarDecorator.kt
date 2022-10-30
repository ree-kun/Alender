package com.example.customalarm.calendar

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import com.example.customalarm.R
import com.prolificinteractive.materialcalendarview.*

abstract class CalendarDecorator(private val resources: Resources) : DayViewDecorator {

    override fun decorate(view: DayViewFacade) {
        val res = ResourcesCompat.getDrawable(resources, R.drawable.circle_background, null)
        view.setBackgroundDrawable(res!!)
    }

}
