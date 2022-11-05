package com.example.customalarm.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalTime

@Entity(tableName = "alarm_setting")
data class AlarmSettingEntity (

    @PrimaryKey(autoGenerate = true)
    var id: Long,

    val title: String,

    val time: LocalTime,

)
