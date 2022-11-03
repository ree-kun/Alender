package com.example.customalarm.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_setting")
data class AlarmSettingEntity (

    @PrimaryKey(autoGenerate = true)
    var id: Int,

    val title: String,

    val time: String,

)
