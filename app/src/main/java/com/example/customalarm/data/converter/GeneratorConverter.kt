package com.example.customalarm.data.converter

import androidx.room.TypeConverter
import com.example.customalarm.calendar.logic.*
import com.example.customalarm.calendar.logic.dto.MonthlyDay
import com.example.customalarm.dialog.list.Day
import com.example.customalarm.dialog.list.EndOfMonth
import com.example.customalarm.dialog.list.RepeatUnit
import com.example.customalarm.dialog.list.RepeatUnit.*

class GeneratorConverter {

    companion object {
        private const val DELIMITER = ","
    }

    @TypeConverter
    fun fromGenerator(generator: CalendarTargetIdentifierGenerator): String {
        val list: MutableList<String> = mutableListOf(generator.repeatUnit().name)
        list.addAll(generator.registerValues())
        return list.joinToString(DELIMITER)
    }

    @TypeConverter
    fun toGenerator(string: String): CalendarTargetIdentifierGenerator {
        val values = string.split(DELIMITER)
        return when (RepeatUnit.of(values[0])!!) {
            NO_REPEAT -> NoRepeatIdentifierGenerator()
            DAILY -> DailyIdentifierGenerator(values[1].toInt())
            WEEKLY -> WeeklyIdentifierGenerator(
                values[1].toInt(), values.drop(2).mapNotNull { Day.of(it) })
            MONTHLY_DAY -> {
                val remain = values.drop(1)
                MonthlyDayIdentifierGenerator(
                    remain.mapNotNull { it.toIntOrNull() }.map { MonthlyDay(it) },
                    EndOfMonth.of(remain.last())
                )
            }
            MONTHLY_NTH_DAY -> {
                val remain = values.drop(1)
                MonthlyWeekIdentifierGenerator(
                    remain.mapNotNull { it.toIntOrNull() },
                    remain.mapNotNull { Day.of(it) }
                )
            }
        }
    }

}