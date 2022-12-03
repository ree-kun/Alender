package com.example.customalarm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.customalarm.data.converter.DateTimeConverter
import com.example.customalarm.data.converter.GeneratorConverter
import com.example.customalarm.data.entity.*

@Database(entities = [AlarmSettingEntity::class, HolidayEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateTimeConverter::class, GeneratorConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmSettingDao(): AlarmSettingDao
    abstract fun holidayDao(): HolidayDao

    // https://somachob.com/android-room/
    companion object {
        private const val DBNAME = "custom_alarm_db.sqlite"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DBNAME
                )
                    // TODO 初期データの投入をここで実装する。
//                    .addCallback(object : Callback() {
//                        override fun onCreate(db: SupportSQLiteDatabase) {
//                            super.onCreate(db)
//
//                            val sql = "INSERT INTO 'holiday' VALUES " +
//                                    "(19684, '勤労感謝の日')"
//                            db.execSQL(sql)
//                        }
//                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}