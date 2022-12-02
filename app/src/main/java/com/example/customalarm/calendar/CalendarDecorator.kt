package com.example.customalarm.calendar

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import com.example.customalarm.R
import com.example.customalarm.calendar.logic.CalendarTargetIdentifierGenerator
import com.example.customalarm.common.data.CalendarHelper
import com.example.customalarm.data.entity.AlarmSettingEntity
import com.prolificinteractive.materialcalendarview.*
import org.threeten.bp.LocalDate

class CalendarDecorator(
    private val resources: Resources,
    var entity: AlarmSettingEntity,
) : DayViewDecorator {

    private val today = LocalDate.now()
    private val targetDate = entity.startDate

    private val startDate = if (today.isAfter(targetDate)) today else targetDate
    private val identifier = entity.generator.generate(entity.startDate.atTime(entity.time))

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
