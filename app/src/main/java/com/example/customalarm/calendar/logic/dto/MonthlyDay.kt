package com.example.customalarm.calendar.logic.dto

import com.example.customalarm.dialog.list.ListOption

class MonthlyDay(
    val dayOfMonth: Int,
) : ListOption {
    override val text: String
        get() = if (dayOfMonth > 0) "${dayOfMonth}日"
        else when (dayOfMonth) {
            -1 -> "最終日"
            -2 -> "最終日の前日"
            -3 -> "最終日の前々日"
            else -> throw IllegalArgumentException("dayOfMonth must be in range -3 <= 31, but found $dayOfMonth.")
        }
}