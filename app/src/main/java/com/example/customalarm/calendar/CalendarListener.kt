package com.example.customalarm.calendar

import com.prolificinteractive.materialcalendarview.*

class CalendarListener : OnDateLongClickListener, OnDateSelectedListener, OnMonthChangedListener {

    override fun onDateLongClick(widget: MaterialCalendarView, date: CalendarDay) {
        println(date)
    }

    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay,
                                selected: Boolean) {
        println(date)
        println(selected)
    }

    override fun onMonthChanged(widget: MaterialCalendarView?, date: CalendarDay?) {
        println(date)
    }

}
