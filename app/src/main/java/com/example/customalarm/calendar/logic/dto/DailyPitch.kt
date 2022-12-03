package com.example.customalarm.calendar.logic.dto

import com.example.customalarm.dialog.list.ListOption

class DailyPitch(
    val pitch: Int,
) : ListOption {
    override val text: String
        get() = when (pitch){
            1 -> "毎日"
            else -> "${pitch}日ごと"
        }
}