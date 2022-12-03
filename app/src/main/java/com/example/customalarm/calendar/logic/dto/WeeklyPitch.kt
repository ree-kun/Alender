package com.example.customalarm.calendar.logic.dto

import com.example.customalarm.dialog.list.ListOption

class WeeklyPitch(
    val pitch: Int,
) : ListOption {
    override val text: String
        get() = when (pitch) {
            1 -> "毎週"
            2 -> "隔週"
            else -> "${pitch}週間ごと"
        }
}