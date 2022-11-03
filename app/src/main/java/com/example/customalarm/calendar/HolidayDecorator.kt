package com.example.customalarm.calendar

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import com.example.customalarm.R
import com.example.customalarm.common.Constant.Companion.ONE_DAY_IN_MILLIS
import com.example.customalarm.data.entity.HolidayEntity
import com.prolificinteractive.materialcalendarview.*

class HolidayDecorator(
    private val resources: Resources,
    private val holidays: List<HolidayEntity>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return holidays.firstOrNull { it.date.time == day.date.toEpochDay() * ONE_DAY_IN_MILLIS } != null
    }

    override fun decorate(view: DayViewFacade) {
        val res = ResourcesCompat.getDrawable(resources, R.drawable.calendar_holiday_background, null)
        view.setBackgroundDrawable(res!!)
    }
}
