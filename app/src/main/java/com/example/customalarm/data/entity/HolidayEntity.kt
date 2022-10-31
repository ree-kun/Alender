package com.example.customalarm.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "holiday")
data class HolidayEntity (

    @PrimaryKey
    val date: Date,

    val name: String,

)
