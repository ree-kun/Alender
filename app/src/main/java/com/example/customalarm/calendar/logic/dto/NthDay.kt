package com.example.customalarm.calendar.logic.dto

import com.example.customalarm.dialog.list.ListOption

class NthDay(
    val nth: Int,
) : ListOption {
    override val text: String
        get() = if (nth > 0) "第${nth}"
        else when (nth) {
            -1 -> "最終"
            else -> throw IllegalArgumentException("nth must be in range -1 <= 6, but found $nth.")
        }
}