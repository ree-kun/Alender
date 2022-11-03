package com.example.customalarm.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holiday")
data class HolidayEntity (

    @PrimaryKey
    val date: Long,

    val name: String,

)
