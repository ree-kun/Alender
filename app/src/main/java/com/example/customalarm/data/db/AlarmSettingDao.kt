package com.example.customalarm.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.customalarm.data.entity.AlarmSettingEntity

@Dao
interface AlarmSettingDao {

    companion object {
        const val TABLE_NAME = "alarm_setting"
    }

    @Query("SELECT * FROM $TABLE_NAME")
    suspend fun selectAll(): List<AlarmSettingEntity>

    @Query("SELECT * FROM $TABLE_NAME WHERE id = (:id)")
    suspend fun selectById(id: Long): AlarmSettingEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAlarmSetting(entity: AlarmSettingEntity): Long

}
