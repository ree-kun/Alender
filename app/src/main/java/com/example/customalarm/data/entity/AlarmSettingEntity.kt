package com.example.customalarm.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.customalarm.calendar.logic.CalendarTargetIdentifierGenerator
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

@Entity(tableName = "alarm_setting")
data class AlarmSettingEntity (

    @PrimaryKey(autoGenerate = true)
    var id: Long,

    var title: String,

    var startDate: LocalDate,

    var time: LocalTime,

    var generator: CalendarTargetIdentifierGenerator,

)
