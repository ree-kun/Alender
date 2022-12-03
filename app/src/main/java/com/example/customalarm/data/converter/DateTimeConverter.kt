package com.example.customalarm.data.converter

import androidx.room.TypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class DateTimeConverter {

    @TypeConverter
    fun fromEpochDate(value: Long): LocalDate {
        return LocalDate.ofEpochDay(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate): Long {
        return date.toEpochDay()
    }

    @TypeConverter
    fun fromTimestamp(value: Int): LocalTime {
        return LocalTime.ofSecondOfDay(value.toLong())
    }

    @TypeConverter
    fun timeToTimestamp(time: LocalTime): Int {
        return time.toSecondOfDay()
    }

}