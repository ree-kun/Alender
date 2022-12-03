package com.example.customalarm.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "holiday")
data class HolidayEntity (

    @PrimaryKey
    val date: LocalDate,

    val name: String,

)
